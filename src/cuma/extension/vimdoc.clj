(ns cuma.extension.vimdoc
  (:require
    [clojure.string :as str]))

(defn append-tabs
  [data & args]
  (let [s (str/join args)
        n (int (Math/ceil (/ (- 48 (count s)) 8)))]
    (str s (str/join (repeat n "\t")))))
