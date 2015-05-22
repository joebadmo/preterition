(ns preterition.client.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! chan take!]]
            [preterition.client.api :refer [get-route-data cached?]]
            [preterition.client.components :refer [Main]]
            [preterition.client.router :refer [start stop router set-title!]]
            [preterition.client.scroll :refer [scroll-to-fragment]]
            [quiescent.core :as q]
            [quiescent.dom :as d]))

(enable-console-print!)

(def nav [{:title "about" :href "/#about"}
          {:title "code" :href "/#code"}
          {:title "prose" :href "/#prose"}
          {:title "blog" :href "/blog"}])

(defonce application-state
  (atom
    {:nav-data nav
     :loading true
     :route nil}))

(defn- activate-nav-item [category fragment {href :href :as nav-item}]
  (if (and
        (not= href "/")
        (or
          (= (str "/" category) href)
          (= (str "/" fragment) href)))
    (assoc nav-item :active true)
    nav-item))

(defn init [state-atom]
  (let [root (if js/document (.getElementById js/document "main"))]
    (go
      (while true
        (let [{:keys [category path fragment type] :as route} (<! router)]
          (swap! state-atom assoc
                 :event-type type
                 :nav-data (map (partial activate-nav-item category fragment) nav))
          (if (cached? route)
            (swap! state-atom assoc
                   :loading false
                   :route {:data (<! (get-route-data {:category category :path path}))
                           :category category
                           :path path
                           :fragment fragment})
            (do
              (swap! state-atom assoc :loading true)
              (swap! state-atom assoc
                     :loading false
                     :route {:data (<! (get-route-data {:category category :path path}))
                             :category category
                             :path path
                             :fragment fragment}))))))

    (add-watch state-atom :watcher
       (fn [key atom old-state new-state]
         (when-let [route (new-state :route)]
           (set-title! route)
           (q/render (Main new-state) root)
           (if (= (new-state :event-type) :click)
             (scroll-to-fragment (route :fragment))))))
    (stop)
    (start)))

(defonce go (init application-state))

(defn on-jsload []
  (init application-state))
