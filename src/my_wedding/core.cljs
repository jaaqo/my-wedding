(ns my-wedding.core
  (:require [reagent.core :as r :refer [atom]]
            [ajax.core :refer [POST]]
            [cljsjs.reactstrap]))


(enable-console-print!)


(defonce app-state
  (atom {:api-url "https://script.google.com/macros/s/AKfycbyLCwTTW-TQcULzFl2CgIpyegFK5KyNHWPlMNPeNgzS5EoRkc0V/exec"
         :form {:first-name ""
                :last-name ""}
         :notifications []
         :sending false}))


(defn handle-change
  "Text input field on-change handler. Uses `form-keys` as path inside state's :form key."
  [& form-keys]
  (fn [event]
    (swap! app-state assoc-in (into [:form] form-keys) (.. event -target -value))))


(defn set-sending-state
  "Set global sending state to `state`."
  [state]
  (swap! app-state assoc :sending state))


(defn add-notification
  "Add notification to notifications list."
  [& {:keys [id type content]}]
  (swap! app-state update :notifications conj
         {:id id
          :type type
          :text content}))


(defn clear-notification
  "Removes notifications by `id` when `in-seconds` time has passed."
  [& {:keys [id in-seconds]}]
  (let [ids-match (fn [n] (= (:id n) id))
        remove-matching-id (fn [ns]
                             (vec (remove ids-match ns)))]
    (js/setTimeout #(swap! app-state update :notifications remove-matching-id)
                   (* in-seconds 1000))))


(defn handle-submit-success
  "Stop sending state and add a self-clearing notification of the form submit success."
  [response]
  (let [id  (random-uuid)]
    (set-sending-state false)
    (if (= "success"
           (:result response))
      (add-notification
       :id id
       :type "success"
       :content "Kiitos ilmottautumisestasi!")
      (add-notification
       :id id
       :type "error"
       :content "Ilmoittautuminen epäonnistui. Yritä kohta uudelleen tai vaihtoehtoisesti ilmoittaudu puhelimitse. Löydät puhelinnumeron kutsusta."))
    (clear-notification :id id :in-seconds 5)))


(defn handle-submit-error
  "Stop sending state and add a self-clearing notification of the form submit error."
  [_]
  (let [id (random-uuid)]
    (set-sending-state false)
    (add-notification
     :id id
     :type "error"
     :content "Ilmoittautuminen epäonnistui. Yritä kohta uudelleen tai vaihtoehtoisesti ilmoittaudu puhelimitse. Löydät puhelinnumeron kutsusta.")
    (clear-notification :id id :in-seconds 5)))


(defn get-state
  "Return app state of any depth using `keys` as path."
  [& keys]
  (get-in @app-state keys))


(def form-cursor (r/cursor app-state [:form]))


(defn get-form-state
  "Return form state from any depth with `keys`."
  [& keys]
  (get-in @form-cursor keys))


(defn handle-submit
  "Prevent default behaviour of form submit and send
  a post ajax call to the google sheets script api endpoint."
  [event]
  (.preventDefault event)
  (let [url (get-state :api-url)
        form-data (doto
                      (js/FormData.)
                    (.append "firstName" (get-form-state :first-name))
                    (.append "lastName" (get-form-state :last-name)))]
    (set-sending-state true)
    (POST url {:body form-data
               :response-format :json
               :keywords? true
               :handler handle-submit-success
               :error handle-submit-error})))


(defn alert-container []
  (let [notifications (:notifications @app-state)
        alert-class (fn [t]
                      (condp = t
                        "error" "alert-danger"
                        "success" "alert-success"
                        "alert-info"))]
    (when (seq (:notifications @app-state))
      [:div
       (doall
        (for [n notifications]
          ^{:key (:id n)}
          [:div.alert {:class (alert-class (:type n))} (:text n)]))])))


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
    [:label {:for "first-name"} "Sukunimi:"]
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
  [:div
   [:h1 "Ilmoittaudu"]
   [response-form
    :on-submit handle-submit
    :text-input-value #(get-form-state %)
    :on-text-input-change #(handle-change %)
    :sending? (get-state :sending)]])


(defn root []
  [:div.container
   [:div.row
    [:div.col
     [alert-container]]]
   [:div.row
    [:div.col
     [response-form-container]]]])


(r/render-component [root]
                    (js/document.getElementById "app"))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
