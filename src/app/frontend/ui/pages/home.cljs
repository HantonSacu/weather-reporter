(ns app.frontend.ui.pages.home
  (:require [keechma.next.helix.core :refer [with-keechma use-sub dispatch]]
            [keechma.next.helix.lib :refer [defnc]]
            [helix.dom :as d]))

(defnc HomeRenderer [props]
  (let [pongs (use-sub props :ping)]
    (d/div
      (d/button {:onClick #(dispatch props :ping :ping)} "Ping")
      (d/hr)
      (map-indexed
        (fn [i p]
          (d/div {:key i} (pr-str p)))
        pongs))))

(def Home (with-keechma HomeRenderer))
