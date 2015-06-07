(ns preterition.web
  (:require [cognitect.transit :as transit]
            [compojure.core :refer [context defroutes GET POST ANY routes]]
            [compojure.route :refer [not-found resources]]
            [clojure.string :refer [split join]]
            [preterition.config :refer [env]]
            [preterition.core :refer [on-post]]
            [preterition.database :as db]
            [preterition.documents :as documents]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.util.response :refer [response file-response content-type charset]])
  (import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def fourohfour (not-found "Not found"))

(defn get-path [uri]
  (->> (split uri #"\/")
       (drop 3)
       (join "/")))

(defn write [data]
  (let [out (ByteArrayOutputStream.)
        writer (transit/writer out :json)
        _ (transit/write writer data)
        serialized (.toString out)]
    (.reset out)
    serialized))

(defn get-document [uri]
  (if-let [doc (-> (get-path uri) documents/get-document)]
    (charset {:body (write doc)} "UTF-8")))

(defroutes api-routes
  (context "/api" []
    (POST "/repo/:username/:repo" [username repo]
          (on-post (str username "/" repo)))
    (GET "/documents" [] {:body (db/get-documents)})
    (GET "/document/*" {uri :uri} (if-let [res (get-document uri)] res fourohfour))
    (GET "/category/:category" [category] {:body (-> category db/get-documents-by-category write)})))

(def resource-routes
  (if (= env :prod)
    (routes
      (resources "/" {:root "public"})
      (GET "/" [] (-> (file-response "index.html" {:root "resources/public"}) (content-type "text/html")))
      (GET "/*" {uri :uri}
           (-> uri rest join (str ".html") (file-response {:root "resources/public"}) (content-type "text/html")))
      (ANY "/*" [] fourohfour))
    (routes
      (GET "/*" [] (-> (file-response "index.html" {:root "resources"}) (content-type "text/html")))
      (ANY "/*" [] fourohfour))))


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

(def app (-> (routes
               api-routes
               resource-routes)
             wrap-content-type
             wrap-cors))
