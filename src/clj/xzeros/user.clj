(ns xzeros.user
  (:require
    [io.pedestal.http.body-params :as body-params]
    [xzeros.service]
    [clojure.data.json]
    [io.pedestal.http.ring-middlewares :as middlewares]
    [ring.middleware.session.cookie :as cookie]
    [xzeros.db.user :as db-user]
    [clojure.string :as str])
  (:import (xzeros Jwt)
           )
  )

(def bearerPrefix "Bearer ")

(defn get-nonce [name]
  (get (db-user/find-user name) "nonce")
  )

(defn valid-auth [name password]
  (let [u (db-user/find-user name)]
    (and
      (= "true" (get u "verified"))
      (= password (get u "password")))
    )
  )

(defn register [name password nonce verified]
  (let [u (db-user/find-user name)]
    (if (nil? u)
      (do
        (db-user/save-user name password nonce verified)
        true)
      false
      )
    )
  )

(defn nonce [{:keys [params] :as request}]
  (let [name (params :name)
       ]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (clojure.data.json/write-str {:data (get-nonce name)})}
    )
  )

(defn login [{:keys [params] :as request}]
  (let [name (params :name)
        pw (params :password)
        login? (valid-auth name pw)
        headers {"Content-Type" "application/json"}
        bearer (if login? (Jwt/createTokenForSubject name))
        ]
    {:status 200
     :headers headers
     :body (clojure.data.json/write-str {:data bearer})}
    )
  )

(defn getBearerFromHeaders [headers]
  (let [_ (println "headers: " headers)
        auth-header (headers "authorization")
        _ (println "authheader: " auth-header)
        bearer (if (and auth-header (.startsWith auth-header "Bearer")) (.substring auth-header (.length bearerPrefix)))
        _ (println "bearer in get: " bearer)
        ]
    bearer
    )
  )

(defn getSubjectFromHeaders [headers]
  (let [bearer (getBearerFromHeaders headers)]
    (if bearer (Jwt/decodeTokenSubject bearer))
    )
  )

(defn getAuthUser [{:keys [headers] :as request}]
  (getSubjectFromHeaders headers)
  )

(defn check [request]
  (let [name (getAuthUser request)
        ]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (clojure.data.json/write-str {:data name})}
    )
  )

(defn logout [request]
  (let [name (getAuthUser request)
        ]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (clojure.data.json/write-str true)}
    )
  )

(def routes
  [
   "/user"
   ["/nonce"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params) xzeros.service/session-intc]
    {:get `xzeros.user/nonce }
    ]
   ["/check"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params) xzeros.service/session-intc]
    {:get `xzeros.user/check }
    ]
   [
    "/login"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params) xzeros.service/session-intc]
    {:get `xzeros.user/login}
    ]
   [
    "/logout"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params) xzeros.service/session-intc]
    {:get `xzeros.user/logout}
    ]
   ]
  )
