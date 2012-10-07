(ns com.klauer.j7-watcher.core
  (:require [name.stadig.polyfn :refer [defpolyfn]])
  (:import [java.nio.file Path Paths StandardWatchEventKinds
            WatchEvent WatchKey Watchable WatchService WatchEvent$Kind]))

(set! *warn-on-reflection* true)

(def kinds {:create StandardWatchEventKinds/ENTRY_CREATE
            :delete StandardWatchEventKinds/ENTRY_DELETE
            :modify StandardWatchEventKinds/ENTRY_MODIFY})

(defn make-path 
  "Creates a java.nio.file.Path object because Paths#get doesn't quite work without stupid fidgity
stuff like this"
  [^String directory]
  (Paths/get directory (into-array String "")))

(defn make-watcher [^Path dir]
  (-> dir .getFileSystem .newWatchService))

(defn register-with [^WatchService watch watch-kinds ^Path directory]
  (-> directory (.register watch watch-kinds)))

(defn unroll-event
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

(defn wait-for [^WatchService watch func]
  (let [w (.take watch)
        e (.pollEvents w)
        unrolled (map #(unroll-event %1 w) e)]
    ;; force calling each event to the user-defined function
    (dorun (map func unrolled))
    ;; then we reset the watch key
    (.reset w)
    ;; and start over again
    (recur watch func)))

(defpolyfn make-watch-types-from clojure.lang.PersistentVector [types]
  (into-array types))

(defpolyfn make-watch-types-from clojure.lang.APersistentMap$ValSeq [types]
  (into-array types))


(defn make-watch 
  "Make a watch on a path 'path', given a seq of kinds (see 'kinds')
   and passes these events to the `func` with any other arguments `args`" 
  [path watch-types func & args]
  (let [p (make-path path)
        watcher (make-watcher p)
        types (make-watch-types-from watch-types)]
    (register-with watcher types p)
    (wait-for watcher func)))