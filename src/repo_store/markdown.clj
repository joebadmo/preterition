(ns repo-store.markdown
  (:require [clojure.zip :refer [edit end? insert-child node root] :as zip]
            [me.raynes.cegdown :as md]
            [hickory.convert :refer [hickory-to-hiccup]]
            [hickory.core :refer [parse-fragment as-hickory]]
            [hickory.select :refer [tag until select-next-loc] :as select]
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
  (let [content (-> loc node :content first)]
    (if (instance? java.lang.String content)
      (edit loc (fn [l] (update-in l [:attrs] (fn [attrs] (assoc attrs :name content)))))
      loc)))

(defn- add-heading-names [zipper]
  (until #(zip/next (if (is-heading %) (add-heading-name %) %))
         zipper
         end?))

(defn- is-empty-p-tag [loc]
  (and ((tag :p) loc) (nil? (-> loc node :content))))

(def ^:private select-next-empty-p (partial select-next-loc is-empty-p-tag))

(defn- remove-empty-p-tags [zipper]
  (until #(if-let [t (select-next-empty-p %)]
                     (zip/remove t)
                     (until zip/next % end?))
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
                 remove-empty-p-tags
                 add-heading-names
                 root
                 hickory-to-hiccup))
