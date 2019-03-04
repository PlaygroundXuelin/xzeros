(ns xzeros.xzero
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [xzeros.service]
            ))

(def echo
  {:name ::echo
   :enter #(assoc % :response (xzeros.service/ok (:request %)))})


(def routes
  ["/xzero-echo" {:get `xzeros.xzero/echo }]
  )
