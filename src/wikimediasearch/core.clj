(ns wikimediasearch.core
  (:require
   [liberator.dev :refer :all]
   [ring.middleware.defaults :refer :all]
   [compojure.core :as compojure :refer (GET ANY defroutes)]
   [org.httpkit.server :refer [run-server]]
   [ring.middleware.reload :as rl]
   [taoensso.timbre :as log :refer (trace debug info warn error report)]
   [wikimediasearch.repo :as repo]
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.java.io :as io]
   )
  (:use
   [wikimediasearch.resources]
   [wikimediasearch.util]
   [clojure.pprint]
   )
  (:gen-class))



(def cli-options
  ;; An option with a required argument
  [["-f" "--filename FILENAME" "XML input file"
    :default "resources/enwiki-latest-abstract23.xml"
    ;;     "resources/small.xml"
    :validate [#(.exists (io/as-file %)) "File must exists and be accesible, check path and permissions"]]
   ["-t" "--thread THREAD " "How many threads to compute response from request, default to 4"
    :default 4
    :parse-fn #(Integer/parseInt %)
    :validate [#(> % 4 ) "Must be a number bigger than 4"]]
   ["-q" "--queue-size QUEUE-SIZE" "Max requests queued waiting for thread pool to compute response before rejecting, 503(Service Unavailable) is returned to client if queue is full, default to 20K"
    :default 20000
    :parse-fn #(Integer/parseInt %)
    :validate [#( > % 20000)  "Must be a number bigger than 20000"]]
   ["-h" "--help" "Displays this"]])

(defn -main [& args]
  (parse-opts args cli-options))


;; ## Compojure Routes
(defroutes site-routes
  (GET "/" [] "Welcome to the wikimediasearch site!")
  (GET "/search" [] search-resource)
  )


(def site
  "Main handler for the example Compojure site."
  (-> site-routes
      ;;       (wrap-trace :header :ui)
      (wrap-defaults site-defaults)
      ))

;; ## Server Lifecycle

(defonce server (atom nil))

(defn kill!
  "Kill the server"
  []
  (swap! server (fn [s] (when s (s) nil)))
  (exit 0))


(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        {:keys [filename help]} options]
    ;;         filename (:file options)
    ;;         queue-size (:queue-size options)
    ;;         thread (:thread options)]
    (cond
     (seq errors)     (exit 1 errors)
     (:help options)  (exit 0 (usage "wiki cache" summary))
     :else            (swap! server
                             (fn [s]
                               (if s
                                 (do
                                   (println "Server already running!")
                                   s)
                                 (do
                                   (println "Booting server on port 8090 with " (pformat options) " Please be patient...")
                                   (repo/store-n-index! filename)
                                   (run-server (rl/wrap-reload #'site) (select-keys options [:queue-size :thread])))))
                             ))))

  (defn running?
    "Returns true if the server is currently running, false otherwise."
    []
    (identity @server))

  (defn cycle!
    "Cycles the existing server - shut down the relaunch."
    []
    (kill!)
    (-main))
