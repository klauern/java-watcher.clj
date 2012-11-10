(ns com.klauer.file.watcher-test
  (:use [midje.sweet]
        [com.klauer.file.watcher]
        [fs.core :only [temp-dir]])
  (:require [fs.core :as fs]
            [com.klauer.java-watcher.files :as files])
  (:import [java.nio.file StandardWatchEventKinds Files]
           [java.nio.file.attribute FileAttribute]))

(defn dummy-register
  ([] (register (fs/temp-dir "tmp") #(println "HI") false)))

(future-fact "dirs registered are stored in a registry"
(fact "can make path from String"
      (instance? java.nio.file.Path (files/make-path (.toString (fs/temp-dir "tmp")))) => truthy)
(fact "can make path from File"
      (instance? java.nio.file.Path (files/make-path (fs/temp-dir "tmp"))) => truthy)
             (let [reg (dummy-register)]
               (> 0 (count registry)) => truthy)
             (provided registry => {}))
(future-fact "directories are registered recursively by default")
(future-fact "directory creation events get registered")