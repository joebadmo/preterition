(ns repo-store.parse
  (:require [clojure.string :refer [join split]]
            [clj-time.core :as t]
            [repo-store.front-matter :as fm]
            [repo-store.markdown :as md]))

(defn strip-ext [filename]
  (->> (split filename #"\.")
       (drop-last)
       (join ".")))

(defn parse-post [document file-path]
  (let [path-tokens (split file-path #"\/")
        dir (->> (drop-last path-tokens) (join "/"))
        filename (last path-tokens)
        filename-tokens (split filename #"-")
        date-tokens (take 3 filename-tokens)
        slug (->> (drop 3 filename-tokens) (join "-") (strip-ext))]
    (-> document
        (merge {:date (->> date-tokens
                           (map #(Integer. %))
                           (apply t/date-time))
                :path (->> (concat [dir] date-tokens [slug]) (join "/"))})
        (update-in [:alias] #(conj % (str "/" dir "/" slug))))))

(defn parse [filename content]
  (-> (fm/parse content)
      (update-in [:alias] #(conj [] %))
      (update-in [:content] md/render)
      (merge {:filename filename
              :category (first (split filename #"\/"))})
      (#(if (= (% :category) "blog")
        (parse-post % filename)
        (merge % {:path (strip-ext filename)})))
      (clojure.set/rename-keys {:date :post-date :alias :aliases})))
