(ns preterition.client.util.browser)

(def in-browser (js/eval "typeof window !== 'undefined'"))
