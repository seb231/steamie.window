(ns steamie.window.crawl
  (:require [clojure.core.async :as async]
            [environ.core :refer [env]]
            [steamweb.core :as steam]))

(def in-chan (async/chan 100))
(def steam-api-key (env :steam-api-key))
(def starting-steamid "76561197960375033")
(def all-steamid-list (atom #{}))
(def public-steamid-list (atom #{}))

(defn process-id
  [ch id]
  (swap! all-steamid-list conj id)
  (when-let [r (steam/friend-list steam-api-key id)]
    (swap! public-steamid-list conj id)
    (let [friends (map :steamid (-> r :friendslist :friends))]
      (run! (fn [i]
              (when-not (contains? @all-steamid-list i)
                (async/put! ch i)))
            friends))))

(defn run
  []
  (async/go-loop []
    (when-let [id (async/<! in-chan)]
      (async/go
        (process-id in-chan id))
      (recur)))
  (async/put! in-chan starting-steamid))
