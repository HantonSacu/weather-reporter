(ns app.frontend.ui.main
  (:require [keechma.next.helix.core :refer [with-keechma use-sub]]
            [keechma.next.helix.lib :refer [defnc]]
            [helix.dom :as d]
            [helix.core :as hx :refer [$]]
            [app.frontend.ui.pages.home :refer [Home]]))

(defnc MainRenderer [props]
  (let [{:keys [page]} (use-sub props :router)]
    (d/div
      (case page
        "home" ($ Home)
        (d/div "404")))))

(def Main (with-keechma MainRenderer))
