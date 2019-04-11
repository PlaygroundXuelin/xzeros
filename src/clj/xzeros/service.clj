(ns xzeros.service
  (:require
    [clojure.data.json :as json]
    [io.pedestal.http.ring-middlewares :as middlewares]
    [ring.middleware.session.cookie :as cookie]
    [io.pedestal.http.content-negotiation :as conneg]
    )
  )

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))
(def accepted (partial response 202))

(def not-found (response 404 "Not found"))

(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])
(def content-neg-intc (conneg/negotiate-content supported-types))
(def session-intc (middlewares/session {:store (cookie/cookie-store)}))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn transform-content
  [body content-type]
  (case content-type
    "text/html"        body
    "text/plain"       body
    "application/edn"  (pr-str body)
    "application/json" (json/write-str body)))

(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(def coerce-body
  {:name ::coerce-body
   :leave
         (fn [context]
           (cond-> context
                   (nil? (get-in context [:response :headers "Content-Type"]))
                   (update-in [:response] coerce-to (accepted-type context))))})
