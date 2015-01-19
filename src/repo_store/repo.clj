(ns repo-store.repo
  (:require [repo-store.config :as config]
            [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as query]))

(defn- get-url [repo]
  (str "https://github.com/" repo ".git"))

(defn get-change-set [repo-name]
  "takes a repo user/name and clones if it doesn't exist on the file system or pulls if it does,
  then returns sets of added, edited, and deleted files"
  (let [url (get-url repo-name)
        path (str config/path-prefix repo-name)]
    (if (nil? (git/discover-repo path))
      (do
        (git/git-clone-full url path)
        :all)
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
              change-list)))))))
