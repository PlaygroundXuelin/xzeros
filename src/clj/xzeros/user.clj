(ns xzeros.user
  (:require
    [io.pedestal.http.body-params :as body-params]
    [xzeros.service]
    [clojure.data.json]
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
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (clojure.data.json/write-str {:data (valid-auth name pw)})}
    )
  )

(def routes
  [
   "/user"
   ["/nonce"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
    {:get `xzeros.user/nonce }
    ]
   [
    "/login"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
    {:post `xzeros.user/login}
    ]
   ]
  )
