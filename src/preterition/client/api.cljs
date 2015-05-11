(ns preterition.client.api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! chan take!]]
            [cljs-http.client :as http]
            [clojure.string :refer [join]]
            [cognitect.transit :refer [read reader]]
            [hickory.render :refer [hiccup-to-html]]))

(def host "http://localhost:3449/")

(def r (reader :json))

(defn deserialize [raw]
  (read r raw))

(defn request [path]
  (let [url (str host "api/" path)]
    (http/get url {:with-credentials? false
                   :channel (chan 1 (map #(-> % :body deserialize)))})))

(def ^:private convert-hiccup-to-html #(-> % vector hiccup-to-html))

(defn request-doc [path]
  (let [url (str host "api/document/" path)]
    (http/get url {:with-credentials? false
                   :channel (chan 1 (map #(-> %
                                              :body
                                              deserialize
                                              (update-in [:content] convert-hiccup-to-html))))})))

(def ^:private memo (atom {}))

(defn get-route-key [{:keys [category path]}]
  (join "/" (filter not-empty [category path])))

(defn get-route-data
  ([]
   (get-route-data nil "index"))
  ([category]
   (go
     (if-let [cached (find @memo category)]
       (val cached)
       (let [res (<! (request (str "category/" category)))]
         (swap! memo assoc category res)
         res))))
  ([category path]
   (go
     (let [full-path (->> [category path]
                          (filter not-empty)
                          (join "/"))]
       (if-let [cached (find @memo full-path)]
         (val cached)
         (let [res (<! (request-doc full-path))]
           (swap! memo assoc full-path res)
           res))))))
