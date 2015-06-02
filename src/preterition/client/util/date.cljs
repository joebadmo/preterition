(ns preterition.client.util.date)

(defn format-date [date]
  (.toString (js/Date. date)))
