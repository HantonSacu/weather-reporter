(ns app.frontend.util
  (:require [tick.alpha.api :as t]
            [tick.format :as tf]
            [tick.locale-en-us]))

(defn format-date [date]
  (when date
    (t/format (tf/formatter "MMM dd, yyyy") date)))