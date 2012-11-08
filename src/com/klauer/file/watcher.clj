(ns com.klauer.file.watcher
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

(def kinds {:create StandardWatchEventKinds/ENTRY_CREATE
            :delete StandardWatchEventKinds/ENTRY_DELETE
            :modify StandardWatchEventKinds/ENTRY_MODIFY
            ;; indicates that events may have been lost or discarded
            :overflow StandardWatchEventKinds/OVERFLOW})

(def watcher (atom (.. FileSystems getDefault newWatchService)))
(def watch-kinds (into-array [StandardWatchEventKinds/ENTRY_CREATE StandardWatchEventKinds/ENTRY_MODIFY StandardWatchEventKinds/ENTRY_DELETE]))
(def registry (atom {}))
(def watch-service (atom (.. FileSystems getDefault newWatchService)))

;; A Path has an event that we unroll in to this type
(defrecord PathEvent [path event-type])
;; We register a path and store the function to call on the types provided
(defrecord FunctionRegistration [function path types recursive?])
;; Each registered directory has a key with it and a path, so we can look up the key and path with this
;; record.
(defrecord PathKey [watch-key path])

(defn register-dir
  "Register the given directory to the WatchService for all known types of events (Create, Modify, Delete)"
  [^Path dir]
  (let [key (.register dir @watcher watch-kinds)]))

(defn register-dir-recursive
  "Register a directory and all it's sub-directories recursively based on the start Path passed in"
  [^Path start]
   (Files/walkFileTree start (proxy [SimpleFileVisitor] []
    ^FileVisitResult (preVisitDirectory [^Path dir ^BasicFileAttributes attrs]
                                        (register-dir dir)
                                        FileVisitResult/CONTINUE))))

(defn register
  [^String path-str function recursive?]
  (let [path ^Path (f/make-path path-str)
        registration (->FunctionRegistration function (.toString path) (keys kinds) recursive?)]
    (if recursive?
      (register-dir-recursive path)
      (register-dir path))
    (swap! registry update-in [(.toString path)] conj registration)
    ))