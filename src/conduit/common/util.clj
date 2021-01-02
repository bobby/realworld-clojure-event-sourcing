(ns conduit.common.util
  (:require [clojure.string :as string]
            [clj-uuid :as uuid])
  (:import [java.util UUID]))

(defn uuid
  ([]
   (uuid/v1))
  ([uuid-str]
   (cond
     (uuid? uuid-str)
     uuid-str

     (string/blank? uuid-str)
     nil

     (string? uuid-str)
     (UUID/fromString uuid-str)

     :else
     (throw (ex-info "cannot make UUID from object" {:object uuid-str}))))
  ([namespace-uuid name-uuid]
   (when-not (or (string/blank? (str namespace-uuid))
                 (string/blank? (str name-uuid)))
     (uuid/v5 (uuid namespace-uuid) (uuid name-uuid)))))
