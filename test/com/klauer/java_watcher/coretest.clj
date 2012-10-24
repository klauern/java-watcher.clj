(ns com.klauer.java-watcher.coretest
  (:use [midje.sweet]
        [com.klauer.java-watcher.core])
  (:import [java.nio.file StandardWatchEventKinds]))

(fact "can make paths from strings"
      (instance? java.nio.file.Path (make-path "/usr/local")) => truthy)

(future-fact "each event is passed to the user function separately")

(future-fact "can unroll the event into a map")

(future-fact "functions cease being called after unregistering them")

(future-fact "can cancel a registered watch")
(future-fact "can register a watch")
(future-fact "can inspect the registered watches")
(future-fact "functions are called repeatedly on event changes")

(defn process [event]
  (println "hello event " event))