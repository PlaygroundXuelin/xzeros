(ns xzeros.db.jedis
  (:require [clojure.string :as str])
  (:import (xzeros Redis)
           (redis.clients.jedis JedisPool))
  )

(defn to-key [& args]
  (str/join "`_" args)
  )

(defn create-jedis-pool [host port]
  (JedisPool. (Redis/buildPoolConfig) host port)
  )

(def jedis-pool (create-jedis-pool "127.0.0.1" 6379))

(defmacro with-redis-pool [pool redis & body]
  `(with-open [~redis (.getResource ~pool)]
     ~@body
     )
  )

(defmacro with-redis [redis & body]
  (concat
    (list 'xzeros.db.jedis/with-redis-pool 'xzeros.db.jedis/jedis-pool redis)
    body
    )
  )