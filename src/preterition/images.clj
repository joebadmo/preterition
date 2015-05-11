(ns preterition.images
  (:require [clojure.java.io :refer [file input-stream output-stream copy]]
            [clojure.string :refer [split lower-case]]
            [digest :refer [md5]]
            [preterition.config :refer [downloads-dir]]))

(defn make-local-copy [uri]
  (let [ext (-> uri (split #"\.") last lower-case)
        hash-name (md5 uri)
        path (str downloads-dir "/" hash-name "." ext)
        file-name (str "resources/" path)]
    (if-not (-> file-name file .exists)
      (with-open [in (input-stream uri)
                  out (output-stream file-name)]
        (copy in out)))
    path))
