(ns preterition.render
  (:require [clojure.java.io :as io]
            [clojure.string :refer [split join]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hickory.render :refer [hiccup-to-html]]
            [preterition.database :refer [select-documents get-documents-by-category]]
            [preterition.documents :refer [get-document]])
  (:import [javax.script
            Invocable
            ScriptEngineManager]))

(def nav [{:title "about" :href "/#about"}
          {:title "code" :href "/#code"}
          {:title "prose" :href "/#prose"}
          {:title "blog" :href "/blog"}])

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
    (fn render [{:keys [state title]}]
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
         [:script#state {:type "application/edn"} state]
         (include-js "/js/main.js")]))))

(defn mkdirs [filepath]
  (loop [dirs (-> filepath (split #"/") drop-last)
         current "."]
    (when (not-empty dirs)
      (let [next-dir (str current "/" (first dirs))]
        (.mkdir (java.io.File. next-dir))
        (recur (rest dirs) next-dir)))))

(defn write [state path render]
  (let [content (render state)
        filename (str "resources/public/" path ".html")]
    (mkdirs filename)
    (spit filename content)))

(defn render-all []
  (let [render (render-fn)]

    ; render documents
    (doseq [d (select-documents)]
      (write {:state (-> d :path get-state)
              :title (d :title)
              :path (d :path)}
             render))

    ; render blog listing page
    (write {:state {:nav-data (map (partial activate-nav-item "blog") nav)
                    :loading false
                    :route {:data (get-documents-by-category "blog")
                            :category "blog"
                            :path nil
                            :fragment nil}}
            :title "blog"
            :path "blog"}
           render)))

; (render-all)
