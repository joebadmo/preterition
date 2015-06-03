(ns preterition.client.util.date)

(defn- digits [n]
  (let [s (.toString n)]
    (if (-> s .-length (= 1))
      (str "0" s)
      s)))

(defn format-date [date]
  (let [year (.getFullYear date)
        month (-> date .getMonth (+ 1) digits)
        day (-> date .getDate digits)]
    (str year "-" month "-" day)))
