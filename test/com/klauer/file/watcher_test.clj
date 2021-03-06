(ns com.klauer.file.watcher-test
  (:use [midje.sweet]
        [com.klauer.file.watcher]
        [fs.core :only [temp-dir]])
  (:require [fs.core :as fs]
            [com.klauer.java-watcher.files :as files])
  (:import [java.nio.file StandardWatchEventKinds Files]
           [java.nio.file.attribute FileAttribute]))

(defn dummy-register
  ([] (register (.toString (.toPath (fs/temp-dir "tmp"))) #(println "HI") false))
  ([fn] (register (.toString (.toPath (fs/temp-dir "tmp"))) fn false)))


(fact "dirs registered are stored in a registry"
      (let [reg (dummy-register)]
        (get @registry (:path reg)) => truthy
        (-> (get @registry (:path reg)) first :recursive?) => false))


(future-fact "file delete events are triggered"
             (let [reg (dummy-register println)]
               (fs/create (:path reg)))
             (provided (println) => nil :times 1))
(future-fact "file modification events are triggered")
(future-fact "file create events are triggered")
(future-fact "directory creation events are triggered")
(future-fact "directory modification events are triggered")
(future-fact "directory deletion events are triggered")
(future-fact "directory creation events get registered")
(future-fact "can register subdirectories recursively")

;; Need to test some things about actual file-system events causing functions to be called
;; Midje provides some of that functionality with specifying call-counts
;; https://github.com/marick/Midje/wiki/Specifying-call-counts
;; Some things to check for that I have to think about a bit:
;; - watches continue to watch after multiple events
;; - multiple watches can act on the same event
;; -? can unsubscribe watches (not sure about this one, but I suppose it's possible since the registry is separate
;;    from the blocking watch

(fact "registration doesn't call the function registered"
      (do (dummy-register println)) => truthy
      (provided
        (println) => nil :times 0))

(fact "can make path from String"
      (instance? java.nio.file.Path (files/make-path (.toString (fs/temp-dir "tmp")))) => truthy)
(fact "can make path from File"
      (instance? java.nio.file.Path (files/make-path (fs/temp-dir "tmp"))) => truthy)

(fact "can register a directory with a Path"
      (instance? java.nio.file.WatchKey (register-dir (-> "tmp" fs/temp-dir .toPath))) => truthy)
(fact "can register a directory with a Path's String"
      (instance? java.nio.file.WatchKey (register-dir (-> "tmp" fs/temp-dir .toPath .toString))) => truthy)

(fact "registration is recursive by default"
      (let [path (-> (fs/temp-dir "tmp") .toPath .toString)
            reg (register path #(println "Hello"))]
        (-> (get @registry path) first :recursive?) => truthy))