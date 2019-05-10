(ns xzeros.cloud
  (:require
    [io.pedestal.http.body-params :as body-params]
    [xzeros.service]
    [clojure.data.json :as json]
    [xzeros.user]
    [xzeros.db.lst :as db-lst]
    [clojure.string :as str]
    )
  )

(defn rest-execute [{:keys [json-params headers] :as request}]
  (let [ user (xzeros.user/getAuthUser request)]
    (if (str/blank? user)
      xzeros.service/permission-denied-response

      (let [list-id (json-params :list-id)
            item-id (json-params :item-id)]
        (xzeros.service/no-impl-response user list-id item-id)
        )
      )
    )
  )

(def routes
  [
   "/cloud"
   ["/config"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
    {:get `xzeros.cloud/rest-execute }
    ]
   ;["/item"
   ; ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
   ; {:get `xzeros.lst/rest-execute }
   ; ]
   ;["/updateItem"
   ; ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
   ; {:get `xzeros.lst/rest-execute }
   ; ]
   ;["/deleteItem"
   ; ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
   ; {:get `xzeros.lst/rest-execute }
   ; ]
   ]
  )

