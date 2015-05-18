(ns preterition.util.date
  (:require [preterition.client.scroll :refer [scroll-watch scroll-unwatch]]
            [quiescent.core :as q]
            [quiescent.dom :as dom]))

(defn format-date [date]
  (.toLocaleDateString (js/Date. date)))
