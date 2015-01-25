(ns repo-store.parse
  (:require [repo-store.front-matter :as front-matter]
            [repo-store.markdown :as md]))

(defn parse [content]
  (let [parsed (front-matter/parse content)]
    (assoc parsed :content (md/render (parsed :content)))))
