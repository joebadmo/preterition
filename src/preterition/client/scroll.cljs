(ns preterition.client.scroll
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan sliding-buffer mult tap close! pipe]]
            [clojure.string :refer [split]]
            [goog.events :as events]
            [preterition.client.util.async :refer [debounce]]
            [preterition.client.util.browser :refer [in-browser]]))

(defn scroll-to-fragment [fragment]
  (let [fragment-string (second (split fragment "#"))]
    (if-let [element (.querySelector js/document (str "[name=\"" fragment-string "\"]"))]
      (.scroll js/window 0 element.offsetTop)
      (.scroll js/window 0 0))))

(def ^:private raw-scroll-events (chan (sliding-buffer 1)))
;nashorn chokes on mult because it apparently uses a browser-only feature
(def ^:private scroll-mult (when in-browser (mult raw-scroll-events)))
(def ^:private raw-resize-events (chan (sliding-buffer 1)))

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

(def kill-watch-events (chan))

(defn get-current-fragment [boundaries]
  (fn []
    (let [p (.-pageYOffset js/window)
          filtered (filterv #(>= (.ceil js/Math p) (:pos %)) boundaries)
          m (apply max-key :pos filtered)]
      (if m (m :name) ""))))

(def deduped (chan (sliding-buffer 1) (dedupe)))

(defn scroll-watch [fragments]
  (let [boundaries (get-boundaries-map fragments)
        filtered (chan (sliding-buffer 1) (map (get-current-fragment boundaries)))
        resize-events (debounce raw-resize-events 500)]
    (tap scroll-mult filtered)
    (pipe filtered deduped false)
    (pipe deduped scroll-events false)
    (go
      (let [[v c] (alts! [resize-events kill-watch-events])]
        (close! filtered)
        (close! resize-events)
        (when (= c resize-events)
          (>! scroll-events ((get-current-fragment boundaries)))
          (scroll-watch fragments))))))

(defn scroll-unwatch [fragments]
  (put! kill-watch-events fragments))

(defn start-scroll []
  (events/listen js/window "scroll" #(put! raw-scroll-events %))
  (events/listen js/window "resize" #(put! raw-resize-events true)))
