(ns my-wedding.components.notification
  (:require [my-wedding.db :as db]))


(defn notification-container []
  (let [notifications (db/get-state :notifications)
        alert-class (fn [t]
                      (condp = t
                        "error" "alert-danger"
                        "success" "alert-success"
                        "alert-info"))]
    (when (seq notifications)
      [:div
       (doall
        (for [n notifications]
          ^{:key (:id n)}
          [:div.alert {:class (alert-class (:type n))} (:text n)]))])))
