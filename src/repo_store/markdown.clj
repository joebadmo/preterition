(ns repo-store.markdown
  (:require [me.raynes.cegdown :as md]
            [hickory.core :refer [parse-fragment as-hiccup]]))

(def options [:smartypants])

(def render #(-> %
                 (md/to-html options)
                 (parse-fragment)
                 ((partial map as-hiccup))))
