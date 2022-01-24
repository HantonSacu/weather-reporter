(ns app.frontend.app
  (:require [keechma.next.controllers.router]
            [keechma.next.controllers.subscription]
            [keechma.next.controllers.entitydb]
            [app.frontend.controllers.ping]
            [com.verybigthings.funicular.controller :as f]
            ["react-dom" :as rdom]))

(def app
  (-> {:keechma.subscriptions/batcher rdom/unstable_batchedUpdates,
       :keechma/controllers
       {:router #:keechma.controller{:params true,
                                     :type :keechma/router,
                                     :keechma/routes
                                     [["" {:page "home"}] ":page"]}
        :entitydb #:keechma.controller{:params true,
                                       :type :keechma/entitydb,
                                       :keechma.entitydb/schema {}}
        :ping #:keechma.controller{:params true}}}
    f/install))