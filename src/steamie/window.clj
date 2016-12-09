(ns steamie.window
  (:require [steamweb.core :as steam]
            [clojure.set :as set]))

(def config (read-string (slurp (str "config.edn"))))

(def k (:api-key config))

(def acron-id (:id (:acron config)))

(defn get-games [owned-games]
  (get-in owned-games [:response :games]))

(defn get-games-list [owned-games]
  (map :appid (get-games owned-games)))

(defn shared-games [seq1 seq2] (clojure.set/intersection (set seq1) (set seq2)))

(defn filter-by-shared-games [shared-games owned-games]
  (filter #(= shared-games (:appid %)) (get-games owned-games)))

(defn poisson-sd [mean]
  (Math/sqrt mean))

(defn poisson-ci [poisson-distribution]
  (* 1.96 poisson-distribution))

(defn poisson-di [mean]
  (let [sd (poisson-sd mean)
        ci (poisson-ci sd)
        _ (prn ci)]
    (hash-map :lower (- mean ci)
              :upper (+ mean ci))))

(defn -main []
  (let [acrons-friends-ids (get-friends-list acron-id)
        acrons-friends-game-data (map #(steam/owned-games k %) acrons-friends-ids)
        friend-count (count acrons-friends-game-data)
        rand-friend-1 (nth acrons-friends-game-data (rand-int (dec friend-count)))
        rand-friend-2 (nth acrons-friends-game-data (rand-int (dec friend-count)))
        comparison (shared-games (get-games-list rand-friend-1) (get-games-list rand-friend-2))]
    (hash-map :rand-friend-1 (map #(filter-by-shared-games % rand-friend-1) comparison)
              :rand-friend-2 (map #(filter-by-shared-games % rand-friend-2) comparison))))

;;; Need to do this to both friends and then compare the playtime in some way
;;; need to flatten maps of games per user
;;; if hours played is 5 or less than need to not use poisson and just bin them together
