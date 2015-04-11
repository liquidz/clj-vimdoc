(ns vimdoc.util.path
  (:require
    [clojure.string :as str]))

(def os-name   (.. System getProperties (get "os.name")))
(def windows?  (zero? (.indexOf os-name "Windows")))
(def separator (if windows? "\\" "/"))

(defn file?
  [x]
  (= java.io.File (class x)))

(defn normalize
  [s]
  (if (and (string? s) (.endsWith s separator))
    (str/join (drop-last s))
    s))

(defn join
  [& s]
  (->> s (map normalize)
       (str/join separator)))
