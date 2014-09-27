(ns puglj.core-test
  (:require [clojure.test :refer :all]
            [puglj.core :refer :all]))

(def clean-state {:classes {}})

(def nick "name")
(def other-nick "guy")

(deftest add-player-test
  (testing "Add a player to the pug"
    (is (= {:scout #{nick}} (:classes (add-player clean-state nick #{:scout}))))))

(deftest add-player-with-multiple-classes-test
  (testing "Add a player with more than one class to the pug"
    (is (= {:scout #{nick} :soldier #{nick}}
          (:classes (add-player clean-state nick #{:scout :soldier}))))))

(deftest add-player-to-pug-with-bad-class-test
  (testing "Add a player with a bad class to the pug"
    (is (thrown? AssertionError (add-player clean-state nick #{:abcd})))))

(deftest add-second-player-test
  (testing "Add a second player to the pug"
    (is (= {:scout #{nick} :soldier #{other-nick}}
          (:classes (add-player {:classes {:scout #{nick}}} other-nick #{:soldier}))))))

(deftest add-player-twice-test
  (testing "Readding a player will clear their previous classes"
    (is (= {:soldier #{nick}}
          (:classes (add-player {:classes {:scout #{nick}}} nick #{:soldier}))))))

(deftest remove-player-test
  (testing "Remove a player from the pug"
    (is (= clean-state (remove-player {:classes {:scout #{nick}}} nick)))))

(deftest remove-player-in-multiple-classes
  (testing "Remove a player added as multiple classes from the pug"
    (is (= clean-state (remove-player {:classes {:scout #{nick} :soldier #{nick}}} nick)))))

(deftest remove-player-safe-test
  (testing "Remove a player from the pug doesn't affect other players"
    (is (= {:classes {:scout #{other-nick}}}
          (remove-player {:classes {:scout #{nick other-nick}}} nick)))))

(deftest rename-player-test
  (testing "Rename a player in the pug"
    (is (= {:classes {:scout #{other-nick}}}
          (rename-player {:classes {:scout #{nick}}} nick other-nick)))))

(deftest rename-player-safe
  (testing "Renaming a non-existent player doesn't affect other players"
    (let [state {:classes {:scout #{nick}}}]
      (is (= state (rename-player state other-nick "querty"))))))

(deftest basic-ready-test
  (testing "A game is ready"
    (is (ready? {:classes {:scout #{"a" "b"}
                           :soldier #{"c" "d"}
                           :pyro #{"e" "f"}
                           :demoman #{"g" "h"}
                           :heavy #{"i" "j"}
                           :engineer #{"k" "l"}
                           :medic #{"m" "n"}
                           :sniper #{"o" "p"}
                           :spy #{"q" "r"}}}))))

(deftest ready-with-single-dupe-test
  (testing "Game is not ready when there are less than 18 players"
    (is (not (ready? {:classes {:scout #{"a" "b"}
                           :soldier #{"a" "d"}
                           :pyro #{"e" "f"}
                           :demoman #{"g" "h"}
                           :heavy #{"i" "j"}
                           :engineer #{"k" "l"}
                           :medic #{"m" "n"}
                           :sniper #{"o" "p"}
                           :spy #{"q" "r"}}})))))

(deftest ready-with-complex-dupe-test
  ;todo better name
  (testing "Game is not ready when a player is duped"
    (is (not (ready? {:classes {:scout #{"a" "b"}
                                :soldier #{"a" "d"}
                                :pyro #{"e" "f"}
                                :demoman #{"g" "h"}
                                :heavy #{"i" "j"}
                                :engineer #{"k" "l"}
                                :medic #{"m" "n"}
                                :sniper #{"o" "p"}
                                :spy #{"q" "r" "s" "t"}}})))))