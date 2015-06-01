(ns preterition.client.components
  (:require [clojure.string :refer [blank?]]
            [preterition.client.scroll :refer [scroll-watch scroll-unwatch]]
            [preterition.util.date :refer [format-date]]
            [quiescent.core :as q]
            [quiescent.dom :as dom]))

(q/defcomponent Home [loading]
  (dom/li
    nil
    (dom/img
      {:src "/img/joe.xoxomoon.png"
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

(q/defcomponent Navbar [{:keys [data loading]}]
  (Nav (concat [(Home loading)] (map NavItem data))))

(q/defcomponent Index
  :on-mount (fn [] (if js/document (scroll-watch ["about" "code" "prose"])))
  :on-unmount (fn [] (if js/document (scroll-unwatch ["about" "code" "prose"])))
  [content]
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

(q/defcomponent PostItem [{:keys [path title]}]
  (dom/li
    nil
    (dom/a {:href path :title title} title)))

(q/defcomponent Listing [{:keys [title children]}]
  (dom/article
    nil
    (dom/header
      nil
      (dom/h2 nil title))
    (apply dom/ul nil children)))

(q/defcomponent Post [{:keys [content title post-date]}]
  (dom/article
    nil
    (dom/header
      nil
      (dom/h2 nil title)
      (dom/time nil (format-date post-date)))
    (dom/div
      {:dangerouslySetInnerHTML {:__html content}})))

(q/defcomponent Footer []
  (dom/footer
    nil
    "© 2015 Joe Moon"
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
         {:href "http://www.linkedin.com/in/joemoon" :icon "icon-linkedin-squared"}]))))

(q/defcomponent Main [{:keys [title nav-data loading route]}]
  (dom/div
    {:className "container"}
    (Navbar {:data nav-data :loading loading})
    (dom/main
      nil
      (let [{:keys [category path data]} route]
        (cond
          (every? blank? [category path])
          (Index (-> route :data :content))
          (blank? path)
          (Listing {:title category
                    :children (-> data ((partial map PostItem)))})
          :else (Post data)))
      (Footer))))
