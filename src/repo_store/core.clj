(ns repo-store.core
  (:require [clojure.string :refer [join split]]
            [me.raynes.fs :refer [file walk]]
            [repo-store.config :refer [configs path-prefix]]
            [repo-store.repo :refer :all]
            [repo-store.parse :refer [parse]]
            [repo-store.database :as db]))

(def ^:private filter-markdown-files
  (partial filter (partial re-matches #".*\.(md|markdown)$")))

(defn- slurp-file [repo-name filename]
  (->> filename
       (str path-prefix repo-name "/")
       (slurp)))

(defn- strip-ext [filename]
  (->> (split filename #"\.")
       (drop-last)
       (join ".")))

(defn- get-document [repo-name filename]
  (-> filename
      ((partial slurp-file repo-name))
      parse
      (assoc :filename filename
             :path (strip-ext filename))))

(defn- get-all-markdown-filenames [dir]
  (let [current-path (.getPath (file dir))
        prefix-dir-count (count (split current-path #"\/"))]
    (->> dir
         (walk
           (fn [root dirs files]
             (let [path (.getPath root)
                   split (split path #"\/")
                   tail (drop prefix-dir-count split)
                   prefix (join "/" tail)]
               (map #(join [prefix "/" %]) files))))
         flatten
         filter-markdown-files)))

(defn get-all-documents [conf]
  (let [repo-name (conf :repo)]
    (->> (get-all-markdown-filenames (str path-prefix repo-name))
         (map (partial get-document repo-name)))))

(defn get-document-set [conf]
  (let [repo (get-repo (conf :repo) (conf :branch))
        [get-commit get-change-list] (map #(partial % repo) [get-commit get-change-list])
        get-document (partial get-document (conf :repo))
        get-documents (partial map get-document)
        head-commit (get-head-commit repo)
        head-commit-map (-> (get-commit-map head-commit)
                            (merge (select-keys conf [:repository :username])))]
    (if-let [newest-commit-hash ((db/get-newest-commit-map) :git-commit-hash)]
      (if (not= newest-commit-hash (head-commit-map :git-commit-hash))
        (let [newest-commit (get-commit newest-commit-hash)
              change-set (get-change-list newest-commit head-commit)]
          (-> (merge change-set {:git-commit head-commit-map})
              (update-in [:add] get-documents)
              (update-in [:edit] get-documents)
              (update-in [:delete] (partial map strip-ext)))))
      (->> (get-all-documents conf)
           (assoc {:git-commit head-commit-map} :add)))))

(defn on-post [repo]
  (if-let [config (configs repo)]
    (if-let [document-set (get-document-set config)]
      (do
        (db/update document-set)
        document-set)
      "nothing new")))

; (on-post "joebadmo/joe.xoxomoon.com-content")
