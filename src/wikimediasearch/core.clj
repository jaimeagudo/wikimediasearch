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
  [["-f" "--file FILE" "XML input file"
    :default "resources/enwiki-latest-abstract23.xml"
    ;;     "resources/small.xml"
    :validate [#(.exists (io/as-file %)) "File must exists and be accesible, check path and permissions"]]
   ["-h" "--help"]])

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
  (let [{:keys [options errors]} (parse-opts args cli-options)
        filename (:file options)]
    (if (empty? errors)
      (swap! server
             (fn [s]
               (if s
                 (do (println "Server already running!") s)
                 (do (println (format "Booting server on port 8090 with %s Please be patient..." filename))
                   (repo/store-n-index! filename)
                   (run-server (rl/wrap-reload #'site) {:queue-size 204800 :thread 200})))))
     (exit 1 errors))))

(defn running?
  "Returns true if the server is currently running, false otherwise."
  []
  (identity @server))

(defn cycle!
  "Cycles the existing server - shut down the relaunch."
  []
  (kill!)
  (-main))
