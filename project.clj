(defproject wikimediasearch "0.0.2-SNAPSHOT"
  :description "A sample project to show Clojure skills and benchmark Liberator performance"
  :url "https://github.com/jaimeagudo/wikimediasearch"
  :jvm-opts ^:replace  ["-Xms3G"
                        "-Xmx3G"
                        "-XX:+AggressiveOpts"
                        "-XX:+UseCompressedOops"
                        "-XX:+UnlockDiagnosticVMOptions"
                        "-XX:+DebugNonSafepoints"
                        "-XX:+UnlockCommercialFeatures"
                        "-XX:+FlightRecorder"
                        "-XX:StartFlightRecording=delay=60s,duration=60s,filename=wiki_flight_record.jfr"
                        ]
  :main wikimediasearch.core
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.3.4" :exclusions [org.clojure/tools.macro]]
                 [liberator "0.13"]
                 [com.taoensso/timbre "4.0.2"]
                 [http-kit "2.1.19"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [ring/ring-devel "1.3.2"]
                 [io.aviso/pretty "0.1.17"]
                 [bouncer "0.3.3"]
                 [org.clojure/data.zip "0.1.1"]
                 [pandect "0.5.2"]
                 [cheshire "5.5.0"]
                 [criterium "0.4.3"]
                 [clj-tokenizer "0.1.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/tools.macro "0.1.5"]
                 ]
  :profiles {:dev {:dependencies [[midje "1.6.3"]
                                  [clj-stacktrace "0.2.8"]
                                  [ring/ring-mock "0.2.0"]]}}
  :repl-options {:init-ns wikimediasearch.core }
  :aliases {"test" ["midje"] }
  )

