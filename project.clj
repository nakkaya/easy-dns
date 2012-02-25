(defproject easy-dns "1.0.0-SNAPSHOT"
  :description "EasyDNS Dynamic DNS Updater"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/tools.cli "0.2.1"]
                 [clj-http "0.1.3"]]
  :jvm-opts ["-Xmx16m"]
  :main easy-dns.core)
