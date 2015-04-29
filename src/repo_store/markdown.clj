(ns repo-store.markdown
  (:require [clojure.zip :refer [end? insert-child root] :as zip]
            [me.raynes.cegdown :as md]
            [hickory.convert :refer [hiccup-fragment-to-hickory hiccup-to-hickory hickory-to-hiccup]]
            [hickory.core :refer [parse-fragment as-hickory as-hiccup]]
            [hickory.select :refer [tag until] :as select]
            [hickory.zip :refer [hickory-zip]]))

(def ^:private options [:smartypants])

(def ^:private is-heading (select/or
                            (tag :h1)
                            (tag :h2)
                            (tag :h3)
                            (tag :h4)
                            (tag :h5)
                            (tag :h6)))

(defn- add-heading-name [loc]
  (let [content (-> loc zip/node :content first)]
    (if (instance? java.lang.String content)
      (zip/edit loc (fn [l] (update-in l [:attrs] (fn [attrs] (assoc attrs :name content)))))
      loc)))

(defn- add-heading-names [zipper]
  (until #(zip/next (if (is-heading %) (add-heading-name %) %))
         zipper
         end?))

(defn- wrap [v]
  {:type :element, :attrs nil, :tag :div, :content v})

(def render #(-> %
                 (md/to-html options)
                 (parse-fragment)
                 ((partial map as-hickory))
                 vec
                 wrap
                 hickory-zip
                 add-heading-names
                 root
                 hickory-to-hiccup))
