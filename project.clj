(let [cljfmt (->> "cljfmt.edn" slurp read-string)]
  (defproject app "0.1.0-SNAPSHOT"
    :description "FIXME: write description"
    :url "http://example.com/FIXME"
    :min-lein-version "2.0.0"
    :plugins [[duct/lein-duct "0.12.1"]
              [lein-tools-deps "0.4.5"]
              [lein-cljfmt "0.7.0"]]
    :main ^:skip-aot app.main
    :uberjar-name  "app-standalone.jar"
    :resource-paths ["resources" "target/resources"]
    :prep-tasks     ["javac" "compile" ["run" ":duct/compiler"]]
    :middleware     [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn
                     lein-duct.plugin/middleware]
    :lein-tools-deps/config {:config-files [:install :user :project]}
    :profiles
    {:uberjar {:aot :all}}
    :cljfmt ~cljfmt))
