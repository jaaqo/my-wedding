(ns my-wedding.pages.directions
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

(defn directions-page []
  (let [container-element (r/create-element "div" (clj->js {:style {:height "300px" :width "100%"}}))
        map-element       (r/create-element "div" (clj->js {:style {:height "300px" :width "100%"}}))
        loading-element   (r/create-element "div" (clj->js {:style {:height "300px" :width "100%"}}))]
    [:div.page
     [:h2.page-heading "Näin löydät perille"]
     [:div.row>div.col
      [g-map {:container-element container-element
              :map-element       map-element
              :loading-element   loading-element
              :googleMapURL "https://maps.googleapis.com/maps/api/js?key=AIzaSyDP4DR2j8C6LPscHk9rbtjX2QI7x7ou9Jc&v=3.exp&libraries=geometry,places"}]]
     [:div.row {:style {:margin-top "40px"}}
      [:div.col
       [:h3 "Vihkiminen"]
       [:b "Hyvinkään Vanha kirkko"]
       [:p "Uudenmaankatu 13, 05800 Hyvinkää"]]
      [:div.col
       [:h3 "Hääjuhla"]
       [:b "Kytäjän juhlatila"]
       [:p "Maapässintie 108, 05720 Hyvinkää"]]]
     [:div.row {:style {:margin-top "40px"}}
      [:div.col
       [:h3 "Ajo-ohje kirkolta juhlapaikalle"]
       [:p "Aja " [:b "Uudenmaankatua"] " ja käänny liikennevaloristeyksestä oikealle " [:b "Kalevankadulle."] [:br]
        "Jatka " [:b "Kalevankatua"] " noin 800 m ja käänny vasemmalle " [:b "Läntiselle Yhdystielle (reitti 1361)."] [:br]
        "Aja vastaan tulevasta liikenneympyrästä suoraan ja jatka " [:b "Läntistä Yhdystietä"] " (vaihtuu " [:b "Kytäjäntieksi"] ") noin 9 km." [:br]
        "Käänny vasemmalle " [:b "Palkkisillantielle"] " ja jatka 1,5 km." [:br]
        "Käänny oikealle " [:b "Maapässintielle"] " ja aja tien päähän niin pitkälle, että punainen juhlatila tulee vastaan. Olet perillä!"]]]]))
