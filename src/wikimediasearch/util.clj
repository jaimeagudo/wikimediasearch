(ns wikimediasearch.util
  (:require
   [taoensso.timbre :as log :refer (trace debug info warn error report)]
   [clojure.string :as string]
   [clojure.pprint :as pprint]
   ))


(defn handle-exception
  "Log exceptions in Liberator at ERROR level."
  [ctx]
  (let [e (:exception ctx)]
    (log/error e "Liberator caught" (.getClass e) "message:" (.getMessage e))))


(defn pformat
  "Returns "
  [& args]
  (with-out-str
    (apply pprint/pprint args)))

(defmacro timed
  "Prints the execution time of the given expression"
  [expr]
  (let [sym (= (type expr) clojure.lang.Symbol)]
    `(let [start# (. System (nanoTime))
           return# ~expr
           res# (if ~sym
                  (resolve '~expr)
                  (resolve (first '~expr)))]
       (prn (str "Timed "
                 (:name (meta res#))
                 ": " (/ (double (- (. System (nanoTime)) start#)) 1000000.0) " msecs"))
       return#)))



(defn usage [name options-summary]
  (->> [name
        " index REST API."
        ""
        (str "Usage: " name " [options]. All the options are optional and take default values")
        ""
        "Options:"
        options-summary]
       (string/join \newline)))


(defn exit
  "Do house-keeping upon exit"
  ([status]
   (shutdown-agents) ;;   Shutdown timbre agents to avoid delayed process termination
   (System/exit status))
  ([status msg]
   (info msg)
   (exit status)))

