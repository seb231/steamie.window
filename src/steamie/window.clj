(ns steamie.window
  (:require [steamweb.core :as steam])
  (:gen-class))

(def steam-string-starter "7656119")

(def steam-URL "https://steamcommunity.com/profiles/")

(defn rand-4-digit-number [] ;;;; WIP ;;;;
  (+ (rand-int 1001) 9999))

(defn generate-steam-id-query [starter]
  (str starter (rand-4-digit-number) (rand-4-digit-number)))

(defn generate-steam-URL []
  (str steam-URL (generate-steam-id-query steam-string-starter) "/"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(comment 
  
  (def acrons-friends (steam/friend-list k (:id acron)))
  
  (def acrons-friends-ids (map :steamid (get-in acrons-friends [:friendslist :friends])))

  (def test-game-data (map #(steam/owned-games k %) acrons-friends-ids))

  (def first_example (map :appid (get-in (first foo) [:response :games])))

  (def second_example (map :appid (get-in (second foo) [:response :games])))

  (diff first_example second_example)
)
