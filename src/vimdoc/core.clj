(ns vimdoc.core
  (:gen-class)
  (:require
    [clojure.string   :as str]
    [clojure.java.io  :as io]
    [clj-yaml.core    :as yaml]
    [cuma.core        :refer [render]]
    [vimdoc.util.path :as path]))

(def ^:const DOC_DIR_NAME  "doc")
(def ^:const VIMDOC_YAML   "vimdoc.yml")
(def ^:const TEMPLATE_FILE "help_template.txt")
(def ^:const PLUGIN_DIRS   ["plugin" "autoload" "ftplugin"])

(defn- str-drop [n s] (apply str (drop n s)))
(defn- str-drop-last [n s] (apply str (drop-last n s)))

(defn- meta-line? [s] (.startsWith s "@"))

(defn pickup-doc-comments
  [filename]
  (loop [[line & rest-lines] (str/split-lines (slurp filename))
         started? false
         tmp      []
         result   []]
    (if-not line
      result
      (if started?
        (if (.startsWith line "\" ")
          (recur rest-lines true (conj tmp (str-drop 2 line)) result)
          (recur rest-lines false [] (conj result (conj tmp line))))
        (recur rest-lines (.startsWith line "\"\"") [] result)))))

(def ann
  {"@introduction" :introduction
   "@function"     :function
   "@command"      :command
   "@customize"    :customize
   "@changelog"    :changelog})

(defn- get-type-from-comments
  [cs]
  (->> cs
       (filter meta-line?)
       (map #(get ann %))
       (drop-while nil?)
       first))

(defn- get-type-from-definition
  [s]
  (condp #(.startsWith %2 %1) s
    "function" :function
    "command"  :command
    "imap"     :mapping
    "inoremap" :mapping
    "nmap"     :mapping
    "nnoremap" :mapping
    "vmap"     :mapping
    "vnoremap" :mapping
    "xmap"     :mapping
    "xnoremap" :mapping
    nil))

(defn parse-function
  [s]
  (let [start (inc (.indexOf s " "))
        end   (.lastIndexOf s " ")
        func  (apply str (drop start (take end s)))
        i     (.indexOf func "(")]
    {:name (apply str (take i func))
     :arg  (apply str (drop i func))}))

(defn parse-command
  [s]
  (->> (str/split s #"\s+")
       (drop 1)
       (drop-while #(.startsWith % "-"))
       first
       (hash-map :name)))

(defn parse-mapping
  [s]
  {:name (re-find #"<Plug>[^ ]+" s)})

(defn parse-customize
  [s]
  {:name (re-find #"g:[^'\" ]+" s)})

(defn parse-docs
  [ls]
  (map #(let [strings    (drop-last %)
              definition (last %)
              t (or (get-type-from-comments strings)
                    (get-type-from-definition definition)
                    :introduction)
              text (str/join "\n" (remove meta-line? strings))
              base {:type t, :text text}]
          (merge
            base
            (case t
              :function  (parse-function definition)
              :command   (parse-command definition)
              :customize (parse-customize definition)
              :mapping   (parse-mapping definition)
              {})))
       ls))

(defn categorize-docs
  [docs]
  (let [join-text-fn #(if (seq %) (str/join "\n" (map :text %)))]
    (-> (group-by :type docs)
        (update-in [:introduction] join-text-fn)
        (update-in [:changelog] join-text-fn))))

(defn render-help
  ([docs]
   (render-help TEMPLATE_FILE docs))
  ([tmpl-file docs]
   (-> tmpl-file
       io/resource
       slurp
       (render docs))))

(defn load-config
  [filename]
  (-> filename slurp yaml/parse-string
      (update-in [:exclude] #(map re-pattern %))))

(defn get-vim-files
  [dir]
  (->> PLUGIN_DIRS
       (map #(path/join dir %))
       (mapcat (comp file-seq io/file))
       (filter path/file?)
       (filter #(.endsWith (.getName %) ".vim"))))

(defn exclude-files
  [exclude-list files]
  (reduce
    (fn [res exclude-regexp]
      (remove #(re-find exclude-regexp (.getAbsolutePath %)) res))
    files
    exclude-list))

(defn -main
  [target-dir]
  (let [target-dir (.getAbsolutePath (io/file target-dir))
        doc-dir    (path/join target-dir DOC_DIR_NAME)
        conf       (load-config (path/join target-dir VIMDOC_YAML))
        help-file  (path/join doc-dir (str (:name conf) ".txt"))]

    (.mkdir (io/file doc-dir))

    (->> (get-vim-files target-dir)
         (exclude-files (:exclude conf))
         (mapcat pickup-doc-comments)
         parse-docs
         categorize-docs
         (merge conf)
         render-help
         (spit help-file))))
