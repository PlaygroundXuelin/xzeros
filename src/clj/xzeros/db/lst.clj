(ns xzeros.db.lst
  (:require [xzeros.utils :as utils]
            [xzeros.db.jedis :as jedis])
  (:import (xzeros Redis)))

(defn- lsts-key [user-name]
  (jedis/to-key "lst" "lsts" "user" user-name)
  )

(defn- get-lst-ids [user-name lst-name]
  (let [k (lsts-key user-name)]
    (jedis/with-redis
      redis
      (if (nil? lst-name)
        (into {} (.hgetAll redis k))
        {lst-name (.hget redis k lst-name)}
        )
      )
    )
  )

(defn delete-lst [user-name lst-name]
  (let [k (lsts-key user-name)]
    (jedis/with-redis
      redis
      (Redis/hdelStr redis k (into-array String [lst-name]))
      )
    )
  )

(defn get-lst-id [user-name lst-name]
  ((get-lst-ids user-name lst-name) lst-name)
  )

(defn get-or-new-lst-id [user-name lst-name]
  (let [id (get-lst-id user-name lst-name)]
    (if (nil? id)
      (let [new-id (utils/new-uuid)
            k (lsts-key user-name)]
        (jedis/with-redis
          redis
          (Redis/hsetStr redis k lst-name new-id)
          )
        new-id
        )
      id
      )
    )
  )

(defn get-lst-items [lst-id begin-index end-index]
  (jedis/lrange lst-id begin-index end-index)
  )

(defn set-lst-items [items lst-id begin-index]
  (jedis/with-redis
    redis
    (jedis/lrange-set items lst-id begin-index)
  )
)

(defn set-lst-length [lst-id length]
  (jedis/with-redis
    redis
    (.ltrim redis lst-id 0 (dec length))
    )
  )
