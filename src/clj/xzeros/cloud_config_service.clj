(ns xzeros.cloud-config-service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [xzeros.service]
            [xzeros.cloud.db :as cdb]
            [ring.util.response :as ring-resp]
            [clojure.pprint :as pp]
            [datomic.client.api :as d]
            ))

(defn root-page
  [request]
  (ring-resp/response "Welcome to cloud resources service!"))

(def echo
  {:name ::echo
   :enter #(assoc % :response (xzeros.service/ok (:request %)))})

(defn pp-str
  [x]
  (binding [*print-length* nil
            *print-level* nil]
    (with-out-str (pp/pprint x))))

(defn schema
  "Returns a data representation of db schema."
  [db]
  (->> (d/pull db '{:eid 0 :selector
                         [{:db.install/attribute [* {:db/unique [:db/ident]} {:db/valueType [:db/ident]} {:db/cardinality [:db/ident]}]}]})
       :db.install/attribute
       ;       (map #(update % :db/valueType :db/ident))
       ;       (map #(update % :db/cardinality :db/ident))
       ))

(def handle-schema
  "returns db schema"
  {:name ::schema
   :enter #(assoc % :response (xzeros.service/ok (-> (cdb/get-connection) d/db schema pp-str)))})

(def routes
  ["/db" {:get `xzeros.cloud-config-service/root-page }
   ["/schema" {:get `xzeros.cloud-config-service/handle-schema}]
   ]
  )
