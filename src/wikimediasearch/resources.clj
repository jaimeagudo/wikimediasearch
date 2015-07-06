(ns wikimediasearch.resources
  (:require
   [taoensso.timbre :as log :refer (trace debug info warn error report)]
   [bouncer.core :as b]
   [bouncer.validators :as v]
   [wikimediasearch.repo :as repo]
   )
  (:use
   [liberator.core :only [defresource]]
   [wikimediasearch.util]
   ))


(defn malformed?
  "Receives a Liberator context and a bouncer validation and returns a Liberator malformed?-decision-point"
  [validation]
  (fn [ctx]
    (let [params (get-in ctx [:request :params])
          validation-result (first (b/validate params validation))]
      (if (empty? validation-result)
        [false]
        [true {:message (flatten (vals validation-result))}]))))


;; Include this key into the search-resource map
(def SEARCH_VALIDATIONS {:q [v/required v/string]})


;; ## Site Resources
(defresource search-resource
  :handle-exception handle-exception
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :malformed? (malformed? SEARCH_VALIDATIONS)
  :exists?   (fn [ctxt]
               (let [q (get-in ctxt [:request :params :q])
                     results (repo/search q)]
                 (log/trace "Search results=" (pformat results))
                 (if (empty? results)
                   [false]
                   [true {:q q
                          :results results}])))
  :handle-ok (fn [ctxt]
               (select-keys ctxt [:results :q])
               ))
