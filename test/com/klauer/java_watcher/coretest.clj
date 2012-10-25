(ns com.klauer.java-watcher.coretest
  (:use [midje.sweet]
        [com.klauer.java-watcher.core]
        [fs.core :only [temp-dir]])
  (:import [java.nio.file StandardWatchEventKinds Files]))

(fact "can make paths from strings"
      (instance? java.nio.file.Path (make-path "/usr/local")) => truthy)

(future-fact "each event is passed to the user function separately")

(future-fact "can unroll the event into a map")

(future-fact "functions cease being called after unregistering them")

(future-fact "can cancel a registered watch")
(future-fact "can register a watch")
(future-fact "can inspect the registered watches")
(future-fact "functions are called repeatedly on event changes")

(def dirs [(.toString (temp-dir "one")) (.toString (temp-dir "two"))])

(future-fact "unregistering one watch should NOT invalidate any others"
      (let [first (register-watch (first dirs) [(:modify kinds)] #(println %))
            second (register-watch (second dirs) [(:modify kinds)] #(println %))
            unregistered (unregister-watch (first @registered-watches))]
        (.isValid (first @registered-watches)) => true))


(defn process [event]
  (println "hello event " event))