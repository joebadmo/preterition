(defproject repo-store "0.1.0-SNAPSHOT"
  :description "Store documents from a git repository in a database."
  :url "https://joe.xoxomoon.com"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-jgit "0.8.2"]]
  :main ^:skip-aot repo-store.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})