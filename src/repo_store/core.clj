(ns repo-store.core
  (:require [repo-store.repo :as repo]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (repo/get-change-set "joebadmo/joe.xoxomoon.com-content"))
