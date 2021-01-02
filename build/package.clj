(ns package
  (:require [badigeon.bundle :refer [bundle make-out-path]]
            [badigeon.compile :as c]))

(defn -main [& args]
  (prn ::-main args)
  (c/compile 'conduit.api)
  (bundle "target" {:paths                ["src" "target/classes"]
                    :allow-unstable-deps? true}))
