(ns com.klauer.java-watcher.watcher-test
  (:use [midje.sweet]
        [com.klauer.java-watcher.watcher]
        [fs.core :only [temp-dir]])
  (:require [fs.core :as fs]
            [com.klauer.java-watcher.files :as files])
  (:import [java.nio.file StandardWatchEventKinds Files]
           [java.nio.file.attribute FileAttribute]))

(future-fact "can register a single dir")
(future-fact "dirs registered are stored in a registry")
(future-fact "directories are registered recursively by default")
(future-fact "directory creation events get registered")