(ns preterition.util
  (:require [hickory.render :refer [hiccup-to-html]]))

(def convert-hiccup-to-html #(-> % vector hiccup-to-html))
