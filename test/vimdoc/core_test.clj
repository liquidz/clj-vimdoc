(ns vimdoc.core-test
  (:require
    [clojure.test :refer :all]
    [vimdoc.core  :refer :all]))

(deftest parse-command-test
  (are [x y] (= x (parse-command y))
    {:name "Foo"} "command Foo call foo#bar()"
    {:name "Foo"} "command! Foo call foo#bar()"
    {:name "Foo"} "command! -bar Foo call foo#bar()"))

(deftest parse-mapping-test
  (are [x y] (= x (parse-mapping y))
    {:name "<Plug>(foo)"} "nnoremap <Plug>(foo) :<C-u>Foo<CR>"
    {:name "<Plug>(foo)"} "nnoremap <silent> <Plug>(foo) :<C-u>Foo<CR>"))

(deftest load-config-test
  (let [conf (load-config "test/files/base.yml")]
    (is (= "bar" (:foo conf)))
    (is (every? #(= java.util.regex.Pattern (type %)) (:exclude conf)))))
