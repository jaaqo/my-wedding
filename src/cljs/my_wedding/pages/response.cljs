(ns my-wedding.pages.response
  (:require [ajax.core :refer [POST]]
            [reagent.core :as r]
            [my-wedding.db :as db]
            [react-recaptcha :as Recaptcha]))

(defn onload-callback [response])

(set! js/captchaOnLoad onload-callback)

(defn handle-change
  "Text input field on-change handler. Uses `form-keys` as path inside state's :form key."
  [& form-keys]
  (fn [event]
    (db/set-state assoc-in (into [:form] form-keys) (.. event -target -value))))


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
       :content "Kiitos ilmottautumisestasi!")
      (db/add-notification
       :id id
       :type "error"
       :content "Ilmoittautuminen epäonnistui. Yritä kohta uudelleen tai vaihtoehtoisesti ilmoittaudu puhelimitse. Löydät puhelinnumeron kutsusta."))
    (db/clear-notification :id id :in-seconds 5)))


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


(defn handle-submit
  "Prevent default behaviour of form submit and send
  a post ajax call to the google sheets script api endpoint."
  [event]
  (.preventDefault event)
  (let [url (db/get-state :api-url)
        form-data (doto
                      (js/FormData.)
                    (.append "firstName" (db/get-form-state :first-name))
                    (.append "lastName" (db/get-form-state :last-name)))]
    (db/set-sending-state true)
    (POST url {:body form-data
               :response-format :json
               :keywords? true
               :handler handle-submit-success
               :error handle-submit-error})))



(defn response-form [& {:keys [on-submit
                               text-input-value
                               on-text-input-change
                               on-captcha-verify
                               on-captcha-load
                               disabled?]}]
  [:form.form {:id        "response-form"
               :on-submit on-submit}
   [:div.form-group
    [:label {:for "first-name"} "Etunimi:"]
    [:input {:type      "text"
             :class     "form-control"
             :id        "first-name"
             :value     (text-input-value :first-name)
             :on-change (on-text-input-change :first-name)}]]
   [:div.form-group
    [:label {:for "last-name"} "Sukunimi:"]
    [:input {:type      "text"
             :class     "form-control"
             :id        "last-name"
             :value     (text-input-value :last-name)
             :on-change (on-text-input-change :last-name)}]]
   [:div.form-group
    [:> Recaptcha {:sitekey         "6Ldd01sUAAAAAIFjlGRC2PvEbG36YYu45A6DFqZ8"
                   :hl              "fi"
                   :element-ID      "captcha"
                   :render          "explicit"
                   :onload-callback on-captcha-load
                   :verify-callback on-captcha-verify}]]
   [:button {:type     "submit"
             :class    "btn btn-primary"
             :disabled disabled?}
    (if (db/get-state :sending)
      "Lähetetään..."
      "Lähetä")]])


(defn response-form-container []
  (let [captcha-ok (r/atom false)]
    (fn []
      [response-form
       :on-submit handle-submit
       :text-input-value #(db/get-form-state %)
       :on-text-input-change #(handle-change %)
       :disabled? (or (db/get-state :sending)
                      (not @captcha-ok))
       :on-captcha-load onload-callback
       :on-captcha-verify #(reset! captcha-ok true)])))


(defn response-page []
  [:div
   [:h1 "Ilmoittaudu"]
   [response-form-container]])
