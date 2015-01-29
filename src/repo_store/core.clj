(ns repo-store.core
  (:require [clojure.string :as string]
            [me.raynes.fs :as fs]
            [repo-store.config :as config]
            [repo-store.repo :as repo]
            [repo-store.parse :as parse]
            [repo-store.database :as db]))

(def filter-markdown-files
  (partial filter (partial re-matches #".*\.(md|markdown)$")))

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

(defn get-document-set []
  (let [repo (repo/get-repo config/repo config/branch)
        head-commit (repo/get-head-commit repo)
        head-commit-map (repo/get-commit-map head-commit)]
    (if-let [newest-commit-hash ((db/get-newest-commit-map) :git-commit-hash)]
      (let [newest-commit (repo/get-commit repo newest-commit-hash)
            change-set (repo/get-change-list repo newest-commit head-commit)]
        (-> (merge change-set {:git-commit head-commit-map})
            (update-in [:add] (partial map get-document))
            (update-in [:edit] (partial map get-document))
            (update-in [:delete] (partial map strip-ext))))
      (->> (get-all-markdown-filenames (str config/path-prefix config/repo))
           (map get-document)
           (assoc {:git-commit head-commit-map} :add)))))

(defn on-post []
  (db/update (get-document-set)))
