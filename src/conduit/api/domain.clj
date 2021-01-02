(ns conduit.api.domain
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [clojure.core.async :as async]
            [io.pedestal.log :as log]
            [tilakone.core :as tk]
            [taoensso.carmine :as car :refer (wcar)]
            [conduit.common.redis :as redis]
            [conduit.common.util :as util]))

(set! *warn-on-reflection* true)

;;;; Component Lifecycle

(defn init-api
  [config]
  config)

(defn init-event-processor
  [{:keys [development? kafka-config application-id]
    :as   event-processor}]
  event-processor)

(defn init-health
  [config]
  config)
