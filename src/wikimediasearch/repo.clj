(ns wikimediasearch.repo
  (:require
   [taoensso.timbre :as log :refer (trace debug info warn error report)]
   [clojure.xml :as xml]
   [clojure.java.io :as io]
   [clojure.zip :as zip]
   [clojure.data.zip.xml :as zip-xml]
   [clojure.string :as string]
   [clojure.core.reducers :as r]
   )
  (:use
   [clojure.pprint]
   [wikimediasearch.util]
   [liberator.core :only [defresource]]
   [clj-tokenizer.core]
   ))


;;To save some redundant text on the index
(def URL_PREFIX "http://en.wikipedia.org/wiki/")

(defn parse
  "Returns the parsed xml file as Clojure map"
  [filename]
  (-> filename
      ;;       io/resource
      io/file
      xml/parse
      zip/xml-zip))


;; (defn filter-doc
;;   "Returns a tuple the slug and a map with the abstract & title ignoring everything else. If any of the fields are empty it returns nil"
;;   [doc]
;;   (let [url       (zip-xml/xml1-> doc :url zip-xml/text)
;;         title     (zip-xml/xml1-> doc :title zip-xml/text)
;;         abstract  (zip-xml/xml1-> doc :abstract zip-xml/text)
;;         entry-key (string/replace url URL_PREFIX "")]
;;     (if (some empty? [url title abstract])
;;       nil
;;       [entry-key (assoc {} :title title
;;                    :abstract abstract)])))


(defn filter-docs
  "Builds a hash-map with the abstract & title indexed by the url path after URL_PREFIX."
  [file]
  (into (hash-map)
        (for [doc (zip-xml/xml-> file :doc)]
          (let [url       (zip-xml/xml1-> doc :url zip-xml/text)
                title     (zip-xml/xml1-> doc :title zip-xml/text)
                abstract  (zip-xml/xml1-> doc :abstract zip-xml/text)
                entry-key (string/replace url URL_PREFIX "")]
            (if (some empty? [url title abstract])
              nil
              [entry-key (assoc {} :title title
                           :abstract abstract)])))))


(defn add-val-in-map
  "Cons a lazy sequence with the given value for the proper key entry in the map"
  [myvalue current-index-map mykey]
  (update-in current-index-map [mykey] #(lazy-seq (cons %2 %1)) myvalue))


(defn index-doc!
  "Build a map indexed by word pointing to a sequence of the urls where it appears per the given doc"
  [current-dict url doc]
  (let [text  (str (:title doc) (:abstract doc))
        tokens (distinct (token-seq (token-stream text)))
        add-word-appearance (partial add-val-in-map url)]
    (reduce add-word-appearance current-dict tokens)))


(defn index-docs
  "Build a single map indexed by word pointing to a sequence of the urls where it appears on all the documents of the given docs"
  [dict]
  (reduce-kv index-doc! (hash-map) dict))


(defn store-n-index!
  "Returns a tuple with two maps: the docs and it's correspondant index by word"
  [filename]
  (let [docs (timed (filter-docs (parse filename)))
        index (timed (index-docs docs))]
    (def DOCS-AGENT docs)
    (def INDEX-AGENT index)
    [docs index]))


(defn search
  "Returns a map's vector with the docs that contains matches for the given word"
  [w]
  (let [slugs (get INDEX-AGENT w)]
    (map #(let [doc (get DOCS-AGENT %1)
                url (str URL_PREFIX %1)]
            (assoc doc :url url)) slugs)))

;;     (for [slug slugs
;;                 ]]

