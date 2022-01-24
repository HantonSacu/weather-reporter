(ns app.reitit
  (:require [integrant.core :as ig]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.coercion :as rrc]
            [com.verybigthings.funicular.transit :as funicular-transit]))

(def muuntaja-instance
  (m/create
   (-> m/default-options
       (assoc-in [:formats "application/transit+json" :decoder-opts] funicular-transit/read-handlers)
       (assoc-in [:formats "application/transit+json" :encoder-opts] funicular-transit/write-handlers))))

(defmethod ig/init-key ::opts [_ _args]
  {:muuntaja muuntaja-instance
   :middleware [muuntaja/format-middleware
                rrc/coerce-exceptions-middleware
                rrc/coerce-request-middleware
                rrc/coerce-response-middleware]})
