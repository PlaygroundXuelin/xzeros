(ns xzeros.user
  (:require
    [io.pedestal.http.body-params :as body-params]
    [xzeros.service]
    [clojure.data.json]
    [io.pedestal.http.ring-middlewares :as middlewares]
    [ring.middleware.session.cookie :as cookie]
    [xzeros.db.user :as db-user]))

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

(defn nonce [request]
  (let [name (-> request :params :name)
       ]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (clojure.data.json/write-str {:data (get-nonce name)})}
    )
  )

(defn login [request]
  (let [name (-> request :params :name)
        pw (-> request :params :password)
        ]
    (let [login? (valid-auth name pw)]
      {:status 200
       :session {:user (if login? name nil)}
       :headers {"Content-Type" "application/json"}
       :body (clojure.data.json/write-str {:data login?})}
      )
    )
  )

(defn check [request]
  (let [session (:session request)
        name (:user session)]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (clojure.data.json/write-str {:data name})}
    )
  )

(defn logout [request]
  (let [name (-> request :params :name)
        ]
    {:status 200
     :session {:user nil}
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
