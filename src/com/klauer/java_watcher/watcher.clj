(ns com.klauer.java-watcher.watcher
  (:import [java.nio.file Files SimpleFileVisitor FileVisitResult FileSystems Path Paths StandardWatchEventKinds
            WatchEvent WatchKey Watchable WatchService WatchEvent$Kind]
           [java.nio.file.attribute BasicFileAttributes])
  (:require [com.klauer.java-watcher.files :as files]
            [lamina.executor :as lamexecutor]
            [lamina.core :as lamcore]
            [com.klauer.java-watcher.files :as f]))

;; this takes all sorts of 'inspiration' from Oracle's example code for using the WatchService.
;; I'm hoping to at least use this as the basis for a revamping of the process that I have
;; implemented in core, but I am not sure what is going on entirely yet.


(set! *warn-on-reflection* true)
(def watcher (atom (.. FileSystems getDefault newWatchService)))
(def watch-kinds (into-array ^WatchEvent$Kind [StandardWatchEventKinds/ENTRY_CREATE StandardWatchEventKinds/ENTRY_MODIFY StandardWatchEventKinds/ENTRY_DELETE]))

(defn register-dir
  "Register the given directory to the WatchService for all known types of events (Create, Modify, Delete)"
  [^Path dir]
  (let [key (.register dir @watcher watch-kinds)]))

(defn register-all
  "Register a directory and all it's sub-directories recursively based on the start Path passed in"
  [^Path start]
   (Files/walkFileTree start (proxy [SimpleFileVisitor] []
    ^FileVisitResult (preVisitDirectory [^Path dir ^BasicFileAttributes attrs]
                                        (register-dir dir)
                                        FileVisitResult/CONTINUE))))
