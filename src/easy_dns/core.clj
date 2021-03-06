;;http://support.easydns.com/tutorials/dynamicUpdateSpecs.php
;;https://cp.easydns.com/support/dynamic/

(ns easy-dns.core
  (:gen-class)
  (:use [clojure.tools.cli])
  (:require [clj-http.client :as client]))

(defn log [& args]
  (print "[+]" (str (java.util.Date.)) "- ")
  (apply println args))

(defn update-dns [url creds] 
  (let [response (:body (client/get (str url "1.1.1.1") {:basic-auth creds}))
        stat (first (.split response "\\s"))]
    (if (= stat "NOERROR")
      (log "DNS Update")
      (do (log "Error" stat "will retry in 5 mins")
          (Thread/sleep (* 1000 60 5))
          (update-dns url creds)))))

(defn -main [& args]
  (let [[opts _ banner] (cli args
                             ["--host" "Full hostname being updated"]
                             ["--user" "Username"]
                             ["--token" "Token"]
                             ["--interval" "Update interval (mins)" :default 15 :parse-fn #(Integer. %)]
                             ["--once" "Update once" :default false :flag true]
                             ["--help" "Show help" :default false :flag true])
        {:keys [host user token interval once help]} opts]

    (when (or help
              (some true? (map nil? [host user token])))
      (println "EasyDNS Updater")
      (println banner)
      (System/exit 0))

    (let [creds [user token]
          url (str "https://members.easydns.com/dyn/dyndns.php?hostname=" host "&myip=")]
      (if once
        (update-dns url creds)
        (while true
          (try
            (update-dns url creds)
            (catch Exception e
              (log "Conn Error"))
            (finally
             (Thread/sleep (* 1000 60 interval))))))
      (log "Bye"))))