(defproject repo-store "0.1.0-SNAPSHOT"
  :description "Store documents from a git repository in a database."
  :url "https://joe.xoxomoon.com"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.eclipse.jgit/org.eclipse.jgit.java7 "3.5.0.201409260305-r"]
                 [clj-jgit "0.8.3"]]
  :main ^:skip-aot repo-store.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
