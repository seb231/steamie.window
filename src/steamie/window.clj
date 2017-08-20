(ns steamie.window
  (:require [steamweb.core :as steam]
            [clojure.set :as set]
            [steamie.database :refer :all]))

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

(defn own-game? [appid list-games]
  ((complement nil?) (some #{appid} list-games)))

(defn match-game [appid user-db]
  (if (own-game? appid (map :appid (get-games-out-db user-db)))
    (hash-map (first (keys user-db))
              (dissoc (first (filter #(= (:appid %) appid) (get-games-out-db user-db))) :appid))))

(defn assoc-games-with-user [v m]
  (let [user (first (keys m))]
    (assoc-in m [user :games] v)))

(defn not-nil? [x]
  ((complement nil?) x))

(defn key-nil? [x]
  (-> x
      keys
      first
      not-nil?))

(defn match-time [distribution user]
  (let [upper (get-in distribution [:poisson :upper])
        lower (get-in distribution [:poisson :lower])]
    (if (not-nil? user)
      (if (is-number-in-range? (:time (first (vals user)))
                               lower
                               upper)
        user))))

(defn users-with-matching-game [app users]
  (filterv key-nil? (map #(let [user (first (first %))
                                games (mapv :appid (get-games-out-db %))
                                result (->> %
                                            (match-game (:appid app))
                                            (match-time app)
                                            (assoc-games-with-user games))]
                            (if (not-nil? result)
                              result))
                         users)))

;; TODO
;; 1. need to use game-list to return a users games which are not in this list
;; another function following search?

(defn search-for-matching-games [user-profile game-list user-db]
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

(defn -main [k user]
  (let [profile (build-profile k user)
        profile-game-list (mapv :appid profile)
        database (build-database k user)
        all-matching-db-games (-> (search-for-matching-games profile profile-game-list database)
                                  collate-games
                                  distinct
                                  sort
                                  vec)]
    (filterv #((complement own-game?) % profile-game-list) all-matching-db-games)))

(comment

  "run like"
  (-main "STEAM_API_KEY" (System/getenv "ACRON")))
