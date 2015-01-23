(ns repo-store.core
  (:require [clojure.string :as string]
            [repo-store.config :as config]
            [repo-store.repo :as repo]
            [repo-store.parse :as parse]
            [repo-store.database :as db]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (repo/get-change-set config/repo))

(def filter-markdown-files (partial filter (partial re-matches #".*\.(md|markdown)$")))

(defn get-contents [file-name]
  (->> file-name
    (str config/path-prefix config/repo "/")
    (slurp)))

(def strip-ext #(as-> % s
                  (string/split s #"\.")
                  (drop-last s)
                  (string/join "." s)))

(def change-set (repo/get-change-set config/repo))
(def added (change-set :add))
(def updated (change-set :edit))
(def added-markdown-files (filter-markdown-files added))

(def docs (into {} (map
                   #(vector
                      (strip-ext %)
                      (-> %
                          (get-contents)
                          (parse/parse)
                          (assoc :filename %
                                 :path (first (string/split % #"\.")))))
                   added-markdown-files)))

(def titled-docs (remove #(nil? ((second %) :title)) docs))

;(doseq [doc (map val titled-docs)]
;  (db/insert-document doc))

;(pprint docs)
