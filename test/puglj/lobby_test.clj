(ns puglj.lobby-test
  (:require [clojure.test :refer :all]
            [puglj.lobby :refer :all]))

(def user-id 1)
(def other-user-id 2)

(deftest add-player-test
  (testing "Add a player to the pug"
    (is (= {:scout #{user-id}} (add-player {} user-id #{:scout})))))

(deftest add-player-with-multiple-classes-test
  (testing "Add a player with more than one class to the pug"
    (is (= {:scout #{user-id} :soldier #{user-id}}
          (add-player {} user-id #{:scout :soldier})))))

(deftest add-player-to-pug-with-bad-class-test
  (testing "Add a player with a bad class to the pug"
    (is (thrown? AssertionError (add-player {} user-id #{:abcd})))))

(deftest add-second-player-test
  (testing "Add a second player to the pug"
    (is (= {:scout #{user-id} :soldier #{other-user-id}}
          (add-player {:scout #{user-id}} other-user-id #{:soldier})))))

(deftest add-player-twice-test
  (testing "Readding a player will clear their previous classes"
    (is (= {:soldier #{user-id}}
          (add-player {:scout #{user-id}} user-id #{:soldier})))))

(deftest remove-player-test
  (testing "Remove a player from the pug"
    (is (= {}
          (remove-player {:scout #{user-id}} user-id)))))

(deftest remove-player-in-multiple-classes
  (testing "Remove a player added as multiple classes from the pug"
    (is (= {}
          (remove-player {:scout #{user-id} :soldier #{user-id}} user-id)))))

(deftest remove-player-safe-test
  (testing "Remove a player from the pug doesn't affect other players"
    (is (= {:scout #{other-user-id}}
          (remove-player {:scout #{user-id other-user-id}} user-id)))))

(deftest rename-player-test
  (testing "Rename a player in the pug"
    (is (= {:scout #{other-user-id}}
          (rename-player {:scout #{user-id}} user-id other-user-id)))))

(deftest rename-player-safe
  (testing "Renaming a non-existent player doesn't affect other players"
    (let [state {:scout #{user-id}}]
      (is (= state (rename-player state other-user-id "querty"))))))

(deftest basic-ready-test
  (testing "A game is ready"
    (is (ready? [1 2] {:scout #{1 2}
                           :soldier #{3 4}
                           :pyro #{5 6}
                           :demoman #{7 8}
                           :heavy #{9 10}
                           :engineer #{11 12}
                           :medic #{13 14}
                           :sniper #{15 16}
                           :spy #{17 18}}))))

(deftest ready-with-single-dupe-test
  (testing "Game is not ready when there are less than 18 players"
    (is (not (ready? [1 2] {:scout #{1 2}
                                :soldier #{1 4}
                                :pyro #{5 6}
                                :demoman #{7 8}
                                :heavy #{9 10}
                                :engineer #{11 12}
                                :medic #{13 14}
                                :sniper #{15 16}
                                :spy #{17 18}})))))

(deftest ready-with-complex-dupe-test
  ;todo better name
  (testing "Game is not ready when a player is duped"
    (is (not (ready? [1 2] {:scout #{1 2}
                                :soldier #{1 4}
                                :pyro #{5 6}
                                :demoman #{7 8}
                                :heavy #{9 10}
                                :engineer #{11 12}
                                :medic #{13 14}
                                :sniper #{15 16}
                                :spy #{17 18 19 20}})))))

(deftest ready-without-captains-test
  (testing "Game is not ready without 2 captains"
    (is (not (ready? [1] {:scout #{1 2}
                            :soldier #{3 4}
                            :pyro #{5 6}
                            :demoman #{7 8}
                            :heavy #{9 10}
                            :engineer #{11 12}
                            :medic #{13 14}
                            :sniper #{15 16}
                            :spy #{17 18}})))))

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
    (is (= {} (need [1 2] {:scout #{1 2}
                               :soldier #{3 4}
                               :pyro #{5 6}
                               :demoman #{7 8}
                               :heavy #{9 10}
                               :engineer #{11 12}
                               :medic #{13 14}
                               :sniper #{15 16}
                               :spy #{17 18}})))))

(deftest need-with-dupes-test
  (testing "Need handles duplicates correctly"
    (is (= {#{:scout :soldier} 1} (need [1 2] {:scout #{1 2}
                                                   :soldier #{1 4}
                                                   :pyro #{5 6}
                                                   :demoman #{7 8}
                                                   :heavy #{9 10}
                                                   :engineer #{11 12}
                                                   :medic #{13 14}
                                                   :sniper #{15 16}
                                                   :spy #{17 18 19 20}})))))

(deftest need-with-mulitple-dupes-test
  (testing "Need handles multiple duplicates correctly"
    (is (= {#{:scout :soldier} 1
            #{:soldier :pyro} 1} (need [1 2] {:scout #{1 2}
                                                  :soldier #{1 4}
                                                  :pyro #{4 6}
                                                  :demoman #{7 8}
                                                  :heavy #{9 10}
                                                  :engineer #{11 12}
                                                  :medic #{13 14}
                                                  :sniper #{15 16}
                                                  :spy #{17 18 19 20}})))))

(deftest need-with-dupe-and-extra-player-test
  (testing "Need handles duplicates correctly"
    (is (= {} (need [1 2] {:scout #{1 2 3}
                               :soldier #{1 4}
                               :pyro #{5 6}
                               :demoman #{7 8}
                               :heavy #{9 10}
                               :engineer #{11 12}
                               :medic #{13 14}
                               :sniper #{15 16}
                               :spy #{17 18 19 20}})))))

(deftest need-with-optional-dupe-and-dupe-test
  (testing "Need handles multiple duplicates correctly"
    (is (= {#{:soldier :pyro} 1} (need [1 2] {:scout #{1 2 3}
                                                  :soldier #{1 4}
                                                  :pyro #{4 6}
                                                  :demoman #{7 8}
                                                  :heavy #{9 10}
                                                  :engineer #{11 12}
                                                  :medic #{13 14}
                                                  :sniper #{15 16}
                                                  :spy #{17 18 19 20}})))))
