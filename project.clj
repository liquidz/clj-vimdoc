(defproject vimdoc "0.1.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [cuma "0.1.1"]
                 [circleci/clj-yaml "0.5.3"]]

  :main vimdoc.core
  :aot [vimdoc.core]
  :uberjar-name "vimdoc-standalone.jar"

  ;:profiles {:dev {:source-paths ["dev"]}}
  ;:aliases {"dev" ["run" "-m" "dev/-main"]}
  )
