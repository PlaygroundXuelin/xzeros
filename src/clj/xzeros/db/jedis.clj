(ns xzeros.db.jedis
  (:require [clojure.string :as str]
            [xzeros.config])
  (:import (xzeros Redis)
           )
  )

(def key-sep "`_")
(defn to-key ^String [& args]
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

(defn- check-index [from to]
  (if (neg? from)
    (throw (IndexOutOfBoundsException. (str "Negative from index: " from)))
    )
  (if (and (neg? to) (not= to -1))
    (throw (IndexOutOfBoundsException. (str "Negative to index not -1: " from)))
    )
  )

(defn lrange [k begin-index end-index]
  (check-index begin-index end-index)
  (with-redis
    redis
    (cond
      (= end-index -1) (into [] (.lrange redis k begin-index -1))
      (>= begin-index end-index) []
      :else (into [] (.lrange redis k begin-index (dec end-index)))
      )
    )
  )

(defn lrange-set [items lst-id begin-index]
  (with-redis
    redis
    (let [curr-len (.llen redis lst-id)
          items-count (count items)
          set-count (min items-count (- curr-len begin-index))
          push-count (- (+ begin-index items-count) (max curr-len begin-index))]
      (if (> begin-index curr-len)
        (let [tmp-arr (into-array String (repeat (- begin-index curr-len) ""))]
          (.rpush redis lst-id tmp-arr)
          )
        )
      (doseq [ii (range 0 set-count)]
        (.lset redis lst-id (+ begin-index ii) (nth items ii))
        )
      (if (pos? push-count)
        (.rpush redis lst-id (into-array String (drop (- items-count push-count) items)))
        )
      )
    )
  )
