(ns my-wedding.core
  (:require [reagent.core :as r]
            [my-wedding.navigation :as nav]
            [my-wedding.components.notification :as notify]))


(enable-console-print!)

(defn root []
  [:div.container.d-flex.flex-column
   [:header.row.d-flex.align-items-center
    [:div.col.text-center
     [:h1
      [:span.header-top "Iidan ja Jaakon häät"] [:br]
      [:span.text-pink "1.9.2018"] [:br]
      [:span.header-left [:i.fas.fa-clock] "14.00"]
      [:span.header-right [:i.fas.fa-map-marker-alt] "Hyvinkään Vanha kirkko"]]]]
   [:div.row
    [:div.col
     [nav/navigation-container]]]
   [:div.row
    [:div.col
     [notify/notification-container]]]
   [:div.row
    [:div.col
     [nav/current-page]]]
   [:footer.row.d-flex.justify-content-center.align-items-end 
    [:img.img-fluid {:src "/img/risu.png"}]]])


(defn mount! []
  (r/render-component [root] (js/document.getElementById "app")))


(defn init! []
  (nav/init-routes!)
  (mount!))


(init!)


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (db/set-state update-in [:__figwheel_counter] inc)
)
