(ns repo-store.parse
  (:require [clojure.string :refer [join split]]
            [clj-time.core :as t]
            [repo-store.front-matter :as fm]
            [repo-store.markdown :as md]))

(defn strip-ext [filename]
  (->> (split filename #"\.")
       (drop-last)
       (join ".")))

(defn parse-post [full-filename]
  (let [tokens (split full-filename #"\/")
        dir (->> (drop-last tokens) (join "/"))
        filename (last tokens)
        filename-split (split filename #"-")]
    {:date (->> filename-split
                (take 3)
                (map #(Integer. %))
                (apply t/date-time))
     :path (->> filename-split
                (drop 3)
                (join "-")
                (strip-ext)
                (str dir "/"))}))

(defn parse [filename content]
  (let [parsed (fm/parse content)]
    (->
      (if (-> (split filename #"\/") (first) (= "blog"))
        (merge parsed (parse-post filename))
        (merge parsed {:path (strip-ext filename)}))
      (update-in [:content] md/render)
      (merge {:filename filename})
      (clojure.set/rename-keys {:date :post-date}))))
