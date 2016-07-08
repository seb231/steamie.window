(ns steamie.window
  (:require [steamweb.core :as steam])
  (:gen-class))

(def steam-string-starter "7656119")

(def steam-URL "https://steamcommunity.com/profiles/")

(def config (read-string (slurp (str "config.edn"))))

(def k (:api-key config))

(def acron-id (:id (:acron config)))

(defn rand-4-digit-number [] ;;;; WIP ;;;;
  (+ (rand-int 1001) 9999))

(defn generate-steam-id-query [starter]
  (str starter (rand-4-digit-number) (rand-4-digit-number)))

(defn generate-steam-URL []
  (str steam-URL (generate-steam-id-query steam-string-starter) "/"))

(defn get-friends-list [steam-id]
  (map :steamid (get-in (steam/friend-list k steam-id) [:friendslist :friends])))

(defn get-games [owned-games]
  (get-in owned-games [:response :games]))

(defn get-games-list [owned-games]
  (map :appid (get-games owned-games)))

(defn shared-games [seq1 seq2] (clojure.set/intersection (set seq1) (set seq2)))

(defn filter-by-shared-games [shared-games owned-games]
  (filter #(= shared-games (:appid %)) (get-games owned-games)))

(defn -main []
  (let [acrons-friends-ids (get-friends-list acron-id)
        acrons-friends-game-data (map #(steam/owned-games k %) acrons-friends-ids)
        friend-count (count acrons-friends-game-data)
        rand-friend-1 (nth acrons-friends-game-data (rand-int (dec friend-count)))
        rand-friend-2 (nth acrons-friends-game-data (rand-int (dec friend-count)))]
    (->> (shared-games (get-games-list rand-friend-1) (get-games-list rand-friend-2))
         (map #(filter-by-shared-games % rand-friend-2)))))

;;; Need to do this to both friends and then compare the playtime in some way
