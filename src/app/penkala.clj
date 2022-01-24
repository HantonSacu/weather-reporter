(ns app.penkala
  (:require [com.verybigthings.penkala.next-jdbc :as penkala-next-jdbc :refer [get-env]]
            [com.verybigthings.penkala.env :refer [with-db]]
            [integrant.core :as ig]
            [com.verybigthings.funicular.anomalies :as anom]
            [com.verybigthings.pgerrors.core :as pgerrors])
  (:import (org.postgresql.util PSQLException)))

(defrecord Boundary [env])

(defn assoc-transaction [boundary t]
  (update boundary :env with-db t))

(defn humanize [error-formatters error-data data]
  (let [constraint (:postgresql/constraint error-data)
        formatter (get error-formatters constraint)]
    (cond
      (fn? formatter) (formatter error-data data)
      formatter formatter
      :else (:postgresql.error/message error-data))))

(defn wrap-exception-handler [afn]
  (fn [& args]
    (try
      (apply afn args)
      (catch PSQLException e
        (let [[{error-formatters ::error-formatters} & _] args
              error-data (pgerrors/extract-data e)
              humanized-message (humanize error-formatters error-data {})
              anomaly (anom/incorrect humanized-message error-data)]
          (throw (anom/->ex-info anomaly e)))))))

(def insert! (wrap-exception-handler penkala-next-jdbc/insert!))
(def update! (wrap-exception-handler penkala-next-jdbc/update!))
(def delete! (wrap-exception-handler penkala-next-jdbc/delete!))
(def select! (wrap-exception-handler penkala-next-jdbc/select!))
(def select-one! (wrap-exception-handler penkala-next-jdbc/select-one!))

(defmethod ig/init-key :app/penkala [_ {:keys [db error-formatters]}]
  (let [ds (get-in db [:spec :datasource])
        env (assoc (get-env ds) ::error-formatters error-formatters)]
    (->Boundary env)))
