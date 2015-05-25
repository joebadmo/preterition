(ns preterition.client.render
  (:require [cljs.reader :as edn]
            [preterition.client.components :refer [Main]]))

(defn ^:export render-to-string
  [state]
  (->> state
       edn/read-string
       Main
       (.renderToString js/React)))
