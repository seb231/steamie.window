(ns steamie.database
  (:require [steamweb.core :as steam]
            [clojure.set :as set]))

(def config (read-string (slurp (str "config.edn"))))

(def k (:api-key configq)) ;; need to move this to env var

(def acron-id (:id (:acron config)))

(defn get-friends-list [key steam-id]
  (map :steamid (get-in (steam/friend-list key steam-id) [:friendslist :friends])))

(defn database-collection
  [k id]
  (let [init (get-friends-list k id)]
    (->> init
         (mapcat #(get-friends-list k %))
         distinct)))

(defn collect-database
  "Collect a list of users"
  [k starting-id]
  (let [first-pass (database-collection k starting-id)]
    (->> first-pass
         (mapcat #(database-collection k %))
         distinct)))

(defn build-database
  [k users]
  (map #(-> (steam/owned-games k %)
            (set/rename-keys  {:response (keyword %)}))
       users))
