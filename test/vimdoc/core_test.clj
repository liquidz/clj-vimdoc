(ns vimdoc.core-test
  (:require
    [clojure.test     :refer :all]
    [clojure.java.io  :as io]
    [vimdoc.core      :refer :all]
    [vimdoc.util.path :as path]))

(deftest parser-test
  (is (= [:FILE
          [:STRING "1\n\n"]
          [:BLOCK [:COMMENT "\"\"\" aaa\n"] [:DEFINITION "2\n"]]
          [:STRING "3\n\n"]
          [:BLOCK
           [:COMMENT "\"\"\" bbb\n"]
           [:COMMENT "\"\"\" ccc\n"]
           [:DEFINITION "4\n"]]
          [:STRING "\n"]
          [:STRING "5\n"]]
         (parser (slurp (io/file "test/files/bnf/test.txt"))))))

(deftest parse-definition-test
  (are [x y] (= (parse-definition [:DEFINITION x]) y)
    "let foo1 = bar"
    {:type :variable :name "foo1"}

    "function! foo#bar1(x, y) abort"
    {:type :function :name "foo#bar1" :args "x, y"}
    "function foo#bar2(x, y) abort"
    {:type :function :name "foo#bar2" :args "x, y"}
    "function foo#bar3(x, y)"
    {:type :function :name "foo#bar3" :args "x, y"}

    "command! Foo1 call foo#bar(1, 2)"
    {:type :command :name ":Foo1"}
    "command Foo2 call foo#bar(1, 2)"
    {:type :command :name ":Foo2"}
	"command! -nargs=1 Foo3 call foo#bar(<q-args>)"
    {:type :command :name ":Foo3"}
    "command! -nargs=1 -range=% -complete=customlist Foo4 call foo#bar(<q-args>)"
    {:type :command :name ":Foo4"}

    "nnoremap <silent> <Plug>(foo1) :<C-u>Foo<CR>"
    {:type :mapping :name "<Plug>(foo1)"}
    "nnoremap <silent> <buffer> <Plug>(foo2) :<C-u>Foo<CR>"
    {:type :mapping :name "<Plug>(foo2)"}
    "nmap <silent> <Plug>(foo3) :<C-u>Foo<CR>"
    {:type :mapping :name "<Plug>(foo3)"}
    )
  )

(deftest collect-test
  (is (nil?
        nil
        ;(collect (io/file "test/files/test_plugin/autoload/foo.vim"))
        ))
  )


;(deftest parse-command-test
;  (are [x y] (= x (parse-command y))
;    {:name "Foo"} "command Foo call foo#bar()"
;    {:name "Foo"} "command! Foo call foo#bar()"
;    {:name "Foo"} "command! -bar Foo call foo#bar()"))
;
;(deftest parse-mapping-test
;  (are [x y] (= x (parse-mapping y))
;    {:name "<Plug>(foo)"} "nnoremap <Plug>(foo) :<C-u>Foo<CR>"
;    {:name "<Plug>(foo)"} "nnoremap <silent> <Plug>(foo) :<C-u>Foo<CR>"))
;
;(deftest parse-function-test
;  (are [x y] (= x (parse-function y))
;    {:name "foo" :arg "()"} "function foo() abort"
;    {:name "foo" :arg "()"} "function! foo() abort"
;    {:name "foo" :arg "()"} "function! foo () abort"
;    {:name "foo" :arg "()"} "function! foo()"
;    {:name "foo" :arg "()"} "function foo()"
;
;    {:name "foo" :arg "(a, b)"} "function! foo(a, b) abort"
;    {:name "foo" :arg "(a, b)"} "function! foo(a, b)"
;    {:name "foo" :arg "(a, b)"} "function foo(a, b)"))
;
;(deftest load-config-test
;  (let [conf (load-config "test/files/base.yml")]
;    (is (= "bar" (:foo conf)))
;    (is (every? #(= java.util.regex.Pattern (type %)) (:exclude conf)))))
;
;(deftest parse-doc-test
;  (testing "function"
;    (let [doc ["foo" "bar" "function! foo#bar(a, b) abort"]
;          ret (parse-doc doc)]
;      (are [x y] (= x y)
;        :function      (:type ret)
;        "foo\nbar"     (:text ret)
;        "\tfoo\n\tbar" (:indented-text ret)
;        "foo#bar"      (:name ret)
;        "(a, b)"       (:arg ret)))))
;
;(deftest -main-test
;  (testing "init vimdoc.yml"
;    (do (-main "init")
;        (let [f (->> VIMDOC_YAML (path/join ".") io/file)]
;          (is (.exists f))
;          (.delete f))))
;
;  (testing "generate helpfile"
;    (let [plugin-dir (path/join "test" "files" "test_plugin")
;          doc-dir    (path/join plugin-dir "doc")
;          helpfile   (path/join doc-dir "foo.txt")]
;      (-main plugin-dir)
;
;      (are [x] (true? (.exists (io/file x)))
;        doc-dir
;        helpfile)
;      (is (= (slurp helpfile)
;             (slurp (path/join plugin-dir "expected_helpfile.txt"))))
;
;      (doseq [x [helpfile doc-dir]]
;        (.delete (io/file x))))))
