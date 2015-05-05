(ns repo-store.markdown
  (:require [clojure.string :as string]
            [clojure.zip :refer [edit end? insert-child node root] :as zip]
            [me.raynes.cegdown :as md]
            [hickory.convert :refer [hickory-to-hiccup]]
            [hickory.core :refer [parse-fragment as-hickory]]
            [hickory.select :refer [tag until select-next-loc] :as select]
            [hickory.zip :refer [hickory-zip]]
            [repo-store.assets :refer [make-local-copy]]))

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
  (-> (until #(zip/next (if (is-heading %) (add-heading-name %) %))
             zipper
             end?)
      root
      hickory-zip))

(def ^:private select-next-img (partial select-next-loc (tag :img)))

(defn- replace-image-src [loc]
  (edit loc (fn [l] (update-in l [:attrs] (fn [attrs] (update-in attrs [:src] make-local-copy))))))

(defn- replace-next-image [loc]
  (if-let [t (select-next-img loc)]
    (-> t replace-image-src zip/next)
    (until zip/next loc end?)))

(defn- replace-images [zipper]
  (-> (until replace-next-image zipper end?)
      root
      hickory-zip))

(defn- is-empty-p-tag [loc]
  (and ((tag :p) loc) (nil? (-> loc node :content))))

(def ^:private select-next-empty-p (partial select-next-loc is-empty-p-tag))

(defn- remove-next-empty-p [loc]
  (if-let [t (select-next-empty-p loc)]
    (zip/remove t)
    (until zip/next loc end?)))

(defn- remove-empty-p-tags [zipper]
  (-> (until remove-next-empty-p zipper end?)
      root
      hickory-zip))

(defn- wrap [v]
  {:type :element, :attrs nil, :tag :div, :content v})

(def render #(-> %
                 (md/to-html options)
                 (string/replace #"\n" "")
                 (parse-fragment)
                 ((partial map as-hickory))
                 vec
                 wrap
                 hickory-zip
                 remove-empty-p-tags
                 add-heading-names
                 replace-images
                 root
                 hickory-to-hiccup))
