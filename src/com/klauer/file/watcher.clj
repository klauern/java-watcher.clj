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

(def watch-service (atom ^WatchService (.. FileSystems getDefault newWatchService)))
(def watch-kinds (into-array [StandardWatchEventKinds/ENTRY_CREATE StandardWatchEventKinds/ENTRY_MODIFY StandardWatchEventKinds/ENTRY_DELETE]))
(def registry (atom {}))

;; A Path has an event that we unroll in to this type
(defrecord PathEvent [path event-type])
;; We register a path and store the function to call on the types provided
(defrecord FunctionRegistration [function path types recursive?])
;; Each registered directory has a key with it and a path, so we can look up the key and path with this
;; record.
(defrecord PathKey [watch-key path])

(defn unroll-event
  "Convert a WatchEvent into a PathEvent"
  [^WatchEvent event ^WatchKey key]
  (let [dir (.watchable key)
        ;; This is how you get an absolute path, because, surprise!, #toAbsolutePath
        ;; doesn't work that way... See http://stackoverflow.com/a/7802029/7008
        ;; for my awful discovery.
        context (.context event)
        kind (.kind event)
        full_path (.toString (.resolve ^Path dir ^Path context))]
    (->PathEvent full_path kind)))

(defprotocol RegistersDirectories
  "Registers a directory with a WatchService"
  (register-dir [path]))

(extend-protocol RegistersDirectories
  String
  (register-dir 
    [^String path]
  (.register ^Path (f/make-path path) @watch-service watch-kinds))
  Path
  (register-dir
    [^Path path]
  (.register path @watch-service watch-kinds)))

(defn register-dir-recursive
  "Register a directory and all it's sub-directories recursively based on the start Path passed in"
  [^Path start]
   (Files/walkFileTree start (proxy [SimpleFileVisitor] []
    ^FileVisitResult (preVisitDirectory [^Path dir ^BasicFileAttributes attrs]
                                        (register-dir dir)
                                        FileVisitResult/CONTINUE))))

(defn register
  "register a directory with a function"
  ([^String path-str function]
    (register path-str function true))
  ([^String path-str function recursive?]
    (let [path ^Path (f/make-path path-str)
          registration (->FunctionRegistration function (.toString path) (keys kinds) recursive?)]
      (if recursive?
        (register-dir-recursive path)
        (register-dir path))
      (swap! registry update-in [(.toString path)] conj registration)
      registration
