(ns preterition.render
  (:require [clojure.java.io :as io]
            [clojure.string :refer [split join]]
            [digest :refer [md5]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hickory.render :refer [hiccup-to-html]]
            [optimus.link :as link]
            [preterition.database :as db]
            [preterition.documents :refer [get-document]]
            [preterition.util :refer [convert-hiccup-to-html]])
  (:import [javax.script
            Invocable
            ScriptEngineManager]))

(def nav [{:title "about" :href "/#about"}
          {:title "code" :href "/#code"}
          {:title "prose" :href "/#prose"}
          {:title "blog" :href "/blog"}])

(defn- activate-nav-item [category {href :href :as nav-item}]
  (let [active (= (str "/" category) href)]
    (assoc nav-item :active active)))

(defn get-state [full-path]
  (let [[category & path-tokens] (-> (split full-path #"/") ((partial remove empty?)))
        path (join "/" path-tokens)
        data (if (and (empty? path) (not= category "index"))
               (db/get-documents-by-category category)
               (-> full-path get-document (update-in [:content] convert-hiccup-to-html)))]
    {:nav-data (map (partial activate-nav-item category) nav)
     :loading false
     :route {:data data
             :category (if (not= category "index") category)
             :path path
             :fragment nil}}))

(defn cache-buster [path]
  (->> (str "./resources/public" path) slurp md5 (str path "?")))

(defn render-fn []
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
    (fn render [path title]
      (let [state (get-state path)]
        (html5
          [:head
           [:meta {:charset "utf-8"}]
           [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
           [:meta {:name "viewport" :content "width=device-width"}]
           [:title title]
           [:link {:href "http://fonts.googleapis.com/css?family=Roboto|Inconsolata"
                   :rel "stylesheet"
                   :type "text/css"}]
           (include-css (cache-buster "/css/style.css"))
           [:link {:rel "shortcut icon" :href (cache-buster "/img/joe.xoxomoon.png")}]]
          [:body
           ; Render view to HTML string and insert it where React will mount.
           [:div#main (render-to-string state)]
           [:script#state {:type "application/edn"} state]
           (include-js (cache-buster "/js/main.js"))])))))
