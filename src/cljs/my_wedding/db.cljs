(ns my-wedding.db
  (:require [reagent.core :as r]))


(defonce app-state
  (r/atom {:api-url "https://script.google.com/macros/s/AKfycbyLCwTTW-TQcULzFl2CgIpyegFK5KyNHWPlMNPeNgzS5EoRkc0V/exec"
         :page :home
         :form {:first-name ""
                :last-name ""}
         :notifications []
         :sending false}))


(defn get-state
  "Return app state of any depth using `keys` as path."
  [& keys]
  (get-in @app-state keys))


(def form-cursor (r/cursor app-state [:form]))


(defn get-form-state
  "Return form state from any depth with `keys`."
  [& keys]
  (get-in @form-cursor keys))


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
