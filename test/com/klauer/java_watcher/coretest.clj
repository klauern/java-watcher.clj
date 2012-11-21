(ns com.klauer.java-watcher.coretest
  (:use [midje.sweet]
        [com.klauer.java-watcher.core]
        [fs.core :only [temp-dir]])
  (:require [fs.core :as fs]
            [com.klauer.java-watcher.files :as files])
  (:import [java.nio.file StandardWatchEventKinds Files]
           [java.nio.file.attribute FileAttribute]))

(def dirs [(.toString (temp-dir "one")) (.toString (temp-dir "two"))])
(defn dummy-fn [path]
  (println "dummy function called with path " path))
(defn dummy-watch
  ([dir]
    (register-watch dir [:modify] dummy-fn))
  ([dir msg]
    (register-watch dir [:modify] dummy-fn)))
(defn watches []
  @registered-watches)

(comment
  ;; this is likely to be removed when the other watcher gets implemented
  ;; completely.  Actually, this entire test file will go away.
  (future-fact "each event is passed to the user function separately")
  (future-fact "can unroll the event into a map")
  (future-fact "functions cease being called after unregistering them")
  (future-fact "functions are called repeatedly on event changes")
  (future-fact "can register multiple functions to be called on the same event subscription")
  (future-fact "unregistering a watch removes all functions to be called on it")
  (future-fact "can use relative paths with make-path")
  (future-fact "can use ~ in make-path")
  (future-fact "relative paths in make-path resolve to fully-qualified paths")
  (future-fact "registering a directory recursively registers sub-directories")
  (future-fact "if file system event is creating a directory, that sub-directory is registered")
)

(fact "can make paths from strings"
      (instance? java.nio.file.Path (files/make-path "/usr/local")) => truthy)

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

(fact "an unregistered watch is invalid"
             (let [watch (dummy-watch (second dirs))
                   still-valid (unregister-watch watch)]
               (.isValid watch) => false
               (.isValid still-valid) => falsey))

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

(future-fact "when registering two watches on separate dirs, one won't get called by events on the other's directory"
      (let [first (dummy-watch (first dirs))
            second (dummy-watch (second dirs))]
        ;; create a file on the second directory
        ;; verify that the first was never called
        ;; verify that the second was
        ;; create a file on the first directory, verify the vice-versa
        ))