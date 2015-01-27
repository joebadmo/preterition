(ns repo-store.repo
  (:require [repo-store.config :as config]
            [repo-store.database :as db]
            [clj-jgit.porcelain :as git]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [clj-jgit.querying :as query]))

(defn- get-url [repo]
  (str "https://github.com/" repo ".git"))

(def convert-commit-time #(-> % long (* 1000) c/from-long))

(defn get-commit-map [rev-commit]
  {:git-commit-hash (.name rev-commit)
   :git-commit-time (-> rev-commit
                        .getCommitTime
                        convert-commit-time)
   :username config/username
   :repository config/repository})

(defn get-change-set
  ([repo-name] (get-change-set repo-name "master"))
  ([repo-name branch]
   (let [url (get-url repo-name)
         path (str config/path-prefix repo-name)]
     (if (nil? (git/discover-repo path))
       (do
         (git/git-clone-full url path)
         (let [repo (git/load-repo path)]
           (do
             (git/git-fetch-all repo)
             (git/git-checkout repo branch)
             (-> (git/git-log repo)
                 first
                 get-commit-map
                 db/insert-commit)
             :all)))
       (let [repo (git/load-repo path)]
         (do
           (git/git-pull repo)
           (let [log (git/git-log repo)
                 newest-commit-hash ((db/get-newest-commit-hash) :git-commit-hash) ; TODO: refactor to handle nil
                 newest-commit (first (filter #(= newest-commit-hash (.name %)) log))
                 head (first (git/git-log repo))
                 change-list (query/changed-files-between-commits repo newest-commit head)]
             (do
               (db/insert-commit (get-commit-map head))
               (reduce
                 (fn [memo [file-name change-type]]
                   (assoc memo change-type (conj (memo change-type) file-name)))
                 {:edit #{} :add #{} :delete #{}}
                 change-list)))))))))

; (def repo (git/load-repo "repos/joebadmo/joe.xoxomoon.com-content"))
; (git/git-checkout repo "repo-store")
; (git/git-branch-list repo)
