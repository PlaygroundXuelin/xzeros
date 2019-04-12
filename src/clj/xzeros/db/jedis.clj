(ns xzeros.db.jedis
  (:require [clojure.string :as str])
  (:import (xzeros Redis)
           (redis.clients.jedis JedisPool))
  )

(defn to-key [& args]
  (str/join "`_" args)
  )

(defn create-jedis-pool [host port]
  (Redis/getJedisPool host port)
  )

(defmacro with-redis-pool [pool redis & body]
  `(with-open [~redis (.getResource ~pool)]
     ~@body
     )
  )

(defmacro with-redis [redis & body]
  `(with-open [~redis (.getResource (create-jedis-pool "127.0.0.1" 6379))]
     ~@body
     )
  )