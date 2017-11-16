(ns steamie.window
  (:require [steamweb.core :as steam]
            [clojure.set :as set]
            [steamie.database :refer :all])
  (:gen-class))

(defn get-games [owned-games]
  (get-in owned-games [:response :games]))

(defn poisson-sd [mean]
  (Math/sqrt mean))

(defn poisson-ci [poisson-distribution]
  (* 1.96 poisson-distribution))

(defn poisson-di [mean]
  (let [sd (poisson-sd mean)
        ci (poisson-ci sd)]
    (hash-map :lower (- mean ci)
              :upper (+ mean ci))))

(defn is-number-in-range? [n lower upper]
  (cond (<= lower n)
        (>= upper n)
        :else false))

(defn build-profile
  [key id]
  (->> (steam/owned-games (System/getenv key) id)
       get-games
       (remove (comp #(< % 60) :playtime_forever))
       (map #(select-keys % [:appid :playtime_forever]))
       (map #(assoc % :poisson (poisson-di (:playtime_forever %))))))

(defn get-games-out-db [user-db]
  (map #(hash-map :appid (:appid %)
                  :time (:playtime_forever %))
       (:games (first (vals user-db)))))

(defn not-nil? [x]
  ((complement nil?) x))

(defn own-game? [appid list-games]
  (not-nil? (some #{appid} list-games)))

(defn in-coll? [coll v]
  (true? (some #(= v %) coll)))

(defn match-game [appid user]
  (if (in-coll? (map :appid (get-games-out-db user)) appid)
    (hash-map (first (keys user))
              (dissoc (first (filter #(= (:appid %) appid) (get-games-out-db user))) :appid))))

(defn match-time [user distribution]
  (let [upper (:upper distribution)
        lower (:lower distribution)]
    (if (is-number-in-range? (:time (first (vals user)))
                             lower
                             upper)
      user)))

(defn filter-by-playtime [profile users]
  (filter #(map (fn [x] (-> (:appid x)
                            (match-game %)
                            (match-time (:poisson x)))) profile) users))

(defn sort-by-playtime [user]
  (sort-by :playtime_forever > (:games (first (vals user)))))

;;; TODO
;;; turn into cmd line tool?
(defn -main [k user n]
  (let [profile (build-profile k user)
        _ (println "profile loaded!")
        profile-game-list (mapv :appid profile)
        _ (println "game list retrieved!")
        _ (println "collecting database...")
        database (build-database k user)
        _ (println "database built!")
        _ (println "matching games...")
        all-matching (filter-by-playtime profile database)
        _ (println "games matched!")
        unique-games (->> (mapcat (fn [games] (map :appid (->> games
                                                               sort-by-playtime
                                                               (filter #(< 0 (:playtime_forever %)))
                                                               (take 5))))
                                  all-matching)
                          distinct
                          (filter #((complement own-game?) % profile-game-list)))
        _ (println (str "your top " n " games are..."))]
    (take n unique-games)))

(comment
  "run like"
  (-main "STEAM_API_KEY" (System/getenv "ACRON") 10))
