(ns preterition.util.date)

(defn format-date [date]
  (.toLocaleDateString (js/Date. date)))
