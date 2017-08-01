(ns steamie.database
  (:require [steamweb.core :as steam]
            [clojure.set :as set]
            [criterium.core :as crit]))

(defn get-friends-list [api-key steam-id]
  (map :steamid (get-in (steam/friend-list (System/getenv api-key) steam-id) [:friendslist :friends])))

(defn collect-users
  "Collect a list of users"
  [api-key starting-id]
  (let [init (get-friends-list api-key starting-id)
        first-pass (->> init
                        (mapcat #(get-friends-list api-key %))
                        distinct)]
    first-pass
    #_(->> first-pass
           (mapcat #(database-collection k %))
           distinct)))

(defn collect-database
  [k user-list]
  (map #(-> (steam/owned-games (System/getenv k) %)
            (set/rename-keys  {:response (keyword %)}))
       user-list))

(defn build-database
  [k starting-id]
  (let [user-list (collect-users k starting-id)]
    (collect-database k user-list)))
