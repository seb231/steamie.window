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

(defn build-profile
  [key id]
  (->> (steam/owned-games (System/getenv key) id)
       get-games
       (remove (comp #(< % 60) :playtime_forever))
       (map #(select-keys % [:appid :playtime_forever]))
       (map #(assoc % :poisson (poisson-di (:playtime_forever %))))))

(defn get-games-out-db [user-db]
  (map #(:appid %) (:games (first (vals user-db)))))

(defn search-game [appid list-games]
  ((complement nil?) (some #{appid} list-games)))

(defn match-game [appid user-db]
  (if (search-game appid (get-games-out-db user-db))
    (keys user-db)))

;;; if null returns an empty vector, need to drop
(defn users-with-matching-game [appid users]
  (reduce into [] (map #(match-game appid %) users)))

(defn search-for-matching-games [user-profile user-db]
  (let [list-of-games (map #(:appid %) user-profile)]
    (map (fn [appid] (hash-map (keyword (str appid))
                               (users-with-matching-game appid user-db)))
         list-of-games)))

(defn -main [k user]
  (let [profile (build-profile k user)
        database (build-database k user)]
    (search-for-matching-games profile database)))

(comment

  "run like"
  (-main "STEAM_API_KEY" (System/getenv "ACRON")))

;;; TODO
;;; rather than doing another search, I may also want do the playtime comparison at the same time
;;; so a success looks matching playtime rather than just same games shared
;;; this will reduce the number of times I have to search over the database
;;; so maybe in the search the playtime of the user-profile needs to be included with the appid
;;; once we have the map of games with players with similar playtimes then there needs to maybe
;;; be second search (could this be reduced by keeping all this info somehwere?) whereby all the
;;; games not played by the user-profile are concanenated but which are shared in the database.
;;; This could be the list of games returned to the user.
