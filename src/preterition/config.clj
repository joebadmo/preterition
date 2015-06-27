(ns preterition.config
  (:require [environ.core :refer [env]]))

(def database-url (env :database-url))
(def database-user (env :database-user))

(def path-prefix "./repos/")

(def ^:private raw-configs [{:username "joebadmo"
                             :repository "joe.xoxomoon.com-content"
                             :branch "repo-store"}])

(defn- enrich [config]
  (let [repo (str (config :username) "/" (config :repository))
        path (str path-prefix repo)]
    (assoc config :repo repo :path path)))

(defn- index-by [k config] {(config k) config})

(def configs (->> (map enrich raw-configs)
                  (map (partial index-by :repo))
                  (reduce #(merge %1 %2) {})))
