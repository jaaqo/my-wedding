(ns my-wedding.core
  (:require [reagent.core :as r]
            [my-wedding.navigation :as nav]
            [my-wedding.components.notification :as notify]))


(enable-console-print!)


(defn root []
  [:div.container
   [:div.row
    [:div.col
     [nav/navigation-container]]]
   [:div.row
    [:div.col
     [notify/notification-container]]]
   [:div.row
    [:div.col
     [nav/current-page]]]])


(defn mount! []
  (r/render-component [root]
                      (js/document.getElementById "app")))


(defn init! []
  (nav/init-routes!)
  (mount!))


(init!)


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (db/set-state update-in [:__figwheel_counter] inc)
)
