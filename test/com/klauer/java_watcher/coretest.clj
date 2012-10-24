(ns com.klauer.java-watcher.coretest
  (:use [midje.sweet]
        [com.klauer.java-watcher.core])
  (:import [java.nio.file StandardWatchEventKinds]))

(fact "can make paths from strings"
      (instance? java.nio.file.Path (make-path "/usr/local")) => truthy)

(future-fact "each event is passed to the user function separately")

(future-fact "can unroll the event into a map")

(defn process [event]
  (println "hello event " event))