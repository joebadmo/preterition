(ns preterition.render
  (:require [clojure.java.io :as io]
            [clojure.string :refer [split join]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hickory.render :refer [hiccup-to-html]]
            [preterition.documents :refer [get-document]])
  (:import [javax.script
            Invocable
            ScriptEngineManager]))

(def nav [{:title "about" :href "/#about"}
          {:title "code" :href "/#code"}
          {:title "prose" :href "/#prose"}
          {:title "blog" :href "/blog"}])

(def documents (select-documents))

(def render (render-fn))

(def index (first documents))

(doseq [d documents]
  (write d))

(defn mkdirs [filepath]
  (loop [dirs (-> filepath (split #"/") drop-last)
         current "."]
    (when (not-empty dirs)
      (let [next-dir (str current "/" (first dirs))]
        (.mkdir (java.io.File. next-dir))
        (recur (rest dirs) next-dir)))))

(defn write [document]
  (let [content (render document)
        path (document :path)
        filename (str "resources/static/" path ".html")]
    (mkdirs filename)
    (spit filename content)))

(def ^:private convert-hiccup-to-html #(-> % vector hiccup-to-html))

(defn- activate-nav-item [category {href :href :as nav-item}]
  (let [active (= (str "/" category) href)]
    (assoc nav-item :active active)))

(defn get-state [full-path]
  (let [[category & path-tokens] (-> (split full-path #"/") ((partial remove empty?)))
        path (join "/" path-tokens)]
    {:nav-data (map (partial activate-nav-item category) nav)
     :loading false
     :route {:data (-> full-path get-document (update-in [:content] convert-hiccup-to-html))
             :category (if (not= category "index") category)
             :path path
             :fragment nil}}))

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
    (fn render [document]
      (let [state (-> document :path get-state)
            title (:title document)]
        (html5
          [:head
           [:meta {:charset "utf-8"}]
           [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
           [:meta {:name "viewport" :content "width=device-width"}]
           [:title title]
           [:link {:href "http://fonts.googleapis.com/css?family=Roboto|Inconsolata"
                   :rel "stylesheet"
                   :type "text/css"}]
           (include-css "/css/style.css")
           [:link {:rel "shortcut icon" :href "/images/joe.xoxomoon.png"}]]
          [:body
           ; Render view to HTML string and insert it where React will mount.
           [:div#main (render-to-string state)]
           (include-js "/js/main.js")])))))
