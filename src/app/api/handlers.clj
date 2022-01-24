(ns app.api.handlers
  (:require [integrant.core :as ig]))

(defmethod ig/init-key ::pong [_ _]
  (fn [{:keys [data penkala]}]
    {:pong (System/currentTimeMillis)}))