(ns repo-store.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :refer [split join]]
            [repo-store.core :refer [on-post]]
            [repo-store.database :as db]))

(defroutes app
  (GET "/" [] "hi")
  (GET "/repo/:username/:repo" [username repo]
        (on-post (str username "/" repo))))
