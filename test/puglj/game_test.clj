(ns puglj.game-test
  (:require [clojure.test :refer :all]
            [puglj.util :refer :all]
            [puglj.game :refer :all]
            [puglj.lobby :as lobby]))

(def pool {:scout #{"a" "b"}
           :soldier #{"c" "d"}
           :pyro #{"e" "f"}
           :demoman #{"g" "h"}
           :heavy #{"i" "j"}
           :engineer #{"k" "l"}
           :medic #{"m" "n"}
           :sniper #{"o" "p"}
           :spy #{"q" "r"}})

(deftest pick-captains-test
  (testing "Can pick two captains"
    (is (= #{"a" "b"} (set (pick-captains ["a" "b"]))))))

(deftest pick-captains-test
  (testing "Can pick no more than two captains"
    (with-redefs [rand-nth (fn [coll] (first coll))]
      (is (= #{"a" "b"} (set (pick-captains ["a" "b" "c"])))))))

(deftest pick-captains-at-least-two-test
  (testing "Pick captains with one captain throws an error"
    (is (thrown? AssertionError (pick-captains ["a"])))))

;todo pick medic/demo captains over others?

(deftest pick-player-test
  (testing "Picking a player adds them to the team and removes them from the pool"
    (is (= {:pool (lobby/remove-player pool "a")
            :teams [{:scout "a"} {}]}
          (pick-player {:pool pool :teams [{} {}]} 0 "a" :scout)))))

(deftest pick-non-existent-player-test
  (testing "Picking a non-existent player throws an error"
    (is (thrown? AssertionError (pick-player {:pool pool :teams [{} {}]} 0 "z" :scout)))))

(deftest pick-non-existent-class-test
  (testing "Picking a non-existent class throws an error"
    (is (thrown? AssertionError (pick-player {:pool pool :teams [{} {}]} 0 "a" :booty)))))

;todo is this a useful assertion?
(deftest-pending pick-player-not-playing-a-class
  (testing "Picking a player on a class they're not playing throws an error"
    (is (thrown? AssertionError (pick-player {:pool pool :teams [{} {}]} 0 "a" :soldier)))))

(deftest complete-test
  (testing "Picking is complete when all classes are filled"
    (is (complete? {:pool {} :teams [{:scout "a"
                                      :soldier "c"
                                      :pyro "e"
                                      :demoman "g"
                                      :heavy "i"
                                      :engineer "k"
                                      :medic "m"
                                      :sniper "o"
                                      :spy "q"}
                                     {:scout "b"
                                      :soldier "d"
                                      :pyro "f"
                                      :demoman "h"
                                      :heavy "j"
                                      :engineer "l"
                                      :medic "n"
                                      :sniper "p"
                                      :spy "r"}]}))))

(deftest complete-false-if-missing-class
  (testing "Picking is not complete if a class is missing"
    (is (not (complete? {:pool {} :teams [{:soldier "c"
                                           :pyro "e"
                                           :demoman "g"
                                           :heavy "i"
                                           :engineer "k"
                                           :medic "m"
                                           :sniper "o"
                                           :spy "q"}
                                          {:scout "b"
                                           :soldier "d"
                                           :pyro "f"
                                           :demoman "h"
                                           :heavy "j"
                                           :engineer "l"
                                           :medic "n"
                                           :sniper "p"
                                           :spy "r"}]})))))