(ns repo-store.parse
  (:require [repo-store.front-matter :as front-matter]
            [repo-store.markdown :as md]))

(def content (slurp "./repos/joebadmo/joe.xoxomoon.com-content/blog/2-ft-ui-for-the-10-ft-screen.html.markdown"))

(defn parse [content]
  (let [parsed (front-matter/parse content)]
    (assoc parsed :content (md/render (parsed :content)))))
