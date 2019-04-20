(ns xzeros.db-test
  (:require [clojure.test :refer :all]
            [xzeros.config]
            [xzeros.db.user :as user])
  (:import (xzeros RedisTestUtils))
  )
(defn start-test-redis-server []
  (let [port 7934
        _ (swap! xzeros.config/config assoc-in [:database :port] port)]
    (RedisTestUtils/startServer port)
    )
  )

(defn save-find-user [name password nonce verified]
  (let [_ (user/save-user name password nonce verified)
        u (user/find-user name)]
    u)
  )

(deftest db-test
  (let [test-redis-server (start-test-redis-server)]
    (try

      (is (= (save-find-user "name" "password" "nonce" true)
             {"password" "password" "nonce" "nonce" "verified" true}
             ))

      (finally (RedisTestUtils/stopServer test-redis-server)))
    )
  )
