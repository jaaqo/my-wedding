(ns my-wedding.pages.response
  (:require [ajax.core :refer [POST]]
            [reagent.core :as r]
            [clojure.string :as str]
            [my-wedding.db :as db]
            [my-wedding.utils :as utils]
            [npm]))

(def recaptcha (r/adapt-react-class (aget js/window "deps" "react-recaptcha")))

(defn onload-callback [response])

(set! js/captchaOnLoad onload-callback)


(defn- toggle-keyword [previous-value new-value]
  (let [value (keyword new-value)]
    (if (= previous-value value)
      nil
      value)))

(defn handle-change
  "Text input field on-change handler. Uses `form-keys` as path inside state's :form key."
  [& form-keys]
  (fn [event]
    (db/set-state assoc-in (into [:form] form-keys) (.. event -target -value))))


(defn handle-change-transforming
  "Text input field on-change handler. Uses `form-keys` as path inside state's :form key.
  Uses `transformer-fn` to edit the input value before saving."
  [transformer-fn & form-keys]
  (fn [event]
    (db/set-state assoc-in (into [:form] form-keys) (transformer-fn (.. event -target -value)))))


(defn handle-checkbox-change [form-key checkbox-key]
  (fn [event]
    (let [prev-value (db/get-form-state form-key checkbox-key)]
      (db/set-state assoc-in [:form form-key checkbox-key] (not prev-value)))))


(defn handle-submit-success
  "Stop sending state and add a self-clearing notification of the form submit success."
  [response]
  (let [id  (random-uuid)]
    (db/set-sending-state false)
    (if (= "success"
           (:result response))
      (db/add-notification
       :id id
       :type "success"
       :content (str "Kiitos ilmoittautumisestasi "
                     (get-in response [:data :name]) " ja "
                     "lämpimästi tervetuloa häihimme!"))
      (db/add-notification
       :id id
       :type "error"
       :content "Ilmoittautuminen epäonnistui. Yritä kohta uudelleen tai vaihtoehtoisesti ilmoittaudu puhelimitse. Löydät puhelinnumeron kutsusta."))
    (do (db/clear-notification :id id :in-seconds 20)
        (utils/scroll-to js/document.body))))


(defn handle-submit-error
  "Stop sending state and add a self-clearing notification of the form submit error."
  [_]
  (let [id (random-uuid)]
    (db/set-sending-state false)
    (db/add-notification
     :id id
     :type "error"
     :content "Ilmoittautuminen epäonnistui. Yritä kohta uudelleen tai vaihtoehtoisesti ilmoittaudu puhelimitse. Löydät puhelinnumeron kutsusta.")
    (db/clear-notification :id id :in-seconds 5)))


(defn validate-form []
  (let [form (db/get-state :form)
        form-errors (db/get-form-errors)
        errors (doall
                (for [[k form-part] form]
                  (let [errors (cond-> {}
                                 (str/blank? (:name form-part))
                                 (assoc :name "Nimi ei voi olla tyhjä"))]
                    (db/set-form-errors k errors))))]
    (if (every? empty? errors)
      form
      false)))


(defn prepare-form-data [data]
  (let [attending? (let [val (get data :attending "")]
                     (= val :attending))
        form-data (doto
                      (js/FormData.)
                    (.append "name" (utils/truncate (get data :name "") 150))
                    (.append "attending" attending?))]
    (if attending?
      (doto form-data
        (.append "allergies" (utils/truncate (get data :allergies "") 800))
        (.append "noTransportation" (get data :no-transportation "false"))
        (.append "transportationChurchVenue" (get data :transportation-church-venue "false"))
        (.append "transportationVenueCity" (get data :transportation-venue-city "false"))
        (.append "wishlist" (utils/truncate (get data :wishlist "") 300)))
      (doto form-data
        (.append "allergies" "")
        (.append "noTransportation" "")
        (.append "transportationChurchVenue" "")
        (.append "transportationVenueCity" "")
        (.append "wishlist" "")))))


(defn handle-submit
  "Prevent default behaviour of form submit and send
  a post ajax call to the google sheets script api endpoint."
  [event]
  (.preventDefault event)
  (when-let [validated-form (validate-form)]
    (when (js/confirm "Haluatko varmasti lähettää ilmoittautumislomakkeen?")
      (doall
       (for [[_ form-part] validated-form]
         (let [url       (db/get-state :api-url)
               form-data (prepare-form-data form-part)]
           (db/set-sending-state true)
           (POST url {:body            form-data
                      :response-format :json
                      :keywords?       true
                      :handler         handle-submit-success
                      :error           handle-submit-error})))))))

(defn text-input-group [{:keys [name label form-key on-change value error]}]
  (let [err (error form-key)]
    [:div.form-group
     [:label {:for name} label]
     [:input {:type      "text"
              :class     (if err
                           "form-control is-invalid"
                           "form-control")
              :id        name
              :value     (value form-key)
              :on-change (on-change form-key)}]
     [:div.invalid-feedback
      (str err)]]))

(defn textarea-group [{:keys [name label form-key on-change value]}]
  [:div.form-group
   [:label {:for name} label]
   [:textarea {:class     "form-control"
               :id        name
               :value     (value form-key)
               :on-change (on-change form-key)}]])

(defn response-form-container []
  (let [captcha-ok                    (r/atom true)
        default-form-key              0
        attending-radio-checked       #(if (= (db/get-form-state default-form-key :attending)
                                              :attending)
                                         true
                                         false)
        checkbox-checked              (fn [form-key checkbox-key]
                                        (let [prev-value (db/get-form-state form-key checkbox-key)]
                                          (if prev-value
                                            true
                                            false)))
        handle-attending-radio-change (handle-change-transforming keyword default-form-key :attending)
        handle-text-input-change      #(handle-change %1 %2)
        handle-remove-person          #(when (js/confirm "Haluatko varmasti poistaa henkilön lomakkeelta?")
                                         (db/remove-person %1))
        handle-add-person             db/add-new-person-fields
        handle-clear-form             #(when (js/confirm "Haluatko varmasti tyhjentää lomakkeen?")
                                         (db/clear-form))
        handle-captcha-load           onload-callback
        handle-captcha-expiry         #(reset! captcha-ok false)
        handle-captcha-verify         #(reset! captcha-ok true)
        get-form-error                #(db/get-form-error %1 %2)
        get-form-value                #(db/get-form-state %1 %2)]
    (fn []
      (let [disabled?  (or (db/get-state :sending)
                           (not @captcha-ok))
            sending?   (db/get-state :sending)
            attending? (= (db/get-form-state 0 :attending) :attending)
            form       (db/get-state :form)]
        [:form.form {:id          "response-form"
                     :on-submit   handle-submit
                     :no-validate true}
         [:div.form-group
            [:div.form-check.form-check-inline {:style {:margin-bottom "10px"}}
           [:input.form-check-input {:type      "radio"
                                     :name      "attending"
                                     :value     :attending
                                     :id        "attending-check"
                                     :checked   (attending-radio-checked)
                                     :on-change handle-attending-radio-change}]
           [:label.form-check-label {:for "attending-check"}
            "Kiitos kutsusta, tulen/tulemme ilomielin juhlimaan kanssanne!"]]

          [:div.form-check.form-check
           [:input.form-check-input {:type      "radio"
                                     :name      "attending"
                                     :value     :not-attending
                                     :id        "not-attending-check"
                                     :checked   (not (attending-radio-checked))
                                     :on-change handle-attending-radio-change}]
           [:label.form-check-label {:for "not-attending-check"}
            "Valitettavasti en/emme pääse osallistumaan."]]]

         (doall
          (for [[key _] form]
            (let [error     (partial get-form-error key)
                  value     (partial get-form-value key)
                  on-change (partial handle-text-input-change key)
                  suffix    (fn [s]
                              (str s "-" key))]
              ^{:key key}
              [:div.person
               [:div.fields
                [text-input-group {:name      "name"
                                   :label     "Nimi:"
                                   :form-key  :name
                                   :error     error
                                   :value     value
                                   :on-change on-change}]
                (when attending?
                  [:div
                   [textarea-group {:name      "allergies"
                                    :label     "Erityisruokavalio/allergiat:"
                                    :form-key  :allergies
                                    :error     error
                                    :value     value
                                    :on-change on-change}]
                   [:div.form-group
                    [:span "Osallistuminen yhteiskuljetukseen:"]
                    [:div.form-check
                     [:input.form-check-input {:type      "checkbox"
                                               :id        (suffix "no-transportation")
                                               :name      "no-transportation"
                                               :checked   (checkbox-checked key :no-transportation)
                                               :on-change #(do
                                                             (db/set-state assoc-in [:form key :transportation-church-venue] false)
                                                             (db/set-state assoc-in [:form key :transportation-venue-city] false)
                                                             ((handle-checkbox-change key :no-transportation)))}]
                     [:label.form-check-label {:for (suffix "no-transportation")}
                      "En osallistu"]]

                    (let [disabled? (checkbox-checked key :no-transportation)]
                      [:div
                       [:div.form-check
                        [:input.form-check-input {:type      "checkbox"
                                                  :id        (suffix "transportation-church-venue")
                                                  :name      "transportation-church-venue"
                                                  :checked   (checkbox-checked key :transportation-church-venue)
                                                  :disabled  disabled?
                                                  :on-change (handle-checkbox-change key :transportation-church-venue)}]
                        [:label.form-check-label {:for (suffix "transportation-church-venue")}
                         "Kirkolta juhlapaikalle"]]
                       [:div.form-check
                        [:input.form-check-input {:type      "checkbox"
                                                  :id        (suffix "transportation-venue-city")
                                                  :name      "transportation-venue-city"
                                                  :checked   (checkbox-checked key :transportation-venue-city)
                                                  :disabled  disabled?
                                                  :on-change (handle-checkbox-change key :transportation-venue-city)}]
                        [:label.form-check-label {:for (suffix "transportation-venue-city")}
                         "Juhlapaikalta Hyvinkään keskustaan"]]])]

                [text-input-group {:name      "wishlist"
                                   :label     "Tämä kappale saa minut taatusti tanssilattialle:"
                                   :form-key  :wishlist
                                   :error     error
                                   :value     value
                                   :on-change on-change}]

                   ])]
               (when-not (zero? key)
                 [:button.btn.btn-light.btn-sm {:type     "button"
                                                :on-click (partial handle-remove-person key)}
                  (str "Poista")])
               [:hr]])))

         (when attending?
           [:div
            [:button {:type     "button"
                      :on-click handle-add-person
                      :class    "btn btn-secondary"}
             "Lisää toinen henkilö"]
            [:hr]])

         #_[:div.form-group
          [recaptcha {:sitekey          "6Ldd01sUAAAAAIFjlGRC2PvEbG36YYu45A6DFqZ8"
                      :hl               "fi"
                      :element-ID       "captcha"
                      :render           "explicit"
                      :onload-callback  handle-captcha-load
                      :expired-callback handle-captcha-expiry
                        :verify-callback  handle-captcha-verify}]]

         [:button {:type     "submit"
                   :class    "btn btn-primary"
                   :style    {:margin-right "15px"}
                   :disabled disabled?}
          (if sending?
            "Lähetetään..."
            "Lähetä")]
         [:button {:type     "button"
                   :on-click handle-clear-form
                   :class    "btn btn-dark"}
          "Tyhjennä"]]))))


(defn response-page []
  [:div.page
   [:h2.page-heading "Ilmoittautuminen häihin"]
   [response-form-container]])
