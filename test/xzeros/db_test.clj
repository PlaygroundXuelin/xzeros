(ns xzeros.db-test
  (:require [clojure.test :refer :all]
            [xzeros.config]
            [xzeros.db.user :as user]
            [xzeros.db.lst :as lst]
            [xzeros.db.jedis :as redis]
            )
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

      (is (thrown? IndexOutOfBoundsException
                   (redis/lrange "k" -1 1)
                   ))
      (is (thrown? IndexOutOfBoundsException
                   (redis/lrange "k" 0 -2)
                   ))
      (is (= [] (redis/lrange "k" 0 0)))
      (is (= [] (redis/lrange "k" 0 -1)))

      (let [k "krangeset"]
        (is (= [] (redis/lrange k 0 -1)))

        (redis/lrange-set ["a0"] k 0)
        (is (= ["a0"] (redis/lrange k 0 -1)))

        (redis/lrange-set ["b0" "b1"] k 0)
        (is (= ["b0" "b1"] (redis/lrange k 0 -1)))

        (redis/lrange-set ["c1" "c2" "c3"] k 1)
        (is (= ["b0" "c1" "c2" "c3"] (redis/lrange k 0 -1)))

        (redis/lrange-set [ "d2" "d3"] k 2)
        (is (= ["b0" "c1" "d2" "d3"] (redis/lrange k 0 -1)))

        (redis/lrange-set [ "e0" "e1"] k 0)
        (is (= ["e0" "e1" "d2" "d3"] (redis/lrange k 0 -1)))

        (redis/lrange-set [ "f3"] k 3)
        (is (= ["e0" "e1" "d2" "f3"] (redis/lrange k 0 -1)))

        (redis/lrange-set [ "g5" "g6"] k 5)
        (is (= ["e0" "e1" "d2" "f3" "" "g5" "g6"] (redis/lrange k 0 -1)))

        (redis/lrange-set [ "h6"] k 6)
        (is (= ["e0" "e1" "d2" "f3" "" "g5" "h6"] (redis/lrange k 0 -1)))

        )

      (is (= (save-find-user "name" "password" "nonce" true)
             {"password" "password" "nonce" "nonce" "verified" true}
             ))

      (is (nil? (lst/get-lst-id "user" "lst")))

      (let [id (lst/get-or-new-lst-id "user" "lst")]
        (is (not (nil? id)))
        (is (= (.length id) 32))
        (is (= id (lst/get-or-new-lst-id "user" "lst")))
        )

      (let [id (lst/get-or-new-lst-id "user" "lst")]
        (is (not (nil? id)))
        (is (= (.length id) 32))
        (is (= id (lst/get-or-new-lst-id "user" "lst")))

        (is (= [] (lst/get-lst-items id 0 -1)))

        (lst/set-lst-items ["a" "b"] id 0)
        (is (= ["a" "b"] (lst/get-lst-items id 0 -1)))

        (lst/set-lst-items ["c" "d"] id 1)
        (is (= ["a" "c" "d"] (lst/get-lst-items id 0 -1)))

        (lst/set-lst-length id 2)
        (is (= ["a" "c"] (lst/get-lst-items id 0 -1)))

        )

      (finally (RedisTestUtils/stopServer test-redis-server)))
    )
  )
