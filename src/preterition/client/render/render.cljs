(ns preterition.client.render
  (:require [cljs.reader :refer [read-string]]
            [preterition.client.components :refer [Main]]))

(defn ^:export render-to-string
  [state]
  (->> state
       read-string
       Main
       (.renderToString js/React)))
