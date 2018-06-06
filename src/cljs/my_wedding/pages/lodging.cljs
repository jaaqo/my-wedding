(ns my-wedding.pages.lodging
  (:require [reagent.core :as r]
            [cljsjs.react-google-maps]
            [npm]))

(def google-map (r/adapt-react-class (aget js/ReactGoogleMaps "GoogleMap")))
(def marker (r/adapt-react-class (aget js/ReactGoogleMaps "Marker")))

(def with-google-map (aget js/ReactGoogleMaps "withGoogleMap"))
(def with-script-js (aget js/ReactGoogleMaps "withScriptjs"))

(defn wedding-google-map []
  [google-map {:default-center {:lat 60.6045783 :lng 24.7953813}
               :default-zoom 11}
   [marker {:position {:lat 60.5912241 :lng 24.6721355}}]
   [marker {:position {:lat 60.6279934 :lng 24.8533823}}]
   [marker {:position {:lat 60.6312416 :lng 24.8595934}
            :clickable true}]])
 
 

(defn g-map [props]
  (let [m (-> wedding-google-map
              r/reactify-component
              with-google-map
              with-script-js
              r/adapt-react-class)]
    [m props]))

(defn lodging-page []
  (let [container-element (r/create-element "div" (clj->js {:style {:height "300px" :width "100%"}}))
        map-element       (r/create-element "div" (clj->js {:style {:height "300px" :width "100%"}}))
        loading-element   (r/create-element "div" (clj->js {:style {:height "300px" :width "100%"}}))]
    [:div
     [:h1 "Majoitus ja semmoset"]
     [:p "Mei in dico movet aliquid. Est an laudem moderatius. Ea mei inani tractatos. An nec duis porro disputando, usu case aeterno ne."]
     [:div 
      [g-map {:container-element container-element
              :map-element       map-element
              :loading-element   loading-element
              :googleMapURL "https://maps.googleapis.com/maps/api/js?key=AIzaSyDP4DR2j8C6LPscHk9rbtjX2QI7x7ou9Jc&v=3.exp&libraries=geometry,drawing,places"}
       ]]]))










; :bootstrapURLKeys {:key "AIzaSyDP4DR2j8C6LPscHk9rbtjX2QI7x7ou9Jc"}
#_[:div.rounded-circle.border.border-primary.d-flex.justify-content-center.align-items-center
      {:style {}
            :lat 
            :lng }
      [:i.fas.fa-church {:style {:font-size "1.5em"}}]]
     #_[:div {:style {:height "25px"
                    :width "25px"}
            :lat 
            :lng }
      [:i.fas.fa-birthday-cake]]
     #_[:div {:style {:height "25px"
                    :width "25px"}
            :lat 
            :lng }
      [:i.fas.fa-bed]]
