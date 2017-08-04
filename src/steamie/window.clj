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
  ((complement nil?) (some #{appid} (map :appid list-games))))

(defn match-game [appid user-db]
  (if (own-game? appid (get-games-out-db user-db))
    (hash-map (first (keys user-db))
              (dissoc (first (filter #(= (:appid %) appid) (get-games-out-db user-db))) :appid))))

(defn not-nil? [x]
  ((complement nil?) x))

(defn match-time [distribution user]
  (let [upper (get-in distribution [:poisson :upper])
        lower (get-in distribution [:poisson :lower])]
    (if (not-nil? user)
      (if (is-number-in-range? (:time (first (vals user)))
                               lower
                               upper)
        user))))

(defn users-with-matching-game [app users]
  (filter not-nil? (map #(let [user (first (first %))
                               games (mapv :appid (get-games-out-db %))
                               result (->> %
                                           (match-game (:appid app))
                                           (match-time app))]
                           (if (not-nil? result)
                             (assoc result :games games)))
                        users)))
;; TODO need to use game-list to return a users games which are not in this list
(defn search-for-matching-games [user-profile game-list user-db]
  (let [search-results (map #(hash-map (keyword (str (:appid %)))
                                       (users-with-matching-game % user-db))
                            user-profile)]
    (filterv #(if ((complement empty?) (first (vals %))) %) search-results)))

(defn -main [k user]
  (let [profile (build-profile k user)
        profile-game-list (mapv :appid profile)
        database (build-database k user)]
    (search-for-matching-games profile profile-game-list database)))

(comment

  "run like"
  (-main "STEAM_API_KEY" (System/getenv "ACRON")))

;; TODO
;; -main currently returns a list of users who have played the game of the profiled
;; user for roughly the same amount of time
;; What it needs to do is return a list of games shared by though users, that the
;; profiled user doesn't own
