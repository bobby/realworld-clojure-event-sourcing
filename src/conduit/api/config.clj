(ns conduit.api.config
  (:require [integrant.core :as ig]
            [com.walmartlabs.dyn-edn :as dyn-edn]
            [clojure.java.io :as io]
            [io.pedestal.log :as log]
            [conduit.api.domain :as domain]
            [conduit.api.web :as web]))

(set! *warn-on-reflection* true)

(defn config
  []
  (->> "config.edn"
       io/resource
       slurp
       (ig/read-string {:readers (dyn-edn/env-readers)})))

(defmethod ig/init-key :env/env
  [_ env]
  env)

(defmethod ig/init-key :env/development?
  [_ env]
  (= env :dev))

;; API Config

(defmethod ig/init-key :api/api
  [_ config]
  (log/info ::ig/init-key :api/api)
  (domain/init-api config))

(defmethod ig/init-key :api/event-processor
  [_ config]
  (log/info ::ig/init-key :api/event-processor)
  (domain/init-event-processor config))

(defmethod ig/halt-key! :api/event-processor
  [_ kafka-streams]
  (log/info ::ig/halt-key! :api/event-processor)
  )

(defmethod ig/init-key :redis/client
  [_ config]
  (log/info ::ig/init-key :api/event-processor)
  config)

(defmethod ig/init-key :redis/streams
  [_ config]
  (log/info ::ig/init-key :api/event-processor)
  config)

;; TODO: make `redis-stream-channel`s

;; Web Config

(defmethod ig/init-key :web/service
  [_ config]
  (log/info ::ig/init-key :web/service)
  (web/init-service config))

(defmethod ig/init-key :web/server
  [_ config]
  (log/info ::ig/init-key :web/server)
  (web/init-server config))

(defmethod ig/halt-key! :web/server
  [_ server]
  (log/info ::ig/halt-key! :web/server)
  (web/halt-server! server))
