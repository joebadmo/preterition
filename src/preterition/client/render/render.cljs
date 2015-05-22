(ns preterition.client.render
  (:require [preterition.client.components :refer [Main]]))

(defn ^:export render-to-string
  [state]
  (.renderToString js/React (Main state)))
