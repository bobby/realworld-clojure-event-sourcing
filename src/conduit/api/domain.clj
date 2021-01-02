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

;;;; Specs

;; TODO; properly handle blank strings

(s/def :profile/username string?)
(s/def :profile/bio string?)
(s/def :profile/image uri?)
(s/def :profile/following boolean?)

(s/def :conduit/profile
  (s/keys :req-un [:profile/username :profile/bio :profile/image :profile/following]))

(s/def :article/slug string?)
(s/def :article/title string?)
(s/def :article/description string?)
(s/def :article/body string?)
(s/def :article/tag string?)
(s/def :article/tagList (s/coll-of :article/tag :kind vector?))

(s/def :article/createdAt
  inst?)
(s/def :article/updatedAt
  inst?)

(s/def :article/favorited
  boolean?)
(s/def :article/favoritesCount
  nat-int?)

(s/def :article/author
  :conduit/profile)

(s/def :article/create-command
  (s/keys :req-un [:article/title :article/description :article/body :article/tagList]))

(s/def :conduit/article
  (s/keys :req-un [:article/slug
                   :article/title
                   :article/description
                   :article/body
                   :article/tagList
                   :article/createdAt
                   :article/updatedAt
                   :article/favorited
                   :article/favoritesCount
                   :article/author]))

;;;; Commands

(defn create-article!
  [api article]
  (let [article-id (util/uuid)]
    {:article/id article-id
     :event      (redis/publish-event! api :article/created (assoc article :article/id article-id))}))

(defn create-article-async!
  [api article]
  (let [{:keys [article/id event]}
        (create-article! api article)]
    ;; TODO: await article with given id on read-model stream
    ;; return a core-async channel
    ;; OR poll redis for presence of article with given id
    ))

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
