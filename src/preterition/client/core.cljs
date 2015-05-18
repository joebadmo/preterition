(ns preterition.client.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! chan take!]]
            [preterition.client.api :refer [get-route-data cached?]]
            [preterition.client.components :refer [Main]]
            [preterition.client.router :refer [start stop router set-title!]]
            [preterition.client.scroll :refer [scroll-to-fragment]]
            [quiescent.core :as q]
            [quiescent.dom :as d]))

(def root (.getElementById js/document "main"))

(def nav [{:title "about" :href "/#about"}
          {:title "code" :href "/#code"}
          {:title "prose" :href "/#prose"}
          {:title "blog" :href "/blog"}])

(defn- activate-nav-item [category fragment {href :href :as nav-item}]
  (if (and
        (not= href "/")
        (or
          (= (str "/" category) href)
          (= (str "/" fragment) href)))
    (assoc nav-item :active true)
    nav-item))

(enable-console-print!)

(defonce application-state
  (atom
    {:nav-data nav
     :loading true
     :route nil}))

(go
  (while true
    (let [{:keys [category path fragment type] :as route} (<! router)]
      (swap! application-state assoc
             :event-type type
             :nav-data (map (partial activate-nav-item category fragment) nav))
      (if (cached? route)
        (swap! application-state assoc
               :loading false
               :route {:data (<! (get-route-data {:category category :path path}))
                       :category category
                       :path path
                       :fragment fragment})
        (do
          (swap! application-state assoc :loading true)
          (swap! application-state assoc
                 :loading false
                 :route {:data (<! (get-route-data {:category category :path path}))
                         :category category
                         :path path
                         :fragment fragment}))))))

(add-watch application-state :watcher
  (fn [key atom old-state new-state]
    (set-title! (-> new-state :route))
    (when (-> new-state :route)
      (q/render (Main new-state) root)
      (if (= (-> new-state :event-type) :click)
        (scroll-to-fragment (-> new-state :route :fragment))))))

(defn on-jsload []
  (stop)
  (start))
