(ns repo-store.core
  (:require [clojure.string :as string]
            [me.raynes.fs :as fs]
            [repo-store.config :as config]
            [repo-store.repo :as repo]
            [repo-store.parse :as parse]
            [repo-store.database :as db]))

(def filter-markdown-files (partial filter (partial re-matches #".*\.(md|markdown)$")))

(def slurp-file #(->> %
    (str config/path-prefix config/repo "/")
    (slurp)))

(def strip-ext #(->> (string/split % #"\.")
                     (drop-last)
                     (string/join ".")))

(defn get-document [filename]
  (-> filename
      slurp-file
      parse/parse
      (assoc :filename filename
             :path (strip-ext filename))))

(defn get-all-markdown-filenames [dir]
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

(defn create-documents [documents]
  (doseq [doc documents]
    (db/insert-document doc)))

(defn update-documents [documents]
  (doseq [doc documents]
    (db/update-document doc)))

(defn delete-documents [file-names]
  (db/delete-documents (map strip-ext file-names)))

; TODO: parallelize
(defn apply-change-set [change-set]
  (if (= change-set :all)
    (->> (get-all-markdown-filenames (str config/path-prefix config/repo))
         (map get-document)
         (create-documents))
    (do
      (let [deletions (change-set :delete)]
        (when-not (empty? deletions)
          (delete-documents deletions)))
      (->> (change-set :add)
           (map get-document)
           (create-documents))
      (->> (change-set :edit)
           (map get-document)
           (update-documents)))))

; (def change-set (repo/get-change-set config/repo config/branch))
; (apply-change-set change-set)
