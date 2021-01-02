(ns conduit.api.web
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [io.pedestal.log :as log]
            [reitit.ring :as ring]
            [ring.middleware.keyword-params :as keyword-params]
            [ring.middleware.params :as params]
            [ring.middleware.session :as session]
            [immutant.web :as web]
            [conduit.api.domain :as domain])
  (:import [java.io ByteArrayInputStream]))

(set! *warn-on-reflection* true)

;;;; Service Definition

;; TODO!!!
(defn dummy
  [_request]
  {:status 200})

(defn routes
  [{:keys [api health channel-socket]}]
  (let [{:keys [ajax-post-fn ajax-get-or-ws-handshake-fn]}
        channel-socket]
    ["/api"
     ["/users"
      {:get  dummy
       :post dummy
       :put  dummy}
      ["/login"
       {:post dummy}]]
     ["/profiles/:username"
      {:get dummy}
      ["/follow"
       {:post   dummy
        :delete dummy}]]
     ["/articles"
      {:get  dummy
       :post dummy}
      ["/feed"
       {:get dummy}]
      ["/:slug"
       {:get    dummy
        :put    dummy
        :delete dummy}
       ["/comments"
        {:get  dummy
         :post dummy}
        ["/:id"
         {:delete dummy}]]
       ["/favorite"
        {:post   dummy
         :delete dummy}]]]]))

;;;; Component Lifecycle

(defn init-service
  [{:keys [development?]
    :as   config}]
  (ring/ring-handler
   (ring/router (routes config))
   (ring/create-default-handler)
   {:middleware [keyword-params/wrap-keyword-params
                 params/wrap-params
                 session/wrap-session]}))

(defn init-server
  [{:keys [service development? host port]}]
  (web/run service {:host host :port port}))

(defn halt-server!
  [server]
  (when server (web/stop server)))
