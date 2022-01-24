(ns app.integration.dummy-test
  (:require [clojure.test :refer [use-fixtures]]
            [com.verybigthings.test.fixtures :refer [with-system! with-reset-db! get-system]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.cljtest :refer [defflow]]))

(use-fixtures :once (partial with-system! {:init [:app/funicular]}))

(use-fixtures :each with-reset-db!)

(defn init []
  (let [system (get-system)]
    {:system system}))

(defflow dummy-test
  {:init init}
  (match? 1 1))
