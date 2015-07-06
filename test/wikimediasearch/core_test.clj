(ns wikimediasearch.core-test
  (:require [ring.mock.request :as mock]
            [cheshire.core :refer :all])
  (:use [midje.sweet]
        [wikimediasearch.repo]
        [wikimediasearch.core]
        [wikimediasearch.resources]))


  (facts "REST basic tests"
         (store-n-index! "resources/small.xml")
         (fact "GET search?q=Kosmos returns 200 and a JSON body"
               (let [expected-body {:q "Kosmos"
                                    :results [{:title "Wikipedia: Kosmos 2221"
                                              :url "http://en.wikipedia.org/wiki/Kosmos_2221"
                                              :abstract "| launch_date = UTC" }]}
                     response (site (mock/request :get "/search" {:q "Kosmos" }))]
                 (:status response) => 200
                 (parse-string (:body response) true) => expected-body
                 (get-in response [:headers "Content-Type"]) => "application/json;charset=UTF-8"))

         (fact "GET search?qs=whatever returns 400"
               (let [response (site (mock/request :get "/search" {:qs "whatever" }))]
                 (:status response) => 400))
         )

