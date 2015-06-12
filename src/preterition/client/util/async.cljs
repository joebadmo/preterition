(ns preterition.client.util.async
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan <! timeout]]))

(defn debounce
  ([in ms] (debounce (chan) in ms))
  ([out in ms]
   (go
     (while true
       (let [e (<! in)]
         (loop [[v ch] [e in]]
           (if (= ch in)
             (recur (alts! [(timeout ms) in]))
             (>! out v))))))
   out))
