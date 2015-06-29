(ns preterition.images
  (:require [clojure.java.io :refer [as-url]]
            [clojure.string :refer [split lower-case]]
            [digest :refer [md5]]
            [fivetonine.collage.util :as util]
            [fivetonine.collage.core :refer :all]
            [preterition.database :refer [get-image write-image]])
  (import [java.io ByteArrayOutputStream]
          java.awt.image.BufferedImage
          javax.imageio.IIOImage
          javax.imageio.ImageWriter
          javax.imageio.ImageWriteParam
          javax.imageio.ImageIO))

(defn get-image-byte-array [^BufferedImage image ext]
  (let [baos (ByteArrayOutputStream.)
        ^ImageWriter writer (.next (ImageIO/getImageWritersByFormatName ext))
        ^ImageWriteParam write-param (.getDefaultWriteParam writer)
        iioimage (IIOImage. image nil nil)
        outstream (ImageIO/createImageOutputStream baos)]
    ; Only compress images that can be compressed. PNGs, for example, cannot be
    ; compressed.
    (when (.canWriteCompressed write-param)
      (doto write-param
        (.setCompressionMode ImageWriteParam/MODE_EXPLICIT)
        (.setCompressionQuality 0.8)))
    (when (.canWriteProgressive write-param)
      (let [mode-map {true  ImageWriteParam/MODE_DEFAULT
                      false ImageWriteParam/MODE_DISABLED}
            mode-flag :progressive]
        (doto write-param
          (.setProgressiveMode (get mode-map
                                    mode-flag
                                    ImageWriteParam/MODE_COPY_FROM_METADATA)))))
    (doto writer
      (.setOutput outstream)
      (.write nil iioimage write-param)
      (.dispose))
    (.close outstream)
    (.toByteArray baos)))

(defn make-local-copy! [uri]
  (let [ext (-> uri (split #"\.") last lower-case)
        hash-name (md5 uri)
        path (str hash-name "." ext)]
    (if-not (get-image path)
      (-> uri
          as-url
          util/load-image
          (resize :width 800)
          (get-image-byte-array ext)
          (write-image path)))
    (str "/img/" path)))
