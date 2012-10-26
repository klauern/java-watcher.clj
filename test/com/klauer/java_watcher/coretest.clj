(ns com.klauer.java-watcher.coretest
  (:use [midje.sweet]
        [com.klauer.java-watcher.core]
        [fs.core :only [temp-dir]])
  (:import [java.nio.file StandardWatchEventKinds Files]))

(def dirs [(.toString (temp-dir "one")) (.toString (temp-dir "two"))])
(defn dummy-watch [dir]
  (register-watch dir [(:modify kinds)] #(println %)))
(defn watches []
  @registered-watches)

(future-fact "each event is passed to the user function separately")
(future-fact "can unroll the event into a map")
(future-fact "functions cease being called after unregistering them")
(future-fact "an unregistered watch is invalid")
(future-fact "can inspect the registered watches")
(future-fact "functions are called repeatedly on event changes")

(fact "can make paths from strings"
      (instance? java.nio.file.Path (make-path "/usr/local")) => truthy)

(background 
  (before :facts (reset! registered-watches #{}))
  (after :facts (reset! registered-watches #{})))

(fact "can register a watch"
      (let [watch (dummy-watch (first dirs))]
        (.isValid watch) => true
        (first @registered-watches) => watch))

(fact "registering a watch returns the same watch service in `registered-watches`"
      (let [watch (dummy-watch (first dirs))]
        watch => (first @registered-watches)))

(fact "unregistering a watch returns the unregistered watch"
      (let [watch (dummy-watch (first dirs))
            unregistered (unregister-watch watch)]
        watch => unregistered))

(fact "unregistering one watch should NOT invalidate any others"
      (let [first (dummy-watch (first dirs))
            second (dummy-watch (second dirs))]
        (unregister-watch first)
        (.isValid second) => true))

(fact "can unregister a registered watch"
      (let [watch (dummy-watch (first dirs))]
        (unregister-watch watch)
        (count (watches)) => 0)
      (provided 
        (watches) => #{}))
