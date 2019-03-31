(ns xzeros.cmd
  (:require
    [io.pedestal.http.body-params :as body-params]
            [xzeros.service]
            [clojure.data.json]
            )
  )

(def supported-script-types ["bash" "clojure"])

(def echo
  {:name ::echo
   :enter #(assoc % :response (xzeros.service/ok (:request %)))})

(defn- execute-bash [script]
  (let
    [
     results "not implemented yet"]
    {:data results}))

(defn- execute-clj [script]
  (let
    [
     results (eval (read-string script))]
    {:data results}))

(defn execute [cmd-type script]
  (let []
    (case cmd-type
      "bash" (execute-bash script)
      "clojure" (execute-clj script)
      {:error (str "type must be in " supported-script-types ": " cmd-type)})))

(defn rest-execute [request]
  (let [cmd-type (-> request :json-params :cmd-type)
        script (-> request :json-params :script)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (clojure.data.json/write-str (execute cmd-type script))}
    )
  )

(def routes
  [
   "/cmd"
   ["/echo"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
    {:get `xzeros.cmd/echo }
    ]
   [
    "/execute"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
    {:post `xzeros.cmd/rest-execute}
    ]
   ]
  )

