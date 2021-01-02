(ns conduit.api.web
  (:require [clojure.core.async :as a]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [io.pedestal.log :as log]
            [reitit.http :as http]
            [reitit.ring :as ring]
            [reitit.http.coercion :as coercion]
            [reitit.http.spec :as spec]
            [reitit.coercion.spec]
            [reitit.interceptor.sieppari :as sieppari]
            [reitit.http.interceptors.parameters :as parameters]
            [reitit.http.interceptors.muuntaja :as muuntaja]
            [reitit.http.interceptors.exception :as exception]
            [muuntaja.core :as m]
            [immutant.web :as web]
            [conduit.api.domain :as domain])
  (:import [java.io ByteArrayInputStream]))

(set! *warn-on-reflection* true)

;;;; Service Definition

(defn create-article
  [api {{{:keys [article]} :body} :parameters}]
  {:status 201
   :body   (domain/create-article-async! api article)})

;; TODO!!!
(defn dummy
  [_request]
  {:status 200})

(defn routes
  [{:keys [api]}]
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
     :post {:parameters {:body :article/create-command}
            :responses  {200 {:body {:article :conduit/article}}}
            :handler    (partial create-article api)}}
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
       :delete dummy}]]]])

;;;; Component Lifecycle

(defn init-service
  [{:keys [development?]
    :as   config}]
  (http/ring-handler
   (http/router
    (routes config)
    {:validate spec/validate
     :data     {:coercion     reitit.coercion.spec/coercion
                :muuntaja     m/instance
                :interceptors [;; query-params & form-params
                               (parameters/parameters-interceptor)
                               ;; content-negotiation
                               (muuntaja/format-negotiate-interceptor)
                               ;; encoding response body
                               (muuntaja/format-response-interceptor)
                               ;; exception handling
                               (exception/exception-interceptor)
                               ;; decoding request body
                               (muuntaja/format-request-interceptor)
                               ;; coercing response bodys
                               (coercion/coerce-response-interceptor)
                               ;; coercing request parameters
                               (coercion/coerce-request-interceptor)]}})
   (ring/create-default-handler)
   {:executor sieppari/executor}))

(defn init-server
  [{:keys [service development? host port]}]
  (web/run service {:host host :port port}))

(defn halt-server!
  [server]
  (when server (web/stop server)))
