(ns preterition.client.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! chan take!]]
            [cljs.reader :refer [read-string]]
            [goog.dom]
            [preterition.client.api :refer [initialState get-route-data cached?]]
            [preterition.client.components :refer [Main]]
            [preterition.client.router :refer [start stop router set-title!]]
            [preterition.client.scroll :refer [scroll-to-fragment]]
            [preterition.util :refer [parse-url-path get-nav]]
            [quiescent.core :as q]
            [quiescent.dom :as d]))

; (enable-console-print!)

(defonce application-state (atom initialState))

(defn init [state-atom]
  (let [root (if js/document (.getElementById js/document "main"))]
    (go
      (while true
        (let [{:keys [category path fragment type] :as route} (<! router)]
          (swap! state-atom assoc
                 :event-type type
                 :nav-data (get-nav category fragment)
                 :route nil)
          (if-not (cached? route)
            (swap! state-atom assoc :loading true))
          (swap! state-atom assoc
                 :loading false
                 :route {:data (<! (get-route-data {:category category :path path}))
                         :category category
                         :path path
                         :fragment fragment}))))))
(defonce once
  (do
    (if-let [s @application-state]
      (init application-state)
      (let [loc (.-location js/document)
            pathname (.-pathname loc)
            fragment (.-hash loc)
            {:keys [category path]} (parse-url-path pathname)]
        (take!
          (get-route-data {:category category :path path})
          (fn [data]
            (swap! application-state assoc
                   :loading false
                   :nav-data (get-nav category fragment)
                   :route {:data data
                           :category category
                           :path path})
            (init application-state)))))

    ; render initially so client can take over
    ; and scroll hooks on index can start
    (when-let [root (if js/document (.getElementById js/document "main"))]
      (let [state @application-state
            {:keys [category path]} (-> state :route)]
        (q/render (Main state) root)
        (if (every? clojure.string/blank? [category path])
          (scroll-to-fragment (-> js/location .-hash))))

      (add-watch application-state :watcher
                 (fn [key atom old-state new-state]
                   (q/render (Main new-state) root)
                   (when-let [route (new-state :route)]
                     (set-title! route)
                     (if (= (new-state :event-type) :click)
                       (scroll-to-fragment (route :fragment))))))

      (start))))

(defn on-jsload []
  (do
    (init application-state)
    (stop)
    (start)))
