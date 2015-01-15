(ns repo-store.core
  (:require [repo-store.repo :as repo]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (repo/pull "joebadom/joe.xoxomoon.com-content"))

