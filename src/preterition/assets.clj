(ns preterition.assets
  (:require [optimus.export :as export]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]))

(defn get-assets []
  (concat
    (assets/load-bundle "." "main.js" ["/js/main.js"])
    (assets/load-assets "." ["/img/joe.xoxomoon.png"])
    (assets/load-bundle "." "style.css" ["/css/style.css"])))

(defn export-assets []
  (-> (get-assets)
      (optimizations/minify-js-assets nil)
      (optimizations/minify-css-assets nil)
      (optimizations/inline-css-imports)
      (optimizations/concatenate-bundles)
      (export/save-assets "./resources/public/")))
