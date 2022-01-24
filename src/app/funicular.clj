(ns app.funicular
  (:require [integrant.core :as ig]
            [clojure.spec.alpha :as s]
            [com.verybigthings.funicular.core :as f]
            [app.schema :refer [registry]]
            [duct.logger :refer [log]]))

(s/check-asserts true)

(defprotocol IFunucilarApi
  (execute [this request] [this request request-context])
  (inspect [this]))

(defmethod ig/init-key :app/funicular [_ {:keys [context logger] :as api}]
  (let [compiled (f/compile api {:malli/registry registry})]
    (reify IFunucilarApi
      (execute [this request]
        (execute this request nil))
      (execute [_ request request-context]
        (log logger :info :funicular/request request)
        (f/execute compiled (merge context request-context) request))
      (inspect [_]
        (f/inspect compiled)))))