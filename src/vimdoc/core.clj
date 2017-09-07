(ns vimdoc.core
  (:gen-class)
  (:require
    [clojure.string   :as str]
    [clojure.java.io  :as io]
    [clojure.edn :as edn]
    [instaparse.core :as insta]
    ;[clj-yaml.core    :as yaml]
    ;[cuma.core        :refer [render]]
    ;[vimdoc.util.path :as path]
    ;[vimdoc.util.seq  :as seq]
    ;cuma.extension.vimdoc
    )
  )

(def ^:const CONFIG_FILE_NAME "vimdoc.edn")

(def parser
  (-> "bnf.txt" io/resource slurp insta/parser))

(defn filter-block
  [ls]
  (filter #(and (sequential? %) (= :BLOCK (first %))) ls))

(defn comment-string
  "Convert parsed comment list to string

  ex. (list [:COMMENT \"\\\"\\\"\\\" hello\n\"]
            [:COMMENT \"\\\"\\\"\\\" world\n\"])
  "
  [comments]
  (transduce (comp (map second)
                   (map #(str/replace-first % #"^\"\"\"\s?" "")))
             str comments))

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
        {:type "variable" :name var-name})
      ;; function
      #"\s*function!? (.+?)\((.+?)\)"
      (let [[[_ func-name func-args]] %]
        {:type "function" :name (format "%s(%s)" func-name func-args)})
      ;; command
	  #"\s*command!?( \-[^=]+=[^ ]+)* (.+?) "
      (let [[ls] %]
        {:type "command" :name (last ls)})
      ;; mapping
      #"\s*n(nore)?map( <[^>]+>)* (.+?) "
      (let [[ls] %]
        {:type "mapping" :name (last ls)}))))

(defn parse-block
  [block]
  (let [block      (rest block)
        comments   (->> block drop-last comment-string)
        definition (-> block last parse-definition)]
    (assoc definition :comments (str/trim comments))))

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

(defn run
  [config]
  (let [files (get-file-list config)
        data  (mapcat parse-file files)
        ]
    (group-by :type data)
    )
  )

;(-> "test/files/test.edn" read-config run)

(defn -main
  [& args]
  (try
    (let [config (read-config CONFIG_FILE_NAME)
          files  (get-file-list config)
          data (mapcat parse-file files)
          ]

      data
      )

    (catch Exception e
      (System/exit 1)
      )
    )
  )
