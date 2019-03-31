(ns xzeros.ml.playground
  (:require [uncomplicate.commons.core :refer [with-release]]
            [uncomplicate.neanderthal
             [native :refer [dv dge]]
             [core :refer [mv!]]]
            )
  )

(with-release [x (dv 0.3 0.9)
               w1 (dge 4 2 [0.3 0.6
                            0.1 2.0
                            0.9 3.7
                            0.0 1.0]
                       {:layout :row})
               h1 (dv 4)]
              (println (mv! w1 x h1)))

(with-release [x (dv 0.3 0.9)
               w1 (dge 4 2 [0.3 0.6
                            0.1 2.0
                            0.9 3.7
                            0.0 1.0]
                       {:layout :row})
               h1 (dv 4)
               w2 (dge 1 4 [0.75 0.15 0.22 0.33])
               y (dv 1)]
              (println (mv! w2 (mv! w1 x h1) y)))