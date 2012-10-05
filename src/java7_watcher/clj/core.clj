(ns java7-watcher.clj.core
  (:import [java.nio.file Path Paths StandardWatchEventKinds
            WatchEvent WatchKey WatchService]))

(def kinds {:create StandardWatchEventKinds/ENTRY_CREATE
            :delete StandardWatchEventKinds/ENTRY_DELETE
            :modify StandardWatchEventKinds/ENTRY_MODIFY})
(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))

(defn make-path 
  "Creates a java.nio.file.Path object because Paths#get doesn't quite work without stupid fidgity
stuff like this"
  [^String directory]
  (Paths/get directory (into-array String "")))

(defn make-watcher [^Path dir]
  (-> dir .getFileSystem .newWatchService))

(defn register [^WatchService watch watch-kinds directory]
  (-> directory (.register watch (into-array [watch-kinds]))))

(defn process-events [events]
  (map events #(println %)))

(defn wait-for [watch func]
  (let [w (.take watch)
        e (.pollEvents w)]    
    (func)
    (.reset w)
    (recur watch func)))
