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

(defn shared-games [seq1 seq2] (set/intersection (set seq1) (set seq2)))

(defn filter-by-shared-games [shared-games owned-games]
  (filter #(= shared-games (:appid %)) (get-games owned-games)))

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
  (->> (steam/owned-games key id)
       get-games
       (remove (comp #(< % 60) :playtime_forever))
       (map #(select-keys % [:appid :playtime_forever]))
       (map #(assoc % :poisson (poisson-di (:playtime_forever %))))))

;; for each game in the profile search the database for users
