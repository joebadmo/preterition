(ns preterition.client.api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! chan take!]]
            [cljs.reader :refer [read-string]]
            [cljs-http.client :as http]
            [clojure.string :refer [join]]
            [cognitect.transit :refer [read reader]]
            [goog.dom]
            [hickory.render :refer [hiccup-to-html]]
            [preterition.client.util.browser :refer [in-figwheel]]
            [preterition.util :refer [convert-hiccup-to-html]]))

(def ^:private host (if in-figwheel "http://localhost:3000/" "/"))

(def ^:private r (reader :json))

(defn- deserialize [raw] (read r raw))

(defn request [path]
  (let [url (str host "api/" path)]
    (http/get url {:with-credentials? false
                   :channel (chan 1 (map #(-> % :body deserialize)))})))

(defn request-doc [path]
  (go
    (-> (<! (request (str "document/" (if (not-empty path) path "index"))))
        (update-in [:content] convert-hiccup-to-html))))

(def initial-state (if-let [e (goog.dom/getElement "state")]
                    (-> e .-textContent read-string)))

(def ^:private memo (atom {}))

(if initial-state
  (let [{:keys [category path]} (initial-state :route)
        k (->> [category path] (filter not-empty) (join "/"))
        res (-> initial-state :route :data)]
    (swap! memo assoc k res)))

(defn cached? [{:keys [category path]}]
  (->> [category path]
       (filter not-empty)
       (join "/")
       (contains? @memo)))

(defn get-route-data [{:keys [category path]}]
  (go
    (let [full-path (->> [category path]
                         (filter not-empty)
                         (join "/"))]
      (if-let [cached (find @memo full-path)]
        (val cached)
        (if (and (empty? path) (not-empty category))
          (let [res (<! (request (str "category/" category)))]
            (swap! memo assoc category res)
            res)
          (let [res (<! (request-doc full-path))]
            (swap! memo assoc full-path res)
            res))))))
