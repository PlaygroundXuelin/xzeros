(ns xzeros.lst
  (:require
    [io.pedestal.http.body-params :as body-params]
    [xzeros.service]
    [clojure.data.json]
    [xzeros.user]
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

(defn get-or-new [{:keys [params headers] :as request}]
  (let [ user (xzeros.user/getAuthUser request)]
    (if (str/blank? user)
      xzeros.service/permission-denied-response

      (let [name (params :name)
            ]
        (xzeros.service/no-impl-response user name)
        )
      )
    )
  )

(defn update-lst [{:keys [params headers] :as request}]
  (let [ user (xzeros.user/getAuthUser request)]
    (if (str/blank? user)
      xzeros.service/permission-denied-response

      (let [{:keys [name ]} (params :name)
            ]
        (xzeros.service/no-impl-response user name)
        )
      )
    )
  )

(defn delete-lst [{:keys [params headers] :as request}]
  (let [ user (xzeros.user/getAuthUser request)]
    (if (str/blank? user)
      xzeros.service/permission-denied-response

      (let [{:keys [name ]} (params :name)
            ]
        (xzeros.service/no-impl-response user name)
        )
      )
    )
  )

(def routes
  [
   "/lst"
   ["/lsts"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
    {:get `xzeros.lst/rest-execute }
    ]
   ["/getOrNew"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
    {:get `xzeros.lst/get-or-new }
    ]
   ["/update"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
    {:get `xzeros.lst/update-lst }
    ]
   ["/delete"
    ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
    {:get `xzeros.lst/delete-lst }
    ]
   ;["/item"
   ; ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
   ; {:get `xzeros.lst/rest-execute }
   ; ]
   ;["/updateItem"
   ; ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
   ; {:get `xzeros.lst/rest-execute }
   ; ]
   ;["/addItem"
   ; ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
   ; {:get `xzeros.lst/rest-execute }
   ; ]
   ;["/deleteItem"
   ; ^:interceptors [xzeros.service/coerce-body xzeros.service/content-neg-intc (body-params/body-params)]
   ; {:get `xzeros.lst/rest-execute }
   ; ]
   ]
  )

