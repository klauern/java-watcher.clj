(ns com.klauer.java-watcher.core
  (:require [name.stadig.polyfn :refer [defpolyfn extend-polyfn]])
  (:import [java.nio.file FileSystems Path Paths StandardWatchEventKinds
            WatchEvent WatchKey Watchable WatchService WatchEvent$Kind])
  (:use [lamina.executor]
        [lamina.core]))

(set! *warn-on-reflection* true)

(def kinds {:create StandardWatchEventKinds/ENTRY_CREATE
            :delete StandardWatchEventKinds/ENTRY_DELETE
            :modify StandardWatchEventKinds/ENTRY_MODIFY
            ;; indicates that events may have been lost or discarded
            :overflow StandardWatchEventKinds/OVERFLOW})

(def watch-service (atom (.. FileSystems getDefault newWatchService)))

(def registered-watches (atom #{}))

(defn make-path 
  "Creates a java.nio.file.Path object from a string because Paths#get doesn't work that way (surprise!)"
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

(defn process-events [^WatchKey key func]
  (let [events (.pollEvents key)
        unrolled-events (map #(unroll-event %1 key) events)]
    (dorun (map func unrolled-events))
    (.reset key)
    ))

(defn wait-for
  [^WatchService watch func]
  (run-pipeline 
      watch
      #(task (.take ^WatchService %))
      #(process-events % func)
      (fn [_] (restart))))

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
    (swap! registered-watches conj (register-with p @watch-service types))
    (wait-for @watch-service func)))

(make-watch "/Users/klauer/dev/clojure/java-watcher.clj/watchabledir" [(:create kinds) (:modify kinds) (:delete kinds)] #(println "Hello event " %))