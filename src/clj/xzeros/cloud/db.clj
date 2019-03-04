(ns xzeros.cloud.db
  (:require
    [datomic.client.api :as d]
    [xzeros.cloud.schema :as schema]
    [clojure.set :as set]
    )
  (:import (java.util Date UUID))
  )

(def cfg {:server-type :peer-server
          :access-key "myaccesskey"
          :secret "mysecret"
          :endpoint "localhost:8998"})

(def get-client
  (memoize #(d/client cfg)))

(defn- anom-map
  [category msg]
  {:cognitect.anomalies/category (keyword "cognitect.anomalies" (name category))
   :cognitect.anomalies/message msg})

(defn- anomaly!
  ([name msg]
   (throw (ex-info msg (anom-map name msg))))
  ([name msg cause]
   (throw (ex-info msg (anom-map name msg) cause))))

(defn ensure-dataset
  "Ensure that a database named db-name exists, running setup-fn
against a connection. Returns connection"
  [db-name setup-sym]
  (require (symbol (namespace setup-sym)))
  (let [setup-var (resolve setup-sym)
        client (get-client)]
    (when-not setup-var
      (anomaly! :not-found (str "Could not resolve " setup-sym)))
    (let [conn (d/connect client {:db-name db-name})
          db (d/db conn)]
      (setup-var conn)
      conn)))

(defn- has-ident?
  [db ident]
  (contains? (d/pull db {:eid ident :selector [:db/ident]})
             :db/ident))

(defn- data-loaded?
  [db]
  (has-ident? db :jobBatch/id))

(defn load-dataset
  [conn]
  (let [db (d/db conn)]
    (if (data-loaded? db)
      :already-loaded
      (let [xact #(d/transact conn {:tx-data %})]
        (xact schema/cloud-config-schema)
        :loaded))))

(defn get-connection
  []
  (ensure-dataset "xzero"
                  'xzeros.cloud.db/load-dataset))

(defn new-batch [conn]
  (let [uuid (UUID/randomUUID)
        batch {:jobBatch/id uuid
         :jobBatch/status {:task/status :taskStatus/not-started }
         }
        ]
    (d/transact conn {:tx-data [batch]})
    )
  )

(defn find-all-batches [db]
  (let [query '[:find (pull ?e [:jobBatch/id {:jobBatch/status [* {:task/status [:db/ident]}]}]) :where [?e :jobBatch/id]]
        ]
    (d/q query db)
    )
  )

(defn new-job [batch-id vars conn]
  (let [uuid (UUID/randomUUID)
        job-data {
         :job/id uuid
         :job/jobBatch [:jobBatch/id batch-id]
         :job/vars vars
         :job/status {:task/status :taskStatus/started :task/startTime (Date.)}
         }
        ]
    (d/transact conn {:tx-data [job-data]})
    ))

(defn find-all-jobs [db]
  (let [query   '[:find (pull ?e
                              [
                               *
                               {:job/status [* {:task/status [:db/ident]}]}
                               {:job/jobBatch [:jobBatch/id]}
                               ]
                              ) :where [?e :job/id]]
        ]
    (d/q query db)
    )
  )

(defn find-jobs-by-ids [job-ids db]
  (let [query   '[:find (pull ?e [* {:job/vars [:db/id :kv/key :kv/value]} {:job/status [* {:task/status [:db/ident]} ]} {:job/jobBatch [:jobBatch/id]} ])
                  :in $, [?job-id ...]
                  :where [?e :job/id ?job-id]]
        ]
    (d/q find-jobs-by-ids db job-ids)
    )
  )

(defn vars-to-map [vars]
  (into {}
        (map (fn [{:keys [:kv/key :kv/value]}] [key value]) vars)
        )
  )

(defn get-job-vars [job]
  (vars-to-map (:job/vars job))
  )

(defn update-job-vars [job-id new-vars conn]
  (let [db (d/db conn)
        job (ffirst (d/q find-jobs-by-ids db [job-id]))
        old-vars-map (get-job-vars job)
        retract-keys (set/intersection (set (keys old-vars-map)) (set (keys new-vars)))
        retract-vars-eids
        (let [old-vars (:job/vars job)
              retract-vars (filter #(retract-keys (:kv/key %)) old-vars)]
          (map :db/id retract-vars))
        retracts (mapv (fn [var-eid] [:db/retract (:db/id job) :job/vars var-eid]) retract-vars-eids)
        new-vars-data (mapv (fn [[k v]] {:kv/key k :kv/value v}) (seq new-vars))
        assertion {:job/id (:job/id job) :job/vars new-vars-data}
        ]
    (d/transact conn {:tx-data (conj retracts assertion)})
    ))

(defn add-job-vars [job-id new-vars conn]
  (let [
        new-vars-data (mapv (fn [[k v]] {:kv/key k :kv/value v}) (seq new-vars))
        assertion {:job/id (:job/id job-id) :job/vars new-vars-data}
        ]
    (d/transact conn {:tx-data [assertion]})
    ))

(defn find-orgs-by-ids [org-ids db]
  (let [query   '[:find (pull ?e [*])
                  :in $, [?org-id ...]
                  :where [?e :org/id ?org-id]]]
    (d/q find-orgs-by-ids db org-ids)
    )
  )

