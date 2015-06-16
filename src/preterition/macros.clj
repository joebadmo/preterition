(ns preterition.macros
  (:require [preterition.config :as config]))

(defmacro env [k] config/env-name)
