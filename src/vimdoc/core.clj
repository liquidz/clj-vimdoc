(ns vimdoc.core
  (:gen-class)
  (:require
    [clojure.string  :as str]
    [clojure.java.io :as io]
    [clojure.edn     :as edn]
    [instaparse.core :as insta]
    [selmer.parser   :as selmp]
    [selmer.filters  :as selmf]
    )
  )

(def ^:const CONFIG_FILE_NAME "vimdoc.edn")
(def ^:const HELP_TEMPLATE    "help_template.txt")
(def ^:const TEXT_WIDTH       78)

(def parser
  (-> "bnf.txt" io/resource slurp insta/parser))

(defn filter-block
  [ls]
  (filter #(and (sequential? %) (= :BLOCK (first %))) ls))

(defn parse-comments
  "ex. (list [:COMMENT \"\\\"\\\"\\\" hello\n\"]
             [:COMMENT \"\\\"\\\"\\\" world\n\"])
  "
  [comments]
  (into [] (comp (map (comp str/trim second))
                 (map #(str/replace-first % #"^\"\"\"\s?" "")))
        comments))

(defmacro re-cond
  [s & clauses]
  (loop [[[regexp body] & ls] (reverse (partition 2 clauses))
         res 'nil ]
    (if (and regexp body)
      (recur ls `(if-let [~'% (re-seq ~regexp ~s)]
                  ~body
                  ~res))
      res)))

(defn parse-definition
  [definition]
  (let [definition (second definition)
        ]
    (re-cond definition
      ;; variable
      #"\s*let (.+?)\s+="
      (let [[[_ var-name]] %]
        {:type :variable :name var-name})
      ;; function
      #"\s*function!? (.+?)\((.+?)\)"
      (let [[[_ func-name func-args]] %]
        ;{:type :function :name (format "%s(%s)" func-name func-args)}
        {:type :function :name func-name :args func-args})
      ;; command
	  #"\s*command!?( \-[^=]+=[^ ]+)* (.+?) "
      (let [[ls] %]
        {:type :command :name (str ":" (last ls))})
      ;; mapping
      #"\s*n(nore)?map( <[^>]+>)* (.+?) "
      (let [[ls] %]
        {:type :mapping :name (last ls)}))))

(defn parse-block
  [block]
  (let [block      (rest block)
        comments   (->> block drop-last parse-comments)
        definition (-> block last parse-definition)]
    (assoc definition :comments comments)))

(defn parse-file
  [file]
  (into []
        (comp (filter #(and (sequential? %) (= :BLOCK (first %))))
              (map parse-block))
        (parser (slurp file))))

(defn get-file-list
  [{:keys [src-dir exclude] :or {src-dir ".", exclude []}}]
  (into []
        (comp (remove #(.isDirectory %))
              (filter #(str/index-of (.getAbsolutePath %) ".vim"))
              (remove (fn [file] (some #(str/index-of (.getAbsolutePath file) %) exclude))))
        (file-seq (io/file src-dir))))

(defn read-config
  [path]
  (-> path io/file slurp edn/read-string))

(defn- selmer-vim-header-filter
  [s title delm]
  (let [label (-> title str/trim str/lower-case (str/replace #" " "-"))
        label (format "%s%s-%s%s" delm s label delm)
        width (- TEXT_WIDTH (count title))]
    (format (str "%s%" width "s") title label)))

(defn- selmer-vim-tag-filter
  [s]
  (let [tag (str "*" s "*")]
    (format (str "%" TEXT_WIDTH "s") tag)))

(defn- selmer-vim-indent-filter
  [s]
  (str (if (= s "<") "" "\t") s))

(defn generate-document-string
  [config]
  (selmf/add-filter! :vim-header selmer-vim-header-filter)
  (selmf/add-filter! :vim-tag selmer-vim-tag-filter)
  (selmf/add-filter! :vim-indent selmer-vim-indent-filter)

  (let [files (get-file-list config)
        data  (mapcat parse-file files)]
    (selmp/render (slurp (io/resource HELP_TEMPLATE))
                  (merge config (group-by :type data)))))

(comment
  (println
    (generate-document-string
      (read-config "test/files/test_plugin/vimdoc.edn"))))

(defn -main
  [& args]
  (try
    (let [config (read-config CONFIG_FILE_NAME)
          help   (generate-document-string config)
          file   (str (:project config) ".txt")
          path   (str/join java.io.File/separator
                           ["doc" file])]
      (spit path help))
    (catch Exception e
      (println e)
      (System/exit 1))))
