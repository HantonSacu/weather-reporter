(ns com.verybigthings.test.fixtures
  (:require [duct.core :as duct]
            [app.readers :refer [readers]]
            [integrant.core :as ig]
            [next.jdbc :as nj])
  (:import io.zonky.test.db.postgres.embedded.EmbeddedPostgres))

(def state* (atom nil))

(defn get-system []
  (-> state* deref :system))

(def db-config
  {:port 54321
   :db-name "postgres"
   :user "postgres"
   :password "postgres"})

(defn make-jdbc-url [{:keys [port db-name user password]}]
  (str "jdbc:postgresql://localhost:" port "/" db-name "?user=" user "&password=" password))

(defn read-config []
  (let [test-config (-> (duct/resource "resources/app/test.edn")
                        (duct/read-config readers))]
    (-> (duct/resource "app/config.edn")
        (duct/read-config readers)
        (assoc :duct.profile/test test-config))))

(defn apply-mocks [system mock]
  (reduce-kv
    (fn [system' key mocked-key]
      (let [key-val (get system' key)]
        (-> system'
            (dissoc key)
            (assoc mocked-key key-val))))
    system
    mock))

(defn init-system! [init mock]
  (duct/load-hierarchy)
  (let [config (read-config)
        prepped-config (-> (duct/prep-config config [:duct.profile/test :duct.profile/prod])
                           (dissoc :duct.database.sql/hikaricp)
                           (assoc-in [:duct.database/sql :datasource] (-> db-config make-jdbc-url nj/get-datasource))
                           (apply-mocks mock))]
    (if (seq init)
      (ig/init prepped-config init)
      (ig/init prepped-config))))

(defn halt-system! [system]
  (ig/halt! system))

(defn start-pg! []
  (-> (EmbeddedPostgres/builder)
      (.setServerConfig "fsync" "off")
      (.setServerConfig "full_page_writes" "off")
      (.setPort (:port db-config))
      (.start)))

(defn stop-pg! [pg]
  (.close pg))

(defn create-db-snapshot! []
  (let [db-name (:db-name db-config)
        db-snapshot-name (str db-name "_snapshot")
        jdbc-url (-> db-config (dissoc :db-name) make-jdbc-url)
        connection (nj/get-connection jdbc-url)]
    (nj/execute! connection ["SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE datname = ? AND pid <> pg_backend_pid()" db-name])
    (nj/execute! connection [(str "CREATE DATABASE " db-snapshot-name " TEMPLATE " db-name)])))

(defn restore-db-snapshot! []
  (let [jdbc-url (make-jdbc-url (assoc db-config :db-name "template1"))
        connection (nj/get-connection jdbc-url)
        db-name (:db-name db-config)
        db-snapshot-name (str db-name "_snapshot")]
    (nj/execute! connection [(str "DROP DATABASE " db-name " WITH (FORCE)")])
    (nj/execute! connection [(str "CREATE DATABASE " db-name " TEMPLATE " db-snapshot-name)])))

(defn with-system!
  ([test-fn] (with-system! nil test-fn))
  ([{:keys [init mock]} test-fn]
   (try
     (let [pg (start-pg!)
           migrate-system (init-system! [:duct/migrator] nil)]
       (halt-system! migrate-system)
       (create-db-snapshot!)
       (reset! state* {:pg pg :system (init-system! init mock)})
       (test-fn))
     (catch Exception e
       (do
         (.printStackTrace e)
         (throw e)))
     (finally
       (when-let [state @state*]
         (halt-system! (:system state))
         (stop-pg! (:pg state))
         (reset! state* nil))))))

(defn with-reset-db! [test-fn]
  (try
    (test-fn)
    (catch Exception e
      (do
        (.printStackTrace e)
        (throw e)))
    (finally
      (restore-db-snapshot!))))

(defn with-before! [after-fns test-fn]
  (let [system (get-system)]
    (doseq [afn after-fns]
      (afn system)))
  (test-fn))

(defn with-after! [after-fns test-fn]
  (test-fn)
  (let [system (get-system)]
    (doseq [afn after-fns]
      (afn system))))
