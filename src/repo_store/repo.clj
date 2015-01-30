(ns repo-store.repo
  (:require [repo-store.config :refer [path-prefix]]
            [repo-store.database :as db]
            [clj-jgit.porcelain :as git]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [clj-jgit.querying :as query]))

(defn- get-url [repo]
  (str "https://github.com/" repo ".git"))

(defn get-repo [repo-name branch]
  (let [path (str path-prefix repo-name)]
    (do
      (when (nil? (git/discover-repo path))
        (git/git-clone-full (get-url repo-name) path))
      (let [repo (git/load-repo path)]
        (do
          (git/git-fetch-all repo)
          (git/git-checkout repo branch)
          repo)))))

(defn get-commit [repo commit-hash]
  (->> (git/git-log repo)
       (filter #(= commit-hash (.name %)))
       (first)))

(def get-head-commit #(first (git/git-log %)))

(def get-change-list query/changed-files-between-commits)

(defn get-change-list [repo old-commit new-commit]
  (let [change-list
        (query/changed-files-between-commits repo old-commit new-commit)]
    (reduce
      (fn [memo [file-name change-type]]
        (assoc memo change-type (conj (memo change-type) file-name)))
      {:edit #{} :add #{} :delete #{}}
      change-list)))

(def ^:private convert-commit-time #(-> % long (* 1000) c/from-long))

(defn get-commit-map [rev-commit]
  {:git-commit-hash (.name rev-commit)
   :git-commit-time (-> rev-commit
                        .getCommitTime
                        convert-commit-time)})

; (def repo (git/load-repo "repos/joebadmo/joe.xoxomoon.com-content"))
; (git/git-checkout repo "repo-store")
; (git/git-branch-list repo)
