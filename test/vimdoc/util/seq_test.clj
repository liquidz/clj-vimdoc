(ns vimdoc.util.seq-test
  (:require
    [clojure.test     :refer :all]
    [vimdoc.util.seq  :refer :all]))

(deftest trim-test
  (is (vector? (trim [1 2 3])))
  (are [x y] (= x (trim y))
    ["1"]        ["" "1"]
    ["1"]        ["1" ""]
    ["1"]        ["" "1" ""]
    ["1" "" "2"] ["" "1" "" "2" ""]))

