(ns steamie.window
  (:gen-class))

(def seb "https://steamcommunity.com/profiles/76561198076420895/")

(def acron {:name "https://steamcommunity.com/id/acron"
            :number "https://steamcommunity.com/profiles/76561197960375033/"})

(def seventeen-string-starter "7656119")

(def steam-URL "https://steamcommunity.com/profiles/")

(defn rand-5-str-number [] ;;;; WIP ;;;;
  (+ (rand-int 10001) 99999))

(defn generate-steam-id-query [starter]
  (str starter (rand-5-str-number) (rand-5-str-number)))

(defn generate-steam-URL []
  (str steam-URL (generate-steam-id-query seventeen-string-starter) "/"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
