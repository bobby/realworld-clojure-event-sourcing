(ns conduit.common.redis
  (:require [clojure.core.async :as async]
            [io.pedestal.log :as log]
            [taoensso.carmine :as car :refer (wcar)]
            [conduit.common.util :as util]))

;; TODO: change to CloudEvents spec?
(defn publish-event!
  ([component action data]
   (publish-event! component (util/uuid) action data))
  ([component event-id action data]
   (publish-event! component event-id action data nil))
  ([{:keys [stream redis] :as component} event-id action data parent]
   (log/info ::publish-event! [stream event-id action data parent])
   (let [event  {:event/id        event-id
                 :event/parent    parent
                 :event/action    action
                 :event/data      data
                 :event/timestamp (java.util.Date.)}
         offset (wcar redis (car/xadd stream "*" event-id event))]
     (assoc event :redis/offset offset))))

(defn- extract-stream-records
  [records]
  (map (fn [[offset [stream record]]]
         (assoc record
                :redis/offset offset
                :redis/stream stream))
       records))

(defn next-batch-from-stream-starting-from-id
  [redis {:keys [stream timeout batch-size]} from-id]
  (let [[[_ records]]
        (wcar redis (car/xread "BLOCK" (or timeout 0)
                               "COUNT" (or batch-size 1)
                               "STREAMS" stream
                               from-id))]
    (extract-stream-records records)))

(defn init-redis-stream-channel
  ([stream-config]
   (init-redis-stream-channel stream-config nil))
  ([stream-config start-id]
   (init-redis-stream-channel stream-config start-id (async/chan 1)))
  ([{:keys [stream batch-size timeout redis] :as component} start-id channel]
   (let [semaphore (atom true)]
     (async/thread
       (loop [from-id (or start-id "$")]
         (if @semaphore
           (let [events (next-batch-from-stream-starting-from-id redis
                                                                 {:stream     stream
                                                                  :batch-size batch-size
                                                                  :timeout    timeout}
                                                                 from-id)]
             (doseq [event events]
               (async/>!! channel event))
             (recur (-> events last :redis/offset)))
           :done)))
     {:channel   channel
      :semaphore semaphore
      :component component})))

(defn stop-redis-stream-channel
  [{:keys [channel semaphore]}]
  (when semaphore (reset! semaphore false))
  (when channel (async/close! channel)))
