(ns repo-store.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :refer [split join]]
            [repo-store.core :refer [on-post]]
            [repo-store.database :as db]
            [ring.middleware.json :refer [wrap-json-response]]))

(def not-found {:status 404 :body "Not found"})

(defn get-path [uri]
  (->> (split uri #"\/")
       (drop 2)
       (join "/")))

(def get-document
  (GET "/*" {uri :uri}
       (if-let [doc (-> (get-path uri) (db/get-document))]
         {:body doc}
         not-found)))

(defroutes app-routes
  (POST "/repo/:username/:repo" [username repo]
        (on-post (str username "/" repo)))
  (GET "/documents" [] {:body (db/get-documents)})
  (context "/document" [] get-document)
  (GET "/category/:category" [category] {:body (db/get-documents-by-category category)}))

(def app (-> app-routes
             wrap-json-response))
