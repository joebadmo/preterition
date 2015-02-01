(defproject repo-store "0.1.0-SNAPSHOT"
  :description "Store documents from a git repository in a database."
  :url "https://joe.xoxomoon.com"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[clj-jgit "0.8.3"]
                 [clj-yaml "0.4.0"]
                 [clj-time "0.9.0"]
                 [compojure "1.3.1"]
                 [me.raynes/cegdown "0.1.1"]
                 [me.raynes/fs "1.4.6"]
                 [org.clojure/clojure "1.6.0"]
                 [org.eclipse.jgit/org.eclipse.jgit.java7 "3.5.0.201409260305-r"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [squirrel "0.1.2-yesql-0.1.0"]
                 [yesql "0.5.0-beta2"]]
  :main ^:skip-aot repo-store.core
  :target-path "target/%s"
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler repo-store.web/app}
  :profiles {:uberjar {:aot :all}})
