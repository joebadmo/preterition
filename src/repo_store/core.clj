(ns repo-store.core
  (:require [clojure.string :as string]
            [me.raynes.fs :as fs]
            [repo-store.config :as config]
            [repo-store.repo :as repo]
            [repo-store.parse :as parse]
            [repo-store.database :as db]))

(def filter-markdown-files (partial filter (partial re-matches #".*\.(md|markdown)$")))

(def get-contents #(->> %
    (str config/path-prefix config/repo "/")
    (slurp)))

(def strip-ext #(as-> % s
                  (string/split s #"\.")
                  (drop-last s)
                  (string/join "." s)))

(def change-set (repo/get-change-set config/repo config/branch))

(defn make-content-map [file-list]
  (into {}
        (map
          #(vector
             (strip-ext %)
             (-> %
                 (get-contents)
                 (parse/parse)
                 (assoc :filename %
                        :path (strip-ext %))))
          file-list)))

(defn slurp-all [dir]
  (let [current-path (.getPath (fs/file dir))
        prefix-dir-count (count (string/split current-path #"\/"))]
    (->> dir
         (fs/walk
           (fn [root dirs files]
             (let [path (.getPath root)
                   split (string/split path #"\/")
                   tail (drop prefix-dir-count split)
                   prefix (string/join "/" tail)]
               (map #(string/join [prefix "/" %]) files))))
         flatten
         filter-markdown-files)))

(defn create-files [file-list]
  (let [content-map (make-content-map file-list)]
    (doseq [doc (map val content-map)]
      (db/insert-document doc))))

(defn update-files [file-list]
  (let [content-map (make-content-map file-list)]
    (doseq [doc (map val content-map)]
      (db/update-document doc))))

; (-> (slurp-all "repos/joebadmo/joe.xoxomoon.com-content")
;     (update-files))
