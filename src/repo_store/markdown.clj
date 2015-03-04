(ns repo-store.markdown
  (:use markdown.core))

(def options [:heading-anchors true])

(def render #(apply md-to-html-string (into [%] options)))
