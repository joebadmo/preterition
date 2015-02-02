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
  (let [path-tokens (split full-filename #"\/")
        dir (->> (drop-last path-tokens) (join "/"))
        filename (last path-tokens)
        filename-tokens (split filename #"-")
        date-tokens (take 3 filename-tokens)]
    {:date (->> date-tokens
                (map #(Integer. %))
                (apply t/date-time))
     :path (->> filename-tokens
                (drop 3)
                (join "-")
                (str dir "/" (join "/" date-tokens) "/")
                (strip-ext))}))

(defn parse [filename content]
  (let [parsed (-> (fm/parse content) (update-in [:alias] #(conj [] %)))]
    (->
      (if (-> (split filename #"\/") (first) (= "blog"))
        (merge parsed (parse-post filename))
        (merge parsed {:path (strip-ext filename)}))
      (update-in [:content] md/render)
      (merge {:filename filename})
      (clojure.set/rename-keys {:date :post-date :alias :aliases}))))
