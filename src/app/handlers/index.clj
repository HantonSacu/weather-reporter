(ns app.handlers.index
  (:require [integrant.core :as ig]
            [hiccup.page :refer [html5 include-js]]))

(defn ok-html [body]
  {:status 200
   :headers {"Content-type" "text/html"}
   :body body})

(defmethod ig/init-key :app.handlers/index [_ _]
  (-> (html5
        [:head
         [:title "App"]
         [:meta {:charset "UTF-8"}]
         [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
         [:link {:href "/css/style.css" :rel "stylesheet" :type "text/css"}]]
        [:body
         [:noscript "You need to enable JavaScript to run this app."]
         [:div {:id "app"}]
         (include-js "/js/main.js")])
    ok-html
    constantly))
