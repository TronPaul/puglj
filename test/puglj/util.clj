(ns puglj.util
  (:require [clojure.test :refer :all]))

(defmacro deftest-pending
  [name & body]
  (let [message (str "\n========\n" name " is pending !!\n========\n")]
    `(deftest ~name
       (println ~message))))