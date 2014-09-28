(ns puglj.lobby
  (:require [clojure.set :as s]))

(def valid-classes #{:scout :soldier :pyro :demoman :heavy :engineer :sniper :medic :spy})

(defn- clean-empty
  [classes]
  (reduce (fn [classes cls]
            (if (= 0 (count (get classes cls)))
              (dissoc classes cls)
              classes))
    classes valid-classes))

(defn- remove-player-from-class
  [classes nick class]
  (if (contains? classes class)
    (update-in classes [class] disj nick)
    classes))

(defn remove-player
  "Removes a player from the pug"
  [classes nick]
  (clean-empty
    (reduce (fn [classes cls]
              (remove-player-from-class classes nick cls))
      classes valid-classes)))

(defn- add-player-to-class
  [classes nick class]
  (if (contains? classes class)
    (update-in classes [class] conj nick)
    (assoc classes class #{nick})))

(defn add-player
  "Adds a player to the pug"
  [classes nick classes-to-add]
  (assert (= 0 (count (s/difference classes-to-add valid-classes))))
  (reduce (fn [classes cls]
            (add-player-to-class classes nick cls))
    (remove-player classes nick) classes-to-add))

(defn- rename-player-in-class
  [classes class old-nick new-nick]
  (if (contains? (get classes class #{}) old-nick)
    (add-player-to-class (remove-player-from-class classes old-nick class) new-nick class)
    classes))

(defn rename-player
  "Renames a player in the pug"
  [classes old-nick new-nick]
  (reduce (fn [classes cls]
            (rename-player-in-class classes cls old-nick new-nick))
    classes valid-classes))

(defn- count-classes
  ([classes]
    (count-classes classes valid-classes))
  ([classes classes-to-count]
    (map #(count (get classes % #{})) classes-to-count)))

(defn- mapunion
  [f & colls]
  (apply s/union (apply map f colls)))

(defn- get-dupes
  [classes class]
  (s/intersection (get classes class)
    (mapunion #(get classes %) (disj valid-classes class))))

(defn- remove-duplicate-names-from-class
  [classes class]
  (if-let [dupes (get-dupes classes class)]
    (reduce (fn [classes nick]
              (remove-player-from-class classes nick class))
      classes dupes)
    classes))

(defn- remove-duplicate-names
  [classes]
  (reduce remove-duplicate-names-from-class classes valid-classes))

(defn players
  "Returns a set of all players"
  [classes]
  (mapunion #(get classes %) valid-classes))

(defn- class-need
  [classes]
  (reduce (fn [reqs cls]
            (let [num (max 0 (- 2 (count (get classes cls #{}))))]
              (if (< 0 num)
                (assoc reqs cls num)
                reqs)))
    {} valid-classes))

(defn- classes-for-player
  [classes nick]
  (set (map first (filter #(contains? (last %) nick) classes))))

(defn- dupes-need
  [classes]
  (reduce (fn [reqs nick]
            (let [cfp (classes-for-player classes nick)]
              (if (and (<= 2 (count cfp))
                    (every? #(>= 2 %) (count-classes classes cfp)))
                (merge-with + reqs {cfp 1})
                reqs)))
  {} (players classes)))

(defn- captains-need
  [captains]
  (if (> 2 (count captains))
    {:captain (- 2 (count captains))}
    {}))

(defn need
  "Returns a map of unmet requirements"
  [captains classes]
  (merge (captains-need captains) (class-need classes) (dupes-need classes)))

(defn ready?
  "Returns true if a pug is ready, else false"
  [captains classes]
  (and (every? (partial <= 2) (count-classes (remove-duplicate-names classes)))
    (<= 18 (count (players classes)))
    (<= 2 (count captains))))
