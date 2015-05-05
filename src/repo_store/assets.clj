(ns repo-store.assets
  (:require [clojure.java.io :refer [file input-stream output-stream copy]]
            [clojure.string :refer [split lower-case]]
            [digest :refer [md5]]
            [repo-store.config :refer [downloads-dir]]))

(defn make-local-copy [uri]
  (let [ext (-> uri (split #"\.") last lower-case)
        hash-name (md5 uri)
        file-name (str downloads-dir "/" hash-name "." ext)]
    (if-not (-> file-name file .exists)
      (with-open [in (input-stream uri)
                  out (output-stream file-name)]
        (copy in out)))
    file-name))
