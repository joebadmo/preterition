(ns preterition.documents
  (:require [clojure.zip :refer [end? insert-child root] :as zip]
            [hickory.zip :refer [hickory-zip]]
            [hickory.select :refer [attr until]]
            [hickory.convert :refer [hiccup-fragment-to-hickory hickory-to-hiccup]]
            [preterition.database :as db]))

(def ^:private is-homework-list-node (attr :name (partial = "homework-list")))

(defn get-homework-list-node [loc]
  (let [node (until zip/next loc #(or (end? %) (is-homework-list-node %)))]
    (if (is-homework-list-node node) node nil)))

(defn make-list-item [item]
  [:li {}
   [:em {} (:title item)]
   " &mdash; "
   [:a {:href (str "/" (:path item))} (.toString (:post-date item))]
   [:br]
   (:description item)])

(defn- add-prose-listing [content]
  (let [hw-list (-> (db/get-documents-by-category "prose")
                    ((partial map make-list-item))
                    ((fn [list] (into [:ul {}] list)))
                    vector
                    hiccup-fragment-to-hickory
                    first)]
    (if-let [hw-list-node (-> (hiccup-fragment-to-hickory [content])
                              first
                              hickory-zip
                              get-homework-list-node)]
      (-> hw-list-node
          (insert-child hw-list)
          root
          hickory-to-hiccup)
      content)))

(defn- get-index []
  (-> (db/get-document "index")
      (update-in [:content] add-prose-listing)))

(defn get-document [path]
  (if (= path "index")
    (get-index)
    (db/get-document path)))
