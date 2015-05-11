(ns preterition.client.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [preterition.client.api :refer [get-route-data]]
            [cljs.core.async :refer [<! chan take!]]
            [quiescent.core :as q]
            [quiescent.dom :as d]))

(def root (.getElementById js/document "app"))

(enable-console-print!)

(q/defcomponent
  Foo
  [content]
  (d/article #js {:dangerouslySetInnerHTML #js {:__html content}} nil))

(take!
  (get-route-data)
  (fn [body]
    (q/render (Foo (:content body)) root)))
