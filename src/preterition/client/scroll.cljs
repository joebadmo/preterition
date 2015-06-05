(ns preterition.client.scroll
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan <! sliding-buffer mult tap]]
            [clojure.string :refer [split]]
            [goog.events :as events]))

(defn scroll-to-fragment [fragment]
  (let [fragment-string (second (split fragment "#"))]
    (if-let [element (.querySelector js/document (str "[name=\"" fragment-string "\"]"))]
      (.scroll js/window 0 element.offsetTop)
      (.scroll js/window 0 0))))

(def ^:private raw-scroll-events (chan (sliding-buffer 1)))

(defn get-boundary [fragment]
  (->> (str "[name=\"" fragment "\"]")
       (.querySelector js/document)
       (.-offsetTop)))

(defn- get-boundaries-map [fragments]
  (map
    #(into {}
       {:name (str "#" %) :pos (get-boundary %)})
    fragments))

(def ^:private watched-fragment-sets (atom {}))

(def scroll-events (chan))

(defn scroll-watch [fragments]
  (if-not (contains? @watched-fragment-sets fragments)
    (let [boundaries (get-boundaries-map fragments)
          previous (atom nil)]
      (go
        (while true
          (let [e (<! raw-scroll-events)]
            (if (@watched-fragment-sets fragments)
              (let [p (.-pageYOffset js/window)
                    filtered (filterv #(>= (.ceil js/Math p) (:pos %)) boundaries)
                    m (apply max-key :pos filtered)
                    current (if m (m :name) "")]
                (when (and (not= @previous current) (> p 0))
                  (>! scroll-events current)
                  (reset! previous current)))))))))
  (swap! watched-fragment-sets assoc fragments true))

(defn scroll-unwatch [fragments]
  (swap! watched-fragment-sets assoc fragments false))

(defn start-scroll []
  (events/listen js/window "scroll" #(put! raw-scroll-events %)))
