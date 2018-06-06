(ns my-wedding.pages.gift-wishes)


(defn gift-wishes-page []
  [:div.page
   [:h2.page-heading "Lahjatoiveet"]
   [:p "Meille tärkeintä on, että tulette juhlimaan kanssamme suurta päiväämme. Jos kuitenkin haluatte muistaa meitä jotenkin, voitte kartuttaa häämatkakassaamme kutsusta löytyvälle tilille tai vaihtoehtoisesti katsoa toiveitamme " [:a {:href "https://lahjalista.net/iidajajaakko2018"
                                                                                                                                                                                                                                             :target "_blank"} "Lahjalista-palvelusta."]]])
