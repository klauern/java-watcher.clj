(ns com.klauer.j7-watcher.core
  (:import [java.nio.file Path Paths StandardWatchEventKinds
            WatchEvent WatchKey WatchService WatchEvent$Kind]))

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

(defn register-with [^WatchService watch watch-kinds directory]
  (-> directory (.register watch watch-kinds)))

(defn- process-events [events]
  (map events #(println %)))

(defn wait-for [watch func & args]
  (let [w (.take watch)
        e (.pollEvents w)]    
    (func args)
    ;; then we reset the watch key
    (.reset w)
    ;; and start over again
    (recur watch func args)))

(defn make-watch 
  "Make a watch on a path 'path', given a seq of kinds (see 'kinds')
   and passes these events to the `func` with any other arguments `args`" 
  [path watch-types func & args]
  (let [p (make-path path)
        watcher (make-watcher p)]
    (register-with watcher watch-types p)
    (wait-for watcher func args)))

(make-watch "/Users/klauer/dev/clojure/java7-watcher.clj/watchabledir" (into-array (vals kinds)) #(println "event happens"))

