(ns preterition.web
  (:require [cognitect.transit :as transit]
            [compojure.core :refer [context defroutes GET POST ANY]]
            [compojure.route :refer [not-found resources]]
            [clojure.string :refer [split join]]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :as strategies]
            [preterition.config :refer [env]]
            [preterition.core :refer [on-post]]
            [preterition.database :as db]
            [preterition.documents :as documents]
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

(defn write [data]
  (let [out (ByteArrayOutputStream.)
        writer (transit/writer out :json)
        _ (transit/write writer data)
        serialized  (.toString out)]
    (.reset out)
    serialized))

(def get-document
  (GET "/*" {uri :uri}
       (if-let [doc (-> (get-path uri) documents/get-document)]
         {:body (write doc)}
         fourohfour)))

(defroutes api-routes
  (context "/api" []
    (POST "/repo/:username/:repo" [username repo]
          (on-post (str username "/" repo)))
    (GET "/documents" [] {:body (db/get-documents)})
    (context "/document" [] get-document)
    (GET "/category/:category" [category] {:body (-> category db/get-documents-by-category write)}))
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
