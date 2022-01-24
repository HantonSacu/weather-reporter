(ns app.readers)

(defn resolve-var [sym]
  (let [resolved (clojure.core/requiring-resolve sym)]
    (if resolved
      (var-get resolved)
      (throw (ex-info (str sym " can't be resolved") {:var sym})))))

(def readers
  {'resolve resolve-var})