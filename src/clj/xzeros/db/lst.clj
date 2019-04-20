(ns xzeros.db.lst
  (:require [xzeros.utils :as utils]
            [xzeros.db.jedis :as jedis]))

(defn get-lst-ids [user-name lst-name]
  (jedis/with-redis
    redis
    (if (nil? lst-name)
      (let [k (jedis/to-key "lst" "lsts" "user" user-name)]
        (.hgetAll redis k)
        )
      (let [k (jedis/to-key "lst" "lsts" "user" user-name)]
        {lst-name (.hget redis k lst-name)}
        )
      )
    )
  )

(defn get-lst-items [lst-id begin-index end-index]
  (jedis/with-redis
    redis
    (if (nil? lst-name)
      (let [k (jedis/to-key "lst" "lst" lst-id)
            jedis-end (if (neg? end-index) end-index (dec end-index))]
        (.lrange redis k begin-index jedis-end)
        )
      )
    )
  )

(defn set-lst-items [items lst-id begin-index]
  (jedis/with-redis
    redis
    (let [curr-len (.llen redis lst-id)
          items-count (count items)]
      (if (> begin-index curr-len)
        (let [tmp-arr (into-array String (repeat (- begin-index curr-len) ""))]
          (.rpush redis lst-id tmp-arr)
          )
        )
      (if (< begin-index curr-len)
        (let [
              set-count (min items-count (- curr-len begin-index))
              ]
          (for [ii [range 0 set-count]]
            (.lset redis lst-id (+ begin-index ii) (nth ii items))
            )
          )
        )
      (if (> (+ begin-index items-count) curr-len)
        (let [push-count0 (- (+ begin-index items-count) curr-len)
              push-count (min push-count0 items-count)]
          (.rpush redis lst-id (into-array String (drop (- items-count push-count) items)))
          )
        )
      )
  )
)
