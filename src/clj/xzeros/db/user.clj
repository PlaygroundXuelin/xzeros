(ns xzeros.db.user
  (:require [xzeros.utils :as utils]
            [xzeros.db.jedis :as jedis]))

(defn save-user [name password nonce verified]
  (jedis/with-redis
    redis
    (let [t (.multi redis)
          k (jedis/to-key "user" name)]
      (doseq [[v-k v-v]
              [["nonce" nonce] ["password" password] ["verified" (String/valueOf verified)]]]
        (.hset t k v-k v-v)
        )
      (.exec t)
      )
    )
  )

(defn find-user [name]
  (jedis/with-redis
    redis
    (let [k (jedis/to-key "user" name)
          db-hm (.hgetAll redis k)]
      (assoc (into {} db-hm) "verified" (Boolean/valueOf (.get db-hm "verified")))
      )
    )
  )
