(ns repo-store.repo
  (:require [clj-jgit.porcelain :as git]
            [clj-jgit.util :as util]
            [clj-jgit.internal :refer :all]
            [clj-jgit.querying :as query])
  (:import [org.eclipse.jgit.diff DiffFormatter DiffEntry]
           [org.eclipse.jgit.util.io DisabledOutputStream]
           [org.eclipse.jgit.diff RawTextComparator]
           [org.eclipse.jgit.revwalk RevWalk RevCommit RevCommitList]
           [org.eclipse.jgit.api Git LogCommand]
           [java.io ByteArrayOutputStream]))

(def path-prefix "./repos/")

(defn- get-url [repo]
  (str "https://github.com/" repo ".git"))

(defn- change-kind
  [^DiffEntry entry]
  (let [change (.. entry getChangeType name)]
    (cond
      (= change "ADD") :add
      (= change "MODIFY") :edit
      (= change "DELETE") :delete
      (= change "COPY") :copy)))

(defn- parse-diff-entry
  [^DiffEntry entry]
  (let [old-path (util/normalize-path (.getOldPath entry))
        new-path (util/normalize-path (.getNewPath entry))
        change-kind (change-kind entry)]
    (cond
      (= old-path new-path)   [new-path change-kind]
      (= old-path "dev/null") [new-path change-kind]
      (= new-path "dev/null") [old-path change-kind]
      :else [old-path change-kind new-path])))

(defn- diff-formatter-for-changes
  [^Git repo]
  (doto
    (DiffFormatter. DisabledOutputStream/INSTANCE)
    (.setRepository (.getRepository repo))
    (.setDiffComparator RawTextComparator/DEFAULT)
    (.setDetectRenames false)))

(defn- changed-files-between-commits
  "List of files changed between two RevCommit objects"
  [^Git repo ^RevCommit old-rev-commit ^RevCommit new-rev-commit]
    (let [df ^DiffFormatter (diff-formatter-for-changes repo)
          entries (.scan df old-rev-commit new-rev-commit)]
      (map parse-diff-entry entries)))

(defn get-change-set [repo-name]
  "takes a repo user/name and clones if it doesn't exist on the file system or pulls if it does,
  then returns sets of added, edited, and deleted files"
  (let [url (get-url repo-name)
        path (str path-prefix repo-name)]
    (if (nil? (git/discover-repo path))
      (do
        (git/git-clone-full url path)
        :all)
      (let [repo (git/load-repo path)
            log (git/git-log repo)
            head (first log)]
        (do
          (git/git-pull repo)
          (let [change-list (changed-files-between-commits repo head (first (git/git-log repo)))]
            (reduce
              (fn [memo [file-name change-type]]
                (assoc memo change-type (conj (memo change-type) file-name)))
              {:edit #{} :add #{} :delete #{}}
              change-list)))))))
