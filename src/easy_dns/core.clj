;;http://support.easydns.com/tutorials/dynamicUpdateSpecs.php
;;https://cp.easydns.com/support/dynamic/

(ns easy-dns.core
  (:gen-class)
  (:use [clojure.contrib command-line])
  (:require [clj-http.client :as client]))

(defn log [& args]
  (apply println "[+]" args))

(defn update-dns [url creds] 
  (let [response (:body (client/get (str url "1.1.1.1") {:basic-auth creds}))
        stat (first (.split response "\\s"))]
    (if (= stat "NOERROR")
      (log "DNS Update")
      (do (log "Error" stat "will retry in 5 mins")
          (Thread/sleep (* 1000 60 5))
          (update-dns url creds)))))

(defn -main [& args]
  (with-command-line args
    "EasyDNS Updater"
    [[host "Full hostname being updated"]
     [user "Username"]
     [token "Token"]
     [interval "Update interval (mins)" "15"]
     [once? "Update once" false]]
    (let [interval (read-string interval)
          creds [user token]
          url (str "https://members.easydns.com/dyn/dyndns.php?hostname=" host "&myip=")]
      (update-dns url creds)
      (when (nil? once?)
        (while true
          (Thread/sleep (* 1000 60 interval))
          (update-dns url creds)))
      (log "Bye"))))