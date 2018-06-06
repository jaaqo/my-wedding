(ns my-wedding.navigation
  (:require [reagent.core :as r]
            [secretary.core :as secretary :refer-macros [defroute]]
            [accountant.core :as accountant]
            [my-wedding.pages.response :refer [response-page]]
            [my-wedding.pages.home :refer [home-page]]
            [my-wedding.pages.gift-wishes :refer [gift-wishes-page]]
            [my-wedding.pages.lodging :refer [lodging-page]]
            [my-wedding.pages.directions :refer [directions-page]]
            [my-wedding.pages.error :refer [error-page]]
            [my-wedding.db :as db]))


(defn path []
  (let [loc js/window.location]
    (str (.-pathname loc) (.-query loc) (.-hash loc))))


(defn current-path []
  @(r/track path (db/get-state :page)))


(defn navigation-container []
  (let [links [{:text "Etusivu"
                :href "/"}
               {:href "/ilmoittautuminen"
                :text "Ilmoittautuminen"}
               {:href "/yhteystiedot-ja-ajo-ohjeet"
                :text "Yhteystiedot ja ajo-ohjeet"}
               {:href "/majoittuminen"
                :text "Majoittuminen"}
               {:href "/lahjatoiveet"
                :text "Lahjatoiveet"}]]
    [:nav.navbar.navbar-expand-md.navbar-light
     [:button.navbar-toggler.border-0.mx-auto
      {:type "button"
       :data-toggle "collapse"
       :data-target "#navbar-collapse"}
      [:span.navbar-toggler-icon]]
     [:div#navbar-collapse.collapse.navbar-collapse
      [:ul.navbar-nav
       (doall
        (for [{:keys [text href]} links]
          ^{:key href}
          [:li.nav-item
           [:a.nav-link {:href href
                         :class (when (= (current-path) href) "active")} text]]))]]]))


(defmulti current-page db/get-current-page)

(defmethod current-page :home []
  [home-page])

(defmethod current-page :response []
  [response-page])

(defmethod current-page :gift-wishes []
  [gift-wishes-page])

(defmethod current-page :lodging []
  [lodging-page])

(defmethod current-page :directions []
  [directions-page])

(defmethod current-page :default []
  [error-page])


(defn hook-navigation! []
  (accountant/configure-navigation!
   {:nav-handler (fn [path]
                   (secretary/dispatch! path))
    :path-exists? (fn [path]
                    (secretary/locate-route path))
    :reload-same-path? true}))


(defn init-routes! []
  (defroute "/" []
    (db/set-state assoc :page :home))
  (defroute "/ilmoittautuminen" []
    (db/set-state assoc :page :response))
  (defroute "/lahjatoiveet" []
    (db/set-state assoc :page :gift-wishes))
  (defroute "/majoittuminen" []
    (db/set-state assoc :page :lodging))
  (defroute "/yhteystiedot-ja-ajo-ohjeet" []
    (db/set-state assoc :page :directions))
  (hook-navigation!)
  (accountant/dispatch-current!))
