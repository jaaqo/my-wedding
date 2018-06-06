(ns my-wedding.pages.lodging)

(defn lodging-page []
  [:div.page
   [:h2.page-heading "Majoittuminen"]
   [:div.row>div.col
    [:p "Olemme varanneet huonekiintiön Scandic Hyvinkää -hotellista, joka sijaitsee kävelymatkan päässä kirkolta. Koodilla " [:b "\"Iida ja Jaakko 2018\""] " saatte huonevarauksen (sis. aamiainen) tarjoushinnalla."]]
   [:div.row>div.col
    [:h3 "Scandic Hyvinkää"]
    [:p "Hämeenkatu 2-4, 05800 Hyvinkää"]
    [:p
        "Voitte tehdä varauksen "
     [:a {:href "https://www.scandichotels.fi/hotellit/suomi/hyvinkaa/scandic-hyvinkaa"
          :target "_blank"}
      [:i.fas.fa-external-link-alt] " Hotellin verkkosivujen"]
     " kautta tai puhelimitse numerosta "
     [:a {:href "tel:+358 19 4291 100"}
      [:i.fas.fa-phone] " +358 19 4291 100"]]
    [:p [:b "Teettehän varauksen hyvissä ajoin!"]]]
   [:div.row>div.col {:style {:margin-top "40px"}}
    [:h3 "Muut"]
    [:p "Hyvinkäältä ja Riihimäeltä löytyy myös muita majoitusvaihtoehtoja:"]
    [:ul
     [:li [:a {:href "https://www.hostellipalopuro.fi/"
               :target "_blank"}
           "Hostelli Palopuro"]]
     [:li 
      [:a {:href "https://www.scandichotels.fi/hotellit/suomi/riihimaki/scandic-riihimaki"
           :target "_blank"}
       "Scandic Riihimäki"]]
     [:li
      [:a {:href "http://www.seurahuone.fi/"
           :target "_blank"}
       "Hotel Seurahuone Riihimäki"]]]]])
