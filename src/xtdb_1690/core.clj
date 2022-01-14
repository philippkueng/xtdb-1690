(ns xtdb-1690.core
  (:require [xtdb.api :as xt]
            [clojure.java.io :as io]))

(defn- uuid [] (java.util.UUID/randomUUID))

(def doc {:name        "Max"
          :hours-slept (map (fn [i]
                              {:date  "2022-01-14"
                               :hours 6.3})
                            (range 5500))})

(defn start-xtdb! []
  (letfn [(kv-store [dir]
            {:kv-store {:xtdb/module 'xtdb.rocksdb/->kv-store
                        :db-dir      (io/file dir)
                        :sync?       true}})]
    (xt/start-node
      {:xtdb/tx-log         (kv-store "data/dev/tx-log")
       :xtdb/document-store (kv-store "data/dev/doc-store")
       :xtdb/index-store    (kv-store "data/dev/index-store")})))

(def xtdb-node (start-xtdb!))

(time
  (xt/q (xt/db xtdb-node) '{:find  [(count ?e)]
                            :where [[?e :name "Max"]]}))

(time
  (doall
    (doseq [i (range (* 50 1000))]
      (xt/submit-tx xtdb-node [[::xt/put (merge doc {:xt/id (uuid)})]]))))

;(defn stop-xtdb! []
;  (.close xtdb-node))