(ns my-wedding.pages.response
  (:require [ajax.core :refer [POST]]
            [my-wedding.db :as db]))


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
                               sending?]}]
  [:form.form {:id "response-form"
               :on-submit on-submit}
   [:div.form-group
    [:label {:for "first-name"} "Etunimi:"]
    [:input {:type "text"
             :class "form-control"
             :id "first-name"
             :value (text-input-value :first-name)
             :on-change (on-text-input-change :first-name)}]]
   [:div.form-group
    [:label {:for "last-name"} "Sukunimi:"]
    [:input {:type "text"
             :class "form-control"
             :id "last-name"
             :value (text-input-value :last-name)
             :on-change (on-text-input-change :last-name)}]]
   [:button {:type "submit"
             :class "btn btn-primary"
             :disabled sending?}
    (if sending?
      "Lähetetään..."
      "Lähetä")]])


(defn response-form-container []
  [response-form
   :on-submit handle-submit
   :text-input-value #(db/get-form-state %)
   :on-text-input-change #(handle-change %)
   :sending? (db/get-state :sending)])


(defn response-page []
  [:div
   [:h1 "Ilmoittaudu"]
   [response-form-container]])
