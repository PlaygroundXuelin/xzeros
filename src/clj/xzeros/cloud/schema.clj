(ns xzeros.cloud.schema)

(def cloud-config-schema
  [
   ;; general enum data
   {:db/ident :httpMethod/get}
   {:db/ident :httpMethod/post}
   {:db/ident :httpMethod/put}

   {:db/ident :taskStatus/not-started}
   {:db/ident :taskStatus/started}
   {:db/ident :taskStatus/completed}
   {:db/ident :taskStatus/failed}

   ;; common schema
   {:db/ident :kv/key
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "variable name"
    }

   {:db/ident :kv/value
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "variable value"
    }

   {:db/ident :task/status
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "job batch account"
    }

   {:db/ident :task/startTime
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc "job batch account"
    }

   {:db/ident :task/endTime
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc "job batch account"
    }

   ;; request parameter binding spec
   {:db/ident :restParam/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "parameter name"
    }

   {:db/ident :restParam/key
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "define what value to apply the expression"
    }

   {:db/ident :restParam/expression
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "define how to get the parameters"
    }

   ;; rest calls spec
   {:db/ident :rest/id
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "id of rest call"
    }

   {:db/ident :rest/url
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "A http url with variable values {variableName} to be substituted with bound values."
    }

   {:db/ident :rest/method
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "http method, e.g. get, post, delete."
    }

   {:db/ident :rest/contentType
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "http content type."
    }

   {:db/ident :rest/content
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "http content."
    }

   {:db/ident :rest/params
    :db/valueType :db.type/ref
    :db/isComponent true
    :db/cardinality :db.cardinality/many
    :db/doc "rest parameters"
    }

   {:db/ident :rest/storeKey
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one
    :db/doc "rest result store key"
    }

   {:db/ident :rest/groupId
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "rest group id"
    }

   ;; accounts group / org
   ;; account
   {:db/ident :org/id
    :db/unique :db.unique/identity
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "organization id"
    }

   {:db/ident :org/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "organization name"
    }

   {:db/ident :account/id
    :db/unique :db.unique/identity
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "account id"
    }

   {:db/ident :account/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "account name"
    }

   {:db/ident :account/org
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "owner org id"
    }

   {:db/ident :account/gcpServiceAccountUser
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Account GCP service Account User"}

   {:db/ident :account/gcpSecret
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Account GCP accountSecret"}

   {:db/ident :account/gcpTokenTime
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc "Token start time"}

   {:db/ident :account/gcpTokenRefreshMs
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "Token refresh millis"}



   ;; schedule batch
   {:db/ident :jobBatch/id
    :db/unique :db.unique/identity
    :db/valueType :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/doc "job schedule batch id, generated via (java.util.UUID/randomUUID)"
    }

   {:db/ident :jobBatch/account
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "job batch account"
    }

   {:db/ident :jobBatch/restGroup
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "rest group"
    }

   {:db/ident :jobBatch/status
    :db/isComponent true
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "job batch account"
    }

   ;; scheduled job
   {:db/ident :job/id
    :db/unique :db.unique/identity
    :db/valueType :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/doc "job id, generated via (java.util.UUID/randomUUID)"
    }

   {:db/ident :job/jobBatch
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "job batch"
    }

   {:db/ident :job/rest
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "job rest spec"
    }

   {:db/ident :job/vars
    :db/valueType :db.type/ref
    :db/isComponent true
    :db/cardinality :db.cardinality/many
    :db/doc "job variable bindings. Component has name and value, both are strings"
    }

   {:db/ident :job/status
    :db/isComponent true
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc "job status"
    }

   ]
  )
