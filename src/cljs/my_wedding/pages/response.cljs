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
       :content (str "Henkilön "
                     (get-in response [:data :firstName]) " "
                     (get-in response [:data :lastName])
                     " ilmoittautuminen onnistui!"))
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
                                 (str/blank? (:first-name form-part))
                                 (assoc :first-name "Etunimi ei voi olla tyhjä")
                                 (str/blank? (:last-name form-part))
                                 (assoc :last-name "Sukunimi ei voi olla tyhjä"))]
                    (db/set-form-errors k errors))))]
    (if (every? empty? errors)
      form
      false)))


(defn handle-submit
  "Prevent default behaviour of form submit and send
  a post ajax call to the google sheets script api endpoint."
  [event]
  (.preventDefault event)
  (when-let [validated-form (validate-form)]
    (when (js/confirm "Haluatko varmasti lähettää ilmoittautumislomakkeen?")
      (doall
       (for [[_ form-part] validated-form]
         (let [url (db/get-state :api-url)
               form-data (doto
                             (js/FormData.)
                           (.append "firstName" (get form-part :first-name ""))
                           (.append "lastName" (get form-part :last-name ""))
                           (.append "allergies" (get form-part :allergies "")))]
           (db/set-sending-state true)
           (POST url {:body form-data
                      :response-format :json
                      :keywords? true
                      :handler handle-submit-success
                      :error handle-submit-error})))))))

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


(defn response-form-fields [{:keys [key error value on-change]}]
  [:div.fields
   [text-input-group {:name "first-name"
                      :label "Etunimi:"
                      :form-key :first-name
                      :error error
                      :value value
                      :on-change on-change}]
   [text-input-group {:name "last-name"
                      :label "Sukunimi:"
                      :form-key :last-name
                      :error error
                      :value value
                      :on-change on-change}]
   [textarea-group {:name "allergies"
                    :label "Allergiat:"
                    :form-key :allergies
                    :error error
                    :value value
                    :on-change on-change}]])


(defn response-form [& {:keys [on-submit
                               form
                               error
                               text-input-value
                               on-text-input-change
                               on-add-another
                               on-clear
                               on-captcha-verify
                               on-captcha-expiry
                               on-captcha-load
                               sending?
                               disabled?]}]
  [:form.form {:id        "response-form"
               :on-submit on-submit
               :no-validate true}
   (doall
    (for [[k _] form]
      ^{:key k}
      [:div.person
       [response-form-fields {:value (partial text-input-value k)
                              :error (partial error k)
                              :on-change (partial on-text-input-change k)}]
       [:hr]]))
   [:button {:type     "button"
             :on-click on-add-another
             :class    "btn btn-secondary"}
    "Lisää henkilö"]
   [:hr]
   [:div.form-group
    [recaptcha {:sitekey         "6Ldd01sUAAAAAIFjlGRC2PvEbG36YYu45A6DFqZ8"
                :hl              "fi"
                :element-ID      "captcha"
                :render          "explicit"
                :onload-callback on-captcha-load
                :expired-callback on-captcha-expiry
                :verify-callback on-captcha-verify}]]
   [:button {:type     "submit"
             :class    "btn btn-primary"
             :style {:margin-right "15px"}
             :disabled disabled?}
    (if sending?
      "Lähetetään..."
      "Lähetä")]
   [:button {:type     "button"
             :on-click on-clear
             :class    "btn btn-dark"}
      "Tyhjennä"]])


(defn response-form-container []
  (let [captcha-ok (r/atom true)]
    (fn []
      [response-form
       :form (db/get-state :form)
       :on-submit handle-submit
       :error #(db/get-form-error %1 %2)
       :text-input-value #(db/get-form-state %1 %2)
       :on-text-input-change #(handle-change %1 %2)
       :on-add-another db/add-new-person-fields
       :on-clear  #(when (js/confirm "Haluatko varmasti tyhjentää lomakkeen?")
                     (db/clear-form))
       :disabled? (or (db/get-state :sending)
                      (not @captcha-ok))
       :sending? (db/get-state :sending)
       :on-captcha-load onload-callback
       :on-captcha-expiry #(reset! captcha-ok false)
       :on-captcha-verify #(reset! captcha-ok true)])))


(defn response-page []
  [:div
   [:h1 "Ilmoittaudu"]
   [response-form-container]])
