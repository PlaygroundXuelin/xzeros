(ns xzeros.db-test
  (:require [clojure.test :refer :all]
            [xzeros.config]
            [xzeros.db.user :as user]
            [xzeros.db.lst :as lst]
            [xzeros.db.jedis :as redis]
            [clojure.data.json :as json]
            [xzeros.xservice :as service]
            [io.pedestal.http :as bootstrap]
            [io.pedestal.test :refer :all]
            )
  (:import (xzeros RedisTestUtils Jwt))
  )

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

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

      (let [resp (response-for service :post "/lst/getOrNew")]
        (is (= 200 (:status resp)))
        (is (= "{\"error\":\"permission denied\"}" (:body resp)))
        )

      (let [user "user"
            bearer (Jwt/createTokenForSubject user)
            req-headers {"Content-Type" "application/json" "authorization" (str "Bearer " bearer)}
            resp (response-for service :post (str "/lst/getOrNew") :body "{\"name\": \"abc\"}" :headers req-headers)]
        (is (= 200 (:status resp)))
        (let [resp-body (json/read-str (:body resp) :key-fn keyword)
              data (:data resp-body)
              lst-id (:lst-id data)
              items (:items data)
              ]
          (is (not (nil? lst-id)))
          (is (= [] items))

          (let [resp2  (response-for service :post (str "/lst/update") :body (json/write-str {:name "abc" :items ["a0" "a1"]} :key-fn name) :headers req-headers)
                resp-body (json/read-str (:body resp2) :key-fn keyword)
                data (:data resp-body)
                ]
            (is (= lst-id data))
            )

          (let [resp3  (response-for service :post (str "/lst/getOrNew") :body (json/write-str {:name "abc"} :key-fn name) :headers req-headers)
                resp-body (json/read-str (:body resp3) :key-fn keyword)
                data (:data resp-body)
                lst-id3 (:lst-id data)
                items3 (:items data)]
            (is (= lst-id lst-id3))
            (is (= items3 ["a0" "a1"]))
            )

          (let [resp2  (response-for service :post (str "/lst/addItems") :body (json/write-str {:name "abc" :items ["a2"]} :key-fn name) :headers req-headers)
                resp-body (json/read-str (:body resp2) :key-fn keyword)
                data (:data resp-body)
                ]
            (is (= lst-id data))
            )

          (let [resp3  (response-for service :post (str "/lst/getOrNew") :body (json/write-str {:name "abc"} :key-fn name) :headers req-headers)
                resp-body (json/read-str (:body resp3) :key-fn keyword)
                data (:data resp-body)
                lst-id3 (:lst-id data)
                items3 (:items data)]
            (is (= lst-id lst-id3))
            (is (= items3 ["a0" "a1" "a2"]))
            )

          (let [resp4  (response-for service :get (str "/lst/delete") :body (json/write-str {:name "abc"} :key-fn name) :headers req-headers)
                resp-body (json/read-str (:body resp4) :key-fn keyword)
                data (:data resp-body)
                ]
            (is (= true data))
            )

          (let [resp5  (response-for service :post (str "/lst/getOrNew") :body (json/write-str {:name "abc"} :key-fn name) :headers req-headers)
                resp-body (json/read-str (:body resp5) :key-fn keyword)
                data (:data resp-body)
                lst-id5 (:lst-id data)
                items5 (:items data)]
            (is (not= lst-id lst-id5))
            (is (= items5 []))
            )

          )
       )



      (finally (RedisTestUtils/stopServer test-redis-server)))
    )
  )
