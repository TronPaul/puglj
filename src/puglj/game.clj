(ns puglj.game
  (:require [puglj.lobby :as lobby]))

(defn pick-captains
  "Pick the two captains"
  [captains]
  (assert (<= 2 (count captains)))
  (reduce (fn [chosen _]
            (conj chosen (rand-nth (apply disj (set captains) (set chosen))))) [] (range 2)))

(defn- add-player-to-team
  [teams team nick class]
  (assoc teams team (assoc (nth teams team) class nick)))

(defn pick-player
  "Pick player for team"
  [state team nick class]
  (assert (contains? (lobby/players (:pool state)) nick))
  (assert (contains? lobby/valid-classes class))
  (update-in (update-in state [:pool] lobby/remove-player "a")
    [:teams] add-player-to-team team nick class))

(defn- complete-team?
  [team]
  (every? #(get team %) lobby/valid-classes))

(defn complete?
  "Check if picking is complete"
  [state]
  (every? complete-team? (:teams state)))
