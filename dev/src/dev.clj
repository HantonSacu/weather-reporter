(ns dev
  (:refer-clojure :exclude [test])
  (:require [clojure.repl :refer :all]
            [fipp.edn :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [duct.core :as duct]
            [duct.core.repl :as duct-repl]
            [eftest.runner :as eftest]
            [integrant.core :as ig]
            [integrant.repl :refer [clear halt go init prep reset]]
            [integrant.repl.state :refer [config system]]
            [duct.repl.shadow-cljs :refer [cljs-repl]]
            [app.readers :refer [readers]]
            [shadow.cljs.devtools.api :as shadow])
  (:import org.apache.commons.io.output.WriterOutputStream
           java.io.PrintStream))

(duct/load-hierarchy)

(defn read-config []
  (duct/read-config (io/resource "app/config.edn") readers))

(defn test []
  (eftest/run-tests (eftest/find-tests "test")))

(def profiles
  [:duct.profile/dev :duct.profile/local])

(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src")

(when (io/resource "local.clj")
  (load "local"))

(integrant.repl/set-prep! #(duct/prep-config (read-config) profiles))

(defn redirect-nrepl-output []
  (System/setOut (PrintStream. (WriterOutputStream. *out*)
                   true))
  (alter-var-root #'*out* (fn [_] *out*)))

(defn generate-migration [project-ns migration-name]
  (let [ts (System/currentTimeMillis)
        migration-name' (name migration-name)
        base (str ts "_" migration-name')
        up (str base ".up.sql")
        down (str base ".down.sql")
        user-dir (System/getProperty "user.dir")
        migrations-dir (str user-dir "/resources/" (name project-ns) "/migrations/")
        existing-migrations (-> migrations-dir io/file file-seq)
        existing-migration-names (reduce
                                   (fn [acc f]
                                     (let [f-name (.getName f)
                                           [_ migration-name _] (re-find #"\d+_(.+)\.(up|down)\.sql" f-name)]
                                       (conj acc migration-name)))
                                   #{}
                                   existing-migrations)]
    (when (contains? existing-migration-names migration-name')
      (throw (ex-info (str "Migration " migration-name " already exists") {:error :migration-exists
                                                                           :migration migration-name})))
    (doseq [f [up down]]
      (let [full-path (str migrations-dir f)]
        (io/make-parents full-path)
        (spit full-path "")))))

(comment
  (redirect-nrepl-output)
  (refresh)
  (go)
  (halt)
  (reset)
  (cljs-repl)

  (test)

  (shadow/repl :app))
