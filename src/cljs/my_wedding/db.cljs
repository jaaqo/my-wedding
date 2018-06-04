(ns my-wedding.db
  (:require [reagent.core :as r]))


(defonce app-state
  (r/atom {:api-url "https://script.google.com/macros/s/AKfycbyLCwTTW-TQcULzFl2CgIpyegFK5KyNHWPlMNPeNgzS5EoRkc0V/exec"
           :page :home
           :form {0 {}}
           :form-errors {}
           :notifications []
           :sending false}))


(defn get-state
  "Return app state of any depth using `keys` as path."
  [& keys]
  (get-in @app-state keys))


(def form-cursor (r/cursor app-state [:form]))
(def form-errors-cursor (r/cursor app-state [:form-errors]))


(defn get-form-state
  "Return form state from any depth with `keys`."
  [& keys]
  (get-in @form-cursor keys))

(defn get-form-errors
  "Return errors."
  []
  @form-errors-cursor)

(defn get-form-error
  "Return form errors from any depth with `keys`."
  [& keys]
  (get-in @form-errors-cursor keys))

(defn set-form-errors
  "Reset form errors to `errors`."
  [key errors]
  (swap! form-errors-cursor assoc key errors)
  errors)

(defn get-current-page
  "Returns current page key from `app-state`"
  []
  (get-state :page))


(defn set-state
  "Set `app-state` using `swap!` with same signature."
  [fn keys & rst]
  (apply swap! app-state fn keys rst))


(defn set-sending-state
  "Set global sending state to `state`."
  [state]
  (set-state assoc :sending state))

(defn add-notification
  "Add notification to notifications list."
  [& {:keys [id type content]}]
  (set-state update :notifications conj
         {:id id
          :type type
          :text content}))


(defn clear-notification
  "Removes notifications by `id` when `in-seconds` time has passed."
  [& {:keys [id in-seconds]}]
  (let [ids-match (fn [n] (= (:id n) id))
        remove-matching-id (fn [ns]
                             (vec (remove ids-match ns)))]
    (js/setTimeout #(set-state update :notifications remove-matching-id)
                   (* in-seconds 1000))))


(defn add-new-person-fields []
  (let [new-key (-> @form-cursor keys last inc)]
    (set-state assoc-in [:form new-key] {})))

(defn clear-form []
  (set-state assoc :form {0 {}})
  (set-state assoc :form-errors {}))
