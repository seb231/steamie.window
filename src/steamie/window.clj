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

(defn in? [coll v]
  (true? (some #(= v %) coll)))

(defn match-game [appid user]
  (if (in? (map :appid (get-games-out-db user)) appid)
    (hash-map (first (keys user))
              (dissoc (first (filter #(= (:appid %) appid) (get-games-out-db user))) :appid))))

(defn assoc-games-with-user [v m]
  (let [user (first (keys m))]
    (assoc-in m [user :games] v)))

(defn key-nil? [x]
  (-> x
      keys
      first
      not-nil?))

(defn match-time [user distribution]
  (let [upper (:upper distribution)
        lower (:lower distribution)]
    (if (is-number-in-range? (:time (first (vals user)))
                             lower
                             upper)
      user)))

(defn filter-if-not-nil->> [x]
  (filter not-nil? x))

(defn vec-to-map [[k v]]
  (hash-map k v))

(defn filter-by-playtime [profile users]
  (filter #(map (fn [x] (-> (:appid x)
                            (match-game %)
                            (match-time (:poisson x)))) profile) users))

;;; Here is where processing is slow
;;; this function searches 5000+ users for every game in the profile
;;; in this case that is 38 games
;;; this may because of how I am storing the data (repeatedly in vectors)
(defn users-with-matching-game [app users]
  (filterv key-nil? (map #(let [user (first (first %))
                                games (mapv :appid (get-games-out-db %))
                                result (->> %
                                            (filter (fn [x] (match-game (:appid app) x)))


                                            (match-time app)


                                            (assoc-games-with-user games))
                                ]
                            (if (not-nil? result)
                              result))
                         users)))

(defn search-for-matching-games [user-profile user-db]
  (let [search-results (map #(hash-map (keyword (str (:appid %)))
                                       (users-with-matching-game % user-db))
                            user-profile)]
    (filterv #(if ((complement empty?) (first (vals %))) %) search-results)))

(defn return-vals [x]
  (-> x
      vals
      first))

(defn collate-games [user-games]
  (reduce into []
          (map #(reduce into []
                        (map :games
                             (map return-vals (return-vals %)))) user-games)))

;;; TODO
;;; popularity is too naive to indicate interesting new game suggestions
;;; suggest building profile for every user, working out which games
;;; they spend the most time playing, return these (minus games
;;; the original profile already have)
(defn count-occurrence [x list]
  (->> list
       (filter #{x})
       count))

(defn sort-map-by-val [m]
  (into (sorted-map-by (fn [key1 key2]
                         (compare [(get m key2) key2]
                                  [(get m key1) key1])))
        m))

(defn take-n-into-map [n m]
  (->> m
       (take n)
       (into {})))

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
        unique-games (->> (map #(map :appid (get-games-out-db %)) all-matching)
                          (reduce into [])
                          distinct
                          sort
                          (filterv #((complement own-game?) % profile-game-list)))
        _ (println (str "your top " n " games are..."))]
    (take n unique-games)))

(comment
  "run like"
  (-main "STEAM_API_KEY" (System/getenv "ACRON") 10))
