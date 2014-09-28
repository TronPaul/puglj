(ns puglj.lobby-test
  (:require [clojure.test :refer :all]
            [puglj.lobby :refer :all]))

(def nick "name")
(def other-nick "guy")

(deftest add-player-test
  (testing "Add a player to the pug"
    (is (= {:scout #{nick}} (add-player {} nick #{:scout})))))

(deftest add-player-with-multiple-classes-test
  (testing "Add a player with more than one class to the pug"
    (is (= {:scout #{nick} :soldier #{nick}}
          (add-player {} nick #{:scout :soldier})))))

(deftest add-player-to-pug-with-bad-class-test
  (testing "Add a player with a bad class to the pug"
    (is (thrown? AssertionError (add-player {} nick #{:abcd})))))

(deftest add-second-player-test
  (testing "Add a second player to the pug"
    (is (= {:scout #{nick} :soldier #{other-nick}}
          (add-player {:scout #{nick}} other-nick #{:soldier})))))

(deftest add-player-twice-test
  (testing "Readding a player will clear their previous classes"
    (is (= {:soldier #{nick}}
          (add-player {:scout #{nick}} nick #{:soldier})))))

(deftest remove-player-test
  (testing "Remove a player from the pug"
    (is (= {}
          (remove-player {:scout #{nick}} nick)))))

(deftest remove-player-in-multiple-classes
  (testing "Remove a player added as multiple classes from the pug"
    (is (= {}
          (remove-player {:scout #{nick} :soldier #{nick}} nick)))))

(deftest remove-player-safe-test
  (testing "Remove a player from the pug doesn't affect other players"
    (is (= {:scout #{other-nick}}
          (remove-player {:scout #{nick other-nick}} nick)))))

(deftest rename-player-test
  (testing "Rename a player in the pug"
    (is (= {:scout #{other-nick}}
          (rename-player {:scout #{nick}} nick other-nick)))))

(deftest rename-player-safe
  (testing "Renaming a non-existent player doesn't affect other players"
    (let [state {:scout #{nick}}]
      (is (= state (rename-player state other-nick "querty"))))))

(deftest basic-ready-test
  (testing "A game is ready"
    (is (ready? ["a" "b"] {:scout #{"a" "b"}
                           :soldier #{"c" "d"}
                           :pyro #{"e" "f"}
                           :demoman #{"g" "h"}
                           :heavy #{"i" "j"}
                           :engineer #{"k" "l"}
                           :medic #{"m" "n"}
                           :sniper #{"o" "p"}
                           :spy #{"q" "r"}}))))

(deftest ready-with-single-dupe-test
  (testing "Game is not ready when there are less than 18 players"
    (is (not (ready? ["a" "b"] {:scout #{"a" "b"}
                                :soldier #{"a" "d"}
                                :pyro #{"e" "f"}
                                :demoman #{"g" "h"}
                                :heavy #{"i" "j"}
                                :engineer #{"k" "l"}
                                :medic #{"m" "n"}
                                :sniper #{"o" "p"}
                                :spy #{"q" "r"}})))))

(deftest ready-with-complex-dupe-test
  ;todo better name
  (testing "Game is not ready when a player is duped"
    (is (not (ready? ["a" "b"] {:scout #{"a" "b"}
                                :soldier #{"a" "d"}
                                :pyro #{"e" "f"}
                                :demoman #{"g" "h"}
                                :heavy #{"i" "j"}
                                :engineer #{"k" "l"}
                                :medic #{"m" "n"}
                                :sniper #{"o" "p"}
                                :spy #{"q" "r" "s" "t"}})))))

(deftest ready-without-captains-test
  (testing "Game is not ready without 2 captains"
    (is (not (ready? ["a"] {:scout #{"a" "b"}
                            :soldier #{"c" "d"}
                            :pyro #{"e" "f"}
                            :demoman #{"g" "h"}
                            :heavy #{"i" "j"}
                            :engineer #{"k" "l"}
                            :medic #{"m" "n"}
                            :sniper #{"o" "p"}
                            :spy #{"q" "r"}})))))

(deftest need-test
  (testing "Need returns list requirements unmet to be ready"
    (is (= {:scout 2
            :soldier 2
            :pyro 2
            :demoman 2
            :heavy 2
            :engineer 2
            :medic 2
            :sniper 2
            :spy 2
            :captain 2} (need [] {})))))

(deftest need-ready-test
  (testing "Need returns an empty map if all requirements are met"
    (is (= {} (need ["a" "b"] {:scout #{"a" "b"}
                               :soldier #{"c" "d"}
                               :pyro #{"e" "f"}
                               :demoman #{"g" "h"}
                               :heavy #{"i" "j"}
                               :engineer #{"k" "l"}
                               :medic #{"m" "n"}
                               :sniper #{"o" "p"}
                               :spy #{"q" "r"}})))))

(deftest need-with-dupes-test
  (testing "Need handles duplicates correctly"
    (is (= {#{:scout :soldier} 1} (need ["a" "b"] {:scout #{"a" "b"}
                                                   :soldier #{"a" "d"}
                                                   :pyro #{"e" "f"}
                                                   :demoman #{"g" "h"}
                                                   :heavy #{"i" "j"}
                                                   :engineer #{"k" "l"}
                                                   :medic #{"m" "n"}
                                                   :sniper #{"o" "p"}
                                                   :spy #{"q" "r" "s" "t"}})))))

(deftest need-with-mulitple-dupes-test
  (testing "Need handles multiple duplicates correctly"
    (is (= {#{:scout :soldier} 1
            #{:soldier :pyro} 1} (need ["a" "b"] {:scout #{"a" "b"}
                                                  :soldier #{"a" "d"}
                                                  :pyro #{"d" "f"}
                                                  :demoman #{"g" "h"}
                                                  :heavy #{"i" "j"}
                                                  :engineer #{"k" "l"}
                                                  :medic #{"m" "n"}
                                                  :sniper #{"o" "p"}
                                                  :spy #{"q" "r" "s" "t"}})))))
