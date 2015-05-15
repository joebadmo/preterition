(ns preterition.web
  (:require [cognitect.transit :as transit]
            [compojure.core :refer [context defroutes GET POST ANY]]
            [compojure.route :refer [not-found resources]]
            [clojure.string :refer [split join]]
            [clojure.zip :refer [end? insert-child root] :as zip]
            [hickory.zip :refer [hickory-zip]]
            [hickory.select :refer [attr until]]
            [hickory.convert :refer [hiccup-fragment-to-hickory hickory-to-hiccup]]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :as strategies]
            [preterition.config :refer [env]]
            [preterition.core :refer [on-post]]
            [preterition.database :as db]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :refer [response file-response content-type]])
  (import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def fourohfour (not-found "Not found"))

(defn get-assets []
  (assets/load-assets "images" [#"/.+\.jpg|png$"]))

(defn get-path [uri]
  (->> (split uri #"\/")
       (drop 3)
       (join "/")))

(def ^:private is-homework-list-node (attr :name (partial = "homework-list")))

(defn get-homework-list-node [loc]
  (let [node (until zip/next loc #(or (end? %) (is-homework-list-node %)))]
    (if (is-homework-list-node node) node nil)))

(defn make-list-item [item]
  [:li {}
   [:em {} (:title item)]
   " &mdash; "
   [:a {:href (str "/" (:path item))} (.toString (:post-date item))]
   [:br]
   (:description item)])

(defn- add-prose-listing [content]
  (let [hw-list (-> (db/get-documents-by-category "prose")
                    ((partial map make-list-item))
                    ((fn [list] (into [:ul {}] list)))
                    vector
                    hiccup-fragment-to-hickory
                    first)]
    (if-let [hw-list-node (-> (hiccup-fragment-to-hickory [content])
                              first
                              hickory-zip
                              get-homework-list-node)]
      (-> hw-list-node
          (insert-child hw-list)
          root
          hickory-to-hiccup)
      content)))

(defn write [data]
  (let [out (ByteArrayOutputStream.)
        writer (transit/writer out :json)
        _ (transit/write writer data)
        serialized  (.toString out)]
    (.reset out)
    serialized))

(defn- get-index []
  (-> (db/get-document "index")
      (update-in [:content] add-prose-listing)
      write))

(def get-document
  (GET "/*" {uri :uri}
       (if-let [doc (-> (get-path uri) (db/get-document) write)]
         {:body doc}
         fourohfour)))

(defroutes api-routes
  (context "/api" []
    (POST "/repo/:username/:repo" [username repo]
          (on-post (str username "/" repo)))
    (GET "/documents" [] {:body (db/get-documents)})
    (GET "/document/index" [] {:body (get-index)})
    (context "/document" [] get-document)
    (GET "/category/:category" [category] {:body (db/get-documents-by-category category)}))
  (resources "/" {:root ""})
  (GET "/*" [] (-> (file-response "index.html" {:root "resources"}) (content-type "text/html")))
  (ANY "/*" [] fourohfour))

(def cors-headers
  {"Access-Control-Allow-Origin" "*"
   "Access-Control-Allow-Headers" "Content-Type"
   "Access-Control-Allow-Methods" "GET,POST,OPTIONS"})

(defn wrap-cors
  "Allow requests from all origins"
  [handler]
  (fn [request]
    (let [response (handler request)]
      (update-in response [:headers]
        merge cors-headers))))

(def app (-> api-routes
             (optimus/wrap
               get-assets
               (if (= env :dev)
                 optimizations/none
                 optimizations/all)
               (if (= env :dev)
                 strategies/serve-live-assets
                 strategies/serve-frozen-assets))
             wrap-content-type
             wrap-cors))
