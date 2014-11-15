(ns puglj.lobby
  (:require [clojure.set :as s]))

(def valid-classes #{:scout :soldier :pyro :demoman :heavy :engineer :sniper :medic :spy})

(def base-state {:captains #{} :classes {}})

(defn- clean-empty
  [classes]
  (reduce (fn [classes cls]
            (if (= 0 (count (get classes cls)))
              (dissoc classes cls)
              classes))
    classes valid-classes))

(defn- remove-player-from-class
  [classes user-id class]
  (if (contains? classes class)
    (update-in classes [class] disj user-id)
    classes))

(defn- remove-player-classes
  [classes user-id]
  (clean-empty
    (reduce (fn [classes cls]
              (remove-player-from-class classes user-id cls))
      classes valid-classes)))

(defn remove-player
  "Removes a player from the pug"
  [state user-id]
  (update-in (update-in state [:classes] remove-player-classes user-id) [:captains] disj user-id))

(defn- add-player-to-class
  [classes user-id class]
  (if (contains? classes class)
    (update-in classes [class] conj user-id)
    (assoc classes class #{user-id})))

(defn add-player-classes
  [classes user-id classes-to-add]
  (reduce (fn [classes cls]
            (add-player-to-class classes user-id cls))
    classes classes-to-add))

(defn add-player
  "Adds a player to the pug"
  [state user-id classes-to-add]
  (assert (= 0 (count (s/difference classes-to-add valid-classes))))
  (update-in (remove-player state user-id) [:classes] add-player-classes user-id classes-to-add))

(defn- rename-player-in-class
  [classes class old-user-id new-user-id]
  (if (contains? (get classes class #{}) old-user-id)
    (add-player-to-class (remove-player-from-class classes old-user-id class) new-user-id class)
    classes))

(defn- rename-player-classes
  [classes old-user-id new-user-id]
  (reduce (fn [classes cls]
            (rename-player-in-class classes cls old-user-id new-user-id))
    classes valid-classes))

(defn- rename-player-captains
  [captains old-user-id new-user-id]
  (if (contains? captains old-user-id)
    (conj (disj captains old-user-id) new-user-id)
    captains))

(defn rename-player
  "Renames a player in the pug"
  [state old-user-id new-user-id]
  (update-in (update-in state [:classes] rename-player-classes old-user-id new-user-id) [:captains] rename-player-captains old-user-id new-user-id))

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
    (reduce (fn [classes user-id]
              (remove-player-from-class classes user-id class))
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
  [classes user-id]
  (set (map first (filter #(contains? (last %) user-id) classes))))

(defn- dupes-need
  [classes]
  (reduce (fn [reqs user-id]
            (let [cfp (classes-for-player classes user-id)]
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
