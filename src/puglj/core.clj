(ns puglj.core
  (:require [clojure.set :as s]))

(def valid-classes #{:scout :soldier :pyro
              :demoman :heavy :engineer
              :sniper :medic :spy})

(defn- clean-empty
  [state]
  (reduce (fn [state cls]
            (if (= 0 (count (get-in state [:classes cls])))
              (update-in state [:classes] dissoc cls)
              state))
    state valid-classes))

(defn- remove-player-from-class
  [state nick class]
  (if (contains? (:classes state) class)
    (update-in state [:classes class] disj nick)
    state))

(defn remove-player
  "Removes a player from the pug"
  [state nick]
  (clean-empty
    (reduce (fn [state cls]
              (remove-player-from-class state nick cls))
      state valid-classes)))

(defn- add-player-to-class
  [state nick class]
  (if (contains? (:classes state) class)
    (update-in state [:classes class] conj nick)
    (update-in state [:classes] assoc class #{nick})))

(defn add-player
  "Adds a player to the pug"
  [state nick classes]
  (assert (= 0 (count (s/difference classes valid-classes))))
  (reduce (fn [state cls]
            (add-player-to-class state nick cls))
    (remove-player state nick) classes))

(defn- rename-player-in-class
  [state class old-nick new-nick]
  (if (contains? (get-in state [:classes class] #{}) old-nick)
    (add-player-to-class (remove-player-from-class state old-nick class) new-nick class)
    state))

(defn rename-player
  "Renames a player in the pug"
  [state old-nick new-nick]
  (reduce (fn [state cls]
            (rename-player-in-class state cls old-nick new-nick)) state valid-classes))

(defn- count-classes
  [state]
  (map #(count (get-in state [:classes %] #{})) valid-classes))

(defn- mapunion
  [f & colls]
  (apply s/union (apply map f colls)))

(defn- get-dupes
  [state class]
  (s/intersection (get-in state [:classes class])
    (mapunion #(get-in state [:classes %]) (disj valid-classes class))))

(defn- remove-duplicate-names-from-class
  [state class]
  (if-let [dupes (get-dupes state class)]
    (reduce (fn [state nick]
              (remove-player-from-class state nick class))
      state dupes)
    state))

(defn- remove-duplicate-names
  [state]
  (reduce remove-duplicate-names-from-class state valid-classes))

(defn ready?
  "Returns true if a pug is ready, else false"
  [state]
  (and (every? (partial <= 2) (count-classes (remove-duplicate-names state)))
    (<= 18 (count (mapunion #(get-in state [:classes %]) valid-classes)))))
