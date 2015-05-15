(ns preterition.client.router
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [put! chan <!]]
            [clojure.string :refer [split join]]
            [goog.events :as events]
            [preterition.client.scroll :refer [start-scroll scroll-events]])
  (:import [goog.history Html5History EventType]))

(defonce html5History (Html5History.))

(def history (.-history js/window)) ; goog.history.setToken doesn't clear the fragment on "/"

(.setUseFragment html5History false)
(.setPathPrefix html5History "")

(def router (chan))

(defn- make-title [category fragment]
  (let [tail (-> fragment (split "#") second)]
    (join " | " (filter not-empty ["joe.xoxomoon.com" category tail]))))

(go
  (while true
    (let [fragment (<! scroll-events)
          path (.-pathname (.-location js/window))
          oldState (js->clj (.-state history) :keywordize-keys true)
          newState (assoc oldState :fragment fragment)
          title (make-title "" fragment)]
      (put! router {:fragment fragment
                    :title title
                    :type :scroll})
      (. history (replaceState (clj->js newState) nil (str path fragment))))))

(defn handle-click [e]
  (let [target (.-target e)
        tag-name (.-tagName target)
        host (.-host (.-location js/window))
        target-host (.-host target)]
    (when (and (= tag-name "A") (= host target-host))
      (.preventDefault e)
      (let [full-path (.-pathname target)
            [category & path-tokens] (-> (split full-path #"/") ((partial remove empty?)))
            path (join "/" path-tokens)
            fragment (.-hash target)
            title (make-title category fragment)
            route {:category (if category category "")
                   :path path
                   :fragment fragment
                   :title title
                   :type :click}]
        (put! router route)
        (. history (pushState (clj->js route) nil (str full-path fragment)))))))

(defn handle-pop [e]
  (let [route (js->clj (.-state e) :keywordize-keys true)]
    (put! router (assoc route :type :pop))))

(defn start []
  (events/listen js/document "click" handle-click)
  (events/listen js/window "popstate" handle-pop)
  (start-scroll)
  (let [location (.-location js/window)
        full-path (.-pathname location)
        [category & path-tokens] (-> (split full-path #"/") ((partial remove empty?)))
        path (join "/" path-tokens)
        fragment (.-hash location)]
    (put! router {:category category
                  :path path
                  :fragment fragment
                  :title "joe.xoxomoon.com"})))

(defn stop []
  (events/removeAll js/document "click")
  (events/removeAll js/document "popstate")
  (events/removeAll js/window "scroll"))

(defonce init (start))

(.setEnabled html5History true)
