(ns ^:no-doc user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh set-refresh-dirs]]
            [clojure.java.io :as io]
            [clojure.spec.test.alpha :as stest]
            [meta-merge.core :refer [meta-merge]]
            [integrant.core :as ig]
            [integrant.repl :refer [clear go halt prep init reset reset-all]]
            [eftest.runner :as eftest]
            [clojure.java.browse :refer [browse-url]]
            [clojure.walk :as walk]
            [conduit.api.config :as config]))

(set! *warn-on-reflection* true)

(def dev-config
  {})

(ns-unmap *ns* 'test)

(defn test []
  (eftest/run-tests (eftest/find-tests "test") {:multithread? false}))

(defn instrument
  []
  (stest/instrument))

(defn unstrument
  []
  (stest/unstrument))

(defn dev
  []
  (require 'conduit.api.config)
  (integrant.repl/set-prep! #(-> ((resolve 'conduit.api.config/config))
                                 (meta-merge dev-config))))
