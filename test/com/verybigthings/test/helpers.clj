(ns com.verybigthings.test.helpers
  (:require [app.funicular :as f]))

(defn get-command [{[_ command-payload] :command}]
  command-payload)

(defn get-query [response query-alias]
  (let [[_ query-payload] (get-in response [:queries query-alias])]
    query-payload))

(defn command! [funicular command payload]
  (-> (f/execute funicular {:command [command payload]})
      get-command))

(defn query! [funicular query query-alias payload]
  (let [res (f/execute funicular {:queries {query-alias [query payload]}})]
    (get-query res query-alias)))
