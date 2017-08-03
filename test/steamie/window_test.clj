(ns steamie.window-test
  (:require [clojure.test :refer :all]
            [steamie.window :refer :all]))

(def example-user-profile
  "this is example output of build-profile"
  '({:appid 10, :playtime_forever 65, :poisson {:upper 80.80202518666516, :lower 49.197974813334845}} {:appid 220, :playtime_forever 62, :poisson {:upper 77.43305543306315, :lower 46.566944566936854}} {:appid 240, :playtime_forever 994, :poisson {:upper 1055.794420460103, :lower 932.2055795398969}} {:appid 380, :playtime_forever 108, :poisson {:upper 128.36891749701, :lower 87.63108250299001}} {:appid 400, :playtime_forever 112, :poisson {:upper 132.74269027874638, :lower 91.2573097212536}} {:appid 7760, :playtime_forever 134, :poisson {:upper 156.68864032946885, :lower 111.31135967053116}} {:appid 500, :playtime_forever 1557, :poisson {:upper 1634.3393250552394, :lower 1479.6606749447606}} {:appid 550, :playtime_forever 910, :poisson {:upper 969.1257642656735, :lower 850.8742357343265}} {:appid 29180, :playtime_forever 120, :poisson {:upper 141.4707242542025, :lower 98.52927574579749}} {:appid 42910, :playtime_forever 1242, :poisson {:upper 1311.0743599318878, :lower 1172.9256400681122}} {:appid 620, :playtime_forever 520, :poisson {:upper 564.6948766638862, :lower 475.3051233361138}} {:appid 48000, :playtime_forever 264, :poisson {:upper 295.846230546173, :lower 232.15376945382704}} {:appid 207170, :playtime_forever 224, :poisson {:upper 253.3345939123077, :lower 194.6654060876923}} {:appid 211420, :playtime_forever 3974, :poisson {:upper 4097.5577532977995, :lower 3850.4422467022}} {:appid 730, :playtime_forever 23659, :poisson {:upper 23960.477054516592, :lower 23357.522945483408}} {:appid 204360, :playtime_forever 819, :poisson {:upper 875.0916250433164, :lower 762.9083749566836}} {:appid 200510, :playtime_forever 1467, :poisson {:upper 1542.0708145686458, :lower 1391.9291854313542}} {:appid 219640, :playtime_forever 2832, :poisson {:upper 2936.3044160138966, :lower 2727.6955839861034}} {:appid 219150, :playtime_forever 512, :poisson {:upper 556.3497373160203, :lower 467.65026268397975}} {:appid 203160, :playtime_forever 430, :poisson {:upper 470.64342505252233, :lower 389.35657494747767}}))

(def example-database
  "this is example output of collect-database"
  '({:76561197960375033 {:game_count 10, :games [{:appid 10, :playtime_forever 0} {:appid 20, :playtime_forever 0} {:appid 30, :playtime_forever 0} {:appid 40, :playtime_forever 0} {:appid 50, :playtime_forever 64} {:appid 60, :playtime_forever 0} {:appid 70, :playtime_forever 0} {:appid 130, :playtime_forever 0} {:appid 220, :playtime_forever 430} {:appid 240, :playtime_forever 6}]}} {:76561197960667429 {:game_count 10, :games [{:appid 10, :playtime_forever 13} {:appid 20, :playtime_forever 0} {:appid 30, :playtime_forever 0} {:appid 40, :playtime_forever 0} {:appid 50, :playtime_forever 0} {:appid 60, :playtime_forever 0} {:appid 70, :playtime_forever 1} {:appid 130, :playtime_forever 0} {:appid 80, :playtime_forever 0} {:appid 100, :playtime_forever 0}]}} {:76561197969555013 {:game_count 10, :games [{:appid 10, :playtime_forever 0} {:appid 20, :playtime_forever 0} {:appid 30, :playtime_forever 0} {:appid 40, :playtime_forever 0} {:appid 50, :playtime_forever 64} {:appid 60, :playtime_forever 0} {:appid 70, :playtime_forever 0} {:appid 130, :playtime_forever 0} {:appid 220, :playtime_forever 430} {:appid 240, :playtime_forever 6}]}}))

(deftest get-games-out-db-test
  ""
  (= '(10 20 30 40 50 60 70 130 220 240)
     (get-games-out-db (first example-database))))

(deftest search-game-test
  ""
  (is (= true (search-game 10 '(10 20 30)))))

(deftest match-game-test
  ""
  (is (= '(:76561197960375033) (match-game 10 (first example-database)))))

(deftest users-with-matching-game-test
  ""
  (is (= [:76561197960375033 :76561197960667429 :76561197969555013]
         (users-with-matching-game 10 example-database))))

(deftest search-for-matching-games-test
  (let [result (search-for-matching-games example-user-profile example-database)]
    ""
    (is (= 3 (count result)))
    ""
    (is (= 3 (count (first (vals (first result))))))))
