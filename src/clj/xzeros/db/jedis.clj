(ns xzeros.db.jedis
  (:require [clojure.string :as str]
            [xzeros.config])
  (:import (xzeros Redis)
           )
  )

(def key-sep "`_")
(defn to-key [& args]
  (str/join key-sep (map #(.replace % "_" "\\_") args))
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
  `(with-open [~redis (.getResource (create-jedis-pool (get-in @xzeros.config/config [:database :host])
                                                       (get-in @xzeros.config/config [:database :port])))]
     ~@body
     )
  )