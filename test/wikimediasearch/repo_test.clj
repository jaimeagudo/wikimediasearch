(ns wikimediasearch.repo-test
  (:require [ring.mock.request :as mock])
  (:use [midje.sweet]
        [wikimediasearch.repo]
        [wikimediasearch.resources]
        [clojure.pprint]
        ))



(facts "Update token ocurrencies"
       (let [empty-map {}
             url1 "http://en.wikipedia.org/wiki/Ahmad_Reza_Pourdastan"
             word "Persian"
             updated-map (add-val-in-map url1 empty-map word)]
         (fact "Should add non-existent words"
               (get updated-map word) => [url1])
         (fact "Should update existent words"
               (let [url2 "http://en.wikipedia.org/wiki/Another_one"
                     updated-map2 (add-val-in-map url2 updated-map word)]
                 (get updated-map2 word) => [url2 url1]))))



(facts "Index docs"
       (let [url "Ahmad_Reza_Pourdastan"
             doc {:title "Wikipedia: Ahmad Reza Pourdastan"
                  :abstract  "Ahmad Reza Pourdastan (Persian: امیر سرتیپ احمدرضا پوردستان) is an Iranian general currently serving as commanding officer of the ground forces of the Iranian Army.<Ref>"}
             dict-index (index-doc! (hash-map) url doc)]
         (fact "Leading words are correctly indexed"
               (get dict-index "Ahmad") => [url])
         (fact "Regular words are correctly indexed"
               (get dict-index "Iranian") => [url])
         (fact "Trailing words are correctly indexed"
               (get dict-index "Iranian") => [url])
         (fact "Words from title are correctly indexed"
               (get dict-index "Wikipedia") => [url])
         (fact "Search Words from title are correctly indexed"
               (count (search "Wikipedia")) => 7)
         (fact "Search Persian Words works"
               (count (search  "احمدرضا")) => 1)
))

(facts "Build and search docs"
       (let [[dict index] (store-n-index! "resources/small.xml")]
         (fact "All appearences are correctly counted"
               (count (search "Wikipedia")) => 7)
         (fact "Single appearences are correctly counted"
               (count (search "Kosmos")) => 1)
         (fact "Missing words are correctly reported"
               (count (search "Jaime")) => 0)
         (fact "Emmpty queries are correctly ignored"
               (count (search "")) => 0)
         ))

