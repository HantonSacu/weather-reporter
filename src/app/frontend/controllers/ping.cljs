(ns app.frontend.controllers.ping
  (:require [keechma.next.controller :as ctrl]
            [keechma.next.controllers.pipelines :as pipelines]
            [keechma.pipelines.core :as pp :refer-macros [pipeline!]]
            [com.verybigthings.funicular.controller :refer [command!]]))

(derive :ping ::pipelines/controller)

(def pipelines
  {:ping
   (pipeline! [value {:keys [state*], :as ctrl}]
     (command! ctrl :api/ping {})
     (println "---" value)
     (pp/swap! state* conj value))})

(defmethod ctrl/start :ping [_ _ _]
  [])

(defmethod ctrl/prep :ping [ctrl] (pipelines/register ctrl pipelines))