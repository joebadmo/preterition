(ns repo-store.front-matter
  (:require [clj-yaml.core :as yaml]
            [clojure.string :as str]))

(defn parse [content]
  (let [split-content (str/split content #"---")] ; TODO: fix to make it check that it's the beginning of block
    (if (= (.length split-content) 1)
      {:content (first split-content)}
      (assoc (yaml/parse-string (nth split-content 1))
             :content (nth split-content 2)))))
