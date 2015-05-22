(ns preterition.render
  (:require [clojure.java.io :as io]
            [hiccup.page :refer [html5 include-css include-js]])
  (:import [javax.script
            Invocable
            ScriptEngineManager]))

((render-fn) "foo" "bar")

(defn- render-fn []
  (let [js (doto (.getEngineByName (ScriptEngineManager.) "nashorn")
             (.eval "var global = this;")
             (.eval (-> "js/render.js"
                        io/resource
                        io/reader)))
        main (.eval js "preterition.client.render")
        render-to-string (fn [edn]
                           (.invokeMethod
                            ^Invocable js
                            main
                            "render_to_string"
                            (-> edn
                                pr-str
                                list
                                object-array)))]
    (fn render [title bar]
      (html5
       [:head
        [:meta {:charset "utf-8"}]
        [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
        [:meta {:name "viewport" :content "width=device-width"}]
        [:title (str title " | Omelette")]]
       [:body
        ; (include-css "//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.1.1/css/bootstrap.css")
        ; (include-css "//fonts.googleapis.com/css?family=Open+Sans:300")
        ; (include-css "/assets/stylesheets/style.css")
        ; (include-js "/assets/scripts/main.js")
        ; Render view to HTML string and insert it where React will mount.
        [:div#omelette-app (render-to-string {:title "static"
                                              :nav-data []
                                              :loading true
                                              :route {:category nil
                                                      :path nil
                                                      :data {:content "foo"}}})]
        ; Serialize app state so client can initialize without making an additional request.
        ; Initialize client and pass in IDs of the app HTML and app EDN elements.
        ]))))
        ; [:script {:type "text/javascript"} "omelette.view.init('omelette-app', 'omelette-state')"]])))
