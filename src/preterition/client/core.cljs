(ns preterition.client.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [preterition.client.api :refer [get-route-data]]
            [preterition.client.components :refer [Page]]
            [preterition.client.router :refer [start router]]
            [cljs.core.async :refer [<! chan take!]]
            [quiescent.core :as q]
            [quiescent.dom :as d]))

(def root (.getElementById js/document "app"))

(def nav [{:title "about" :href "/#about"}
          {:title "code" :href "/#code"}
          {:title "prose" :href "/#prose"}
          {:title "blog" :href "/blog"}])

(enable-console-print!)

(go
  (while true
    (let [{:keys [category path fragment title] :as route} (<! router)]
      (prn route)
      (-> (<! (get-route-data category path))
          :content
          (Page nav true)
          (q/render root)))))

(defn on-jsload []
  (start))
