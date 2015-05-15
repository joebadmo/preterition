(ns preterition.client.components
  (:require [quiescent.core :as q]
            [quiescent.dom :as dom]))

(q/defcomponent Home [loading]
  (dom/li
    nil
    (dom/img
      {:src "/images/joe.xoxomoon.png"
       :className (if loading "loading")})
    (dom/a
      {:className "home" :href "/"}
      "joe.xoxomoon.com")))

(q/defcomponent NavItem [{:keys [title href active]}]
  (dom/li
    nil
    (dom/a
      {:href href
       :title title
       :className (if active "active")}
      title)))

(q/defcomponent Nav [nav-items]
  (dom/nav
    nil
    (dom/div
      {:className "container"}
      (apply
        dom/ul
        nil
        nav-items))))

(q/defcomponent Index [content]
  (dom/div
    nil
    (dom/section
      {:className "lead"}
      (dom/p
        nil
        "Just your typical preterite* immigrant combat veteran with a literature degree.")
      (dom/small
        nil
        "* In Pynchon's religious sense, not the grammatical one."))
    (dom/article
      {:dangerouslySetInnerHTML {:__html content}})))

(q/defcomponent Page [content nav-data loading]
  (dom/div
    {:className "container"}
    (Nav (concat [(Home loading)] (map NavItem nav-data)))
    (dom/main
      nil
      (Index content)
      (dom/footer
        nil
        "© 2013 Joe Moon"
        (apply
          dom/ul
          nil
          (map
            (fn [{:keys [href target icon]}]
              (dom/li
                nil
                (dom/a
                  {:href href :target target}
                  (dom/i
                    {:className icon}))))
        [{:href "mailto:joe@xoxomoon" :target "blank" :icon "icon-mail-squared"}
         {:href "https://github.com/joebadmo" :icon "icon-github-squared"}
         {:href "https://twitter.com/joebadmo" :icon "icon-twitter-squared"}
         {:href "http://www.linkedin.com/in/joemoon" :icon "icon-linkedin-squared"}]))))))
