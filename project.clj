(defproject vimdoc "0.2.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [instaparse "1.4.7"]
                 [cuma "0.1.1"]
                 [circleci/clj-yaml "0.5.3"]]

  :main vimdoc.core
  :aot [vimdoc.core]
  :uberjar-name "vimdoc-standalone.jar")
