(ns xzeros.cmd
  (:require
    [io.pedestal.http.body-params :as body-params]
    [xzeros.service]
    [clojure.data.json]
    [xzeros.user]
    [clojure.string :as str]
    )
  (:import (xzeros RunProcess)))

(def supported-script-types ["bash" "clojure"])

(def echo
  {:name ::echo
   :enter #(assoc % :response (xzeros.service/ok (:request %)))})

(def maxStreamLen 10240)
(def timeoutMs 10000)
(defn- execute-bash [script]
  (let
    [[code outStr errStr] (RunProcess/exec ["sh" "-c" script] maxStreamLen timeoutMs)
     ]
    {:data {:exit code :out outStr :err errStr}}))

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

(defn rest-execute [{:keys [json-params headers] :as request}]
  (let [ user (xzeros.user/getAuthUser request)]
    (if (str/blank? user)
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (clojure.data.json/write-str {:error "permission denied"})}

      (let [cmd-type (json-params :cmd-type)
            script (json-params :script)]
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (clojure.data.json/write-str (execute cmd-type script))}
        )
      )
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

