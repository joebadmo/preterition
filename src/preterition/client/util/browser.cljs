(ns preterition.client.util.browser)

(def in-browser (js/eval "typeof window !== 'undefined'"))
(def in-figwheel (js/eval "typeof fw !== 'undefined'"))
