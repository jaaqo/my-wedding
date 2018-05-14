(ns my-wedding.core
  (:require [reagent.core :as r]
            [my-wedding.navigation :as nav]
            [my-wedding.components.notification :as notify]
            [my-wedding.pages.response :refer [response-page]]
            [my-wedding.pages.home :refer [home-page]]
            [my-wedding.pages.gift-wishes :refer [gift-wishes-page]]
            [my-wedding.pages.error :refer [error-page]]
            [my-wedding.db :as db]))


(enable-console-print!)


(defmulti current-page db/get-current-page)

(defmethod current-page :home []
  [home-page])

(defmethod current-page :response []
  [response-page])

(defmethod current-page :gift-wishes []
  [gift-wishes-page])

(defmethod current-page :default []
  [error-page])


(defn root []
  [:div.container
   [:div.row
    [:div.col
     [nav/navigation]]]
   [:div.row
    [:div.col
     [notify/notification-container]]]
   [:div.row
    [:div.col
     [current-page]]]])


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
