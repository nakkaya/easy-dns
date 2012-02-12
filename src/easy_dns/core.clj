;;http://support.easydns.com/tutorials/dynamicUpdateSpecs.php
;;https://cp.easydns.com/support/dynamic/

(ns easy-dns.core
  (:gen-class)
  (:use [clojure.contrib command-line])
  (:require [clj-http.client :as client]))

(defn log [& args]
  (apply println "[+]" args))

(defn ip []
  (let [s (:body (client/get "http://checkip.dyndns.org/"))]
    (second (re-find #"Address: (.*?)<" s))))

(defn update-dns [url creds ip] 
  (let [response (:body (client/get (str url ip) {:basic-auth creds}))
        stat (first (.split response "\\s"))]
    (if (= stat "NOERROR")
      (log "DNS Update" ip)
      (do (log "Error" stat "will retry in 5 mins")
          (Thread/sleep (* 1000 60 5))
          (update-dns url creds ip)))))

(defn track-ip [interval url creds curr-ip]
  (while true
    (let [ip (ip)]
      (when (not= ip @curr-ip)
        (update-dns url creds ip)
        (swap! curr-ip (fn [_] (identity ip))))
      (Thread/sleep (* 1000 60 interval)))))

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
          url (str "https://members.easydns.com/dyn/dyndns.php?hostname=" host "&myip=")
          curr-ip (atom (ip))]
      (update-dns url creds @curr-ip)
      (when (nil? once?)
        (track-ip interval url creds curr-ip))
      (log "Bye"))))