(ns com.klauer.file.watcher-test
  (:use [midje.sweet]
        [com.klauer.file.watcher]
        [fs.core :only [temp-dir]])
  (:require [fs.core :as fs]
            [com.klauer.java-watcher.files :as files])
  (:import [java.nio.file StandardWatchEventKinds Files]
           [java.nio.file.attribute FileAttribute]))

(defn dummy-register
  ([] (register (.toString (.toPath (fs/temp-dir "tmp"))) #(println "HI") false)))

(fact "dirs registered are stored in a registry"
      (let [reg (dummy-register)]
        (count (keys @registry)) => > 1))

(future-fact "directory creation events get registered")
(future-fact "can register subdirectories recursively")

(fact "can make path from String"
      (instance? java.nio.file.Path (files/make-path (.toString (fs/temp-dir "tmp")))) => truthy)
(fact "can make path from File"
      (instance? java.nio.file.Path (files/make-path (fs/temp-dir "tmp"))) => truthy)

(fact "can register a directory with a Path"
      (instance? java.nio.file.WatchKey (register-dir (-> "tmp" fs/temp-dir .toPath))) => truthy)
(fact "can register a directory with a Path's String"
      (instance? java.nio.file.WatchKey (register-dir (-> "tmp" fs/temp-dir .toPath .toString))) => truthy)