(ns preterition.assets
  (:require [optimus.export :as export]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]))

(defn get-assets []
  (concat
    (assets/load-bundle "src" "main.js" ["/js/main.js"])
    (assets/load-assets "src" ["/img/joe.xoxomoon.png"])
    (assets/load-bundle "src" "style.css" ["/css/style.css"])))

(defn export-assets []
  (-> (get-assets)
      (optimizations/minify-js-assets nil)
      (optimizations/minify-css-assets nil)
      (optimizations/inline-css-imports)
      (optimizations/concatenate-bundles)
      (export/save-assets "./resources/public/")))
