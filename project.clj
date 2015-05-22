(defproject preterition "0.1.0-SNAPSHOT"

  :description "Store documents from a git repository in a database."

  :url "https://joe.xoxomoon.com"

  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}

  :dependencies [[cljs-http "0.1.25"]
                 [clj-jgit "0.8.4"]
                 [clj-time "0.9.0"]
                 [clj-yaml "0.4.0"]
                 [com.cognitect/transit-clj "0.8.271"]
                 [com.cognitect/transit-cljs "0.8.207"]
                 [com.taoensso/nippy "2.9.0-RC2"]
                 [compojure "1.3.4"]
                 [digest "1.4.4"]
                 [hiccup "1.0.5"]
                 [hickory "0.5.4"]
                 [me.raynes/cegdown "0.1.1"]
                 [me.raynes/fs "1.4.6"]
                 [optimus "0.17.1"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3211"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]
                 [quiescent "0.2.0-alpha1"]
                 [ring "1.3.2"]
                 [ring-cors "0.1.6"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [squirrel "0.1.2-yesql-0.1.0"]
                 [yesql "0.5.0-beta2"]]

  :main ^:skip-aot preterition.core

  :target-path "target/%s"

  :plugins [[lein-ring "0.8.11"]
            [lein-cljsbuild "1.0.6"]
            [lein-figwheel "0.3.3"]]

  :clean-targets ^{:protect false} [:target-path "resources/js"]

  :figwheel {:ring-handler preterition.web/app}

  :ring {:handler preterition.web/app}

  :profiles {:uberjar {:aot :all}}

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src/preterition/client"]
              :figwheel { :on-jsload "preterition.client.core/on-jsload" }
              :compiler {:main preterition.client.core
                         :asset-path "/js/out"
                         :output-dir "resources/js/out/dev"
                         :optimizations :whitespace
                         :output-to "resources/js/main.js"}}
             {:id "prod"
              :figwheel false
              :source-paths ["src/preterition/client"]
              :compiler {:source-map "resources/js/main.js.map"
                         :optimizations :advanced
                         :output-dir "resources/js/out"
                         :output-to "resources/js/main.js"} }]})
