(ns preterition.util
  (:require [hickory.render :refer [hiccup-to-html]]
            [clojure.string :refer [split join]]))

(def convert-hiccup-to-html #(-> % vector hiccup-to-html))

(def nav [{:title "about" :href "/#about"}
          {:title "code" :href "/#code"}
          {:title "prose" :href "/#prose"}
          {:title "blog" :href "/blog"}])

(defn activate-nav-item [category fragment {href :href :as nav-item}]
  (let [active (and
                 (not= href "/")
                 (or
                   (= (str "/" category) href)
                   (= (str "/" fragment) href)))]
    (assoc nav-item :active active)))

(defn get-nav [category fragment]
  (map (partial activate-nav-item category fragment) nav))

(defn parse-url-path [path]
  (let [[category & path-tokens] (-> (split path #"/") ((partial remove empty?)))
        path (join "/" path-tokens)]
    {:category category
     :path path}))
