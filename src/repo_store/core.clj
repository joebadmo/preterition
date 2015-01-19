(ns repo-store.core
  (:require [repo-store.config :as config]
            [repo-store.repo :as repo]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (repo/get-change-set config/repo))

(def filter-markdown-files (partial filter (partial re-matches #".*\.(md|markdown)$")))

(defn get-contents [file-name]
  (slurp (str config/path-prefix config/repo "/" file-name)))

(def change-set (repo/get-change-set config/repo))
(def added (change-set :add))
(def added-markdown-files (filter-markdown-files added))

(pprint (into {} (map
           #(vector % (get-contents %))
           added-markdown-files)))
