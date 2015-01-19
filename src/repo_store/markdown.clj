(ns repo-store.markdown
  (:require [me.raynes.cegdown :as md]))

(def pegdown-options [:autolinks :fenced-code-blocks :strikethrough :smartypants])

(def render #(md/to-html % pegdown-options))
