(ns app.schema
  (:require [malli.core :as m]
            [malli.util :as u]))

(def registry
  (merge
   (m/default-schemas)
   (u/schemas)
   {:app/pong
    [:map
     [:pong :int]]}))