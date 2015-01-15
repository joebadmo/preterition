(ns repo-store.repo
  (:require [clj-jgit.porcelain :as git]))

(def path-prefix "./repos/")

(defn- get-url [repo]
  (str "https://github.com/" repo ".git"))

(defn pull [repo]
  (let [url (get-url repo)
        path (str path-prefix repo)]
    (if (nil? (git/discover-repo path))
      (git/git-clone-full url path)
      (git/git-pull (git/load-repo path)))))
