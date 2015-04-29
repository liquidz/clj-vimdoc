(ns vimdoc.util.seq)

(defn trim
  [v]
  (let [blank? #(= "" %)]
    (->> v
         (drop-while blank?)
         reverse
         (drop-while blank?)
         reverse
         vec)))
