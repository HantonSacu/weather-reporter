(ns app.handlers.funicular
  (:require [integrant.core :as ig]
            [app.funicular :as funicular]))

(defmethod ig/init-key :app.handlers/funicular [_ {:keys [logger db funicular]}]
  {:post (fn [{:keys [body-params]}]
           (let [res (funicular/execute funicular body-params {})]
             {:status 200
              :body res}))})
