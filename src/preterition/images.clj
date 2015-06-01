(ns preterition.images
  (:require [clojure.java.io :refer [file input-stream output-stream copy as-url]]
            [clojure.string :refer [split lower-case]]
            [digest :refer [md5]]
            [fivetonine.collage.util :as util]
            [fivetonine.collage.core :refer :all]))

(defn make-local-copy [uri]
  (let [ext (-> uri (split #"\.") last lower-case)
        hash-name (md5 uri)
        path (str "/img/" hash-name "." ext)
        file-name (str "resources/public" path)]
    (if-not (-> file-name file .exists)
      (-> uri
          as-url
          util/load-image
          (resize :width 800)
          (util/save file-name :quality 0.8)))
    path))
