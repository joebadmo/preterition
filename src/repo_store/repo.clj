(ns repo-store.repo
  (:require [repo-store.config :as config]
            [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as query]))

(defn- get-url [repo]
  (str "https://github.com/" repo ".git"))

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
             (git/git-checkout (git/load-repo path) branch)
             :all)))
       (let [repo (git/load-repo path)
             log (git/git-log repo)
             head (first log)]
         (do
           (git/git-pull repo)
           (let [change-list (query/changed-files-between-commits repo head (first (git/git-log repo)))]
             (reduce
               (fn [memo [file-name change-type]]
                 (assoc memo change-type (conj (memo change-type) file-name)))
               {:edit #{} :add #{} :delete #{}}
               change-list))))))))

(def repo (git/load-repo "repos/joebadmo/joe.xoxomoon.com-content"))
(git/git-checkout repo "repo-store")
(git/git-branch-list repo)
