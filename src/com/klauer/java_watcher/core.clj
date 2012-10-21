(ns com.klauer.java-watcher.core
  (:require [name.stadig.polyfn :refer [defpolyfn extend-polyfn]])
  (:import [java.nio.file FileSystems Path Paths StandardWatchEventKinds
            WatchEvent WatchKey Watchable WatchService WatchEvent$Kind]))

(set! *warn-on-reflection* true)

(def kinds {:create StandardWatchEventKinds/ENTRY_CREATE
            :delete StandardWatchEventKinds/ENTRY_DELETE
            :modify StandardWatchEventKinds/ENTRY_MODIFY
            ;; indicates that events may have been lost or discarded
            :overflow StandardWatchEventKinds/OVERFLOW})

(def watch-service (atom (.. FileSystems getDefault newWatchService)))

(defn make-path 
  "Creates a java.nio.file.Path object from a string because Paths#get doesn't quite work without stupid fidgity
stuff like this"
  [^String directory]
  (Paths/get directory (into-array String "")))

(defn register-with 
  "Register a directory to watch for the given event kinds"
  [^Path directory ^WatchService watch watch-kinds]
  (.register directory watch watch-kinds))

(defn unroll-event
  "Convert a WatchEvent into a map of the kind of event and path that changed"
  [^WatchEvent event ^WatchKey key]
  (let [dir (.watchable key)
        ;; This is how you get an absolute path, because, surprise!, #toAbsolutePath
        ;; doesn't work that way... See http://stackoverflow.com/a/7802029/7008
        ;; for my awful discovery.
        context (.context event)
        full_path (.resolve ^Path dir ^Path context)
        ]
    { :kind (.kind event)
     :path (.toString full_path)}))

(defn wait-for 
  "loop and wait for events.  Blocks until an event happens, then processes the event
   with the func passed in for each filesystem event that happens.  recurs until the
   sun burns out or the service is stopped"
  [^WatchService watch func]
  (let [w (.take watch)
        e (.pollEvents w)
        unrolled (map #(unroll-event %1 w) e)]
    ;; force calling each event to the user-defined function
    (dorun (map func unrolled))
    ;; then we reset the watch key
    (.reset w)
    ;; and start over again
    (recur watch func)))

;; this is probably overkill, but it's also really nifty.
(defpolyfn make-watch-types-from [types])
(extend-polyfn make-watch-types-from clojure.lang.PersistentVector [types]
               (into-array types))

(defn make-watch 
  "Make a watch on a path 'path', given a seq of kinds (see 'kinds')
   and passes these events to the `func` with any other arguments `args`" 
  [path watch-types func & args]
  (let [p (make-path path)
        types (make-watch-types-from watch-types)]
    (register-with p @watch-service types)
    (wait-for @watch-service func)))
