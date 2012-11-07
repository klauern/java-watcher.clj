(ns com.klauer.java-watcher.core
  (:import [java.nio.file FileSystems Path Paths StandardWatchEventKinds
            WatchEvent WatchKey Watchable WatchService WatchEvent$Kind])
  (:require [com.klauer.java-watcher.files :as files]
            [lamina.executor :as lamexecutor]
            [lamina.core :as lamcore]))

(set! *warn-on-reflection* true)

(def kinds {:create StandardWatchEventKinds/ENTRY_CREATE
            :delete StandardWatchEventKinds/ENTRY_DELETE
            :modify StandardWatchEventKinds/ENTRY_MODIFY
            ;; indicates that events may have been lost or discarded
            :overflow StandardWatchEventKinds/OVERFLOW})

(def all-kinds (into-array [StandardWatchEventKinds/ENTRY_CREATE StandardWatchEventKinds/ENTRY_DELETE StandardWatchEventKinds/ENTRY_MODIFY]))
(def watch-service (atom (.. FileSystems getDefault newWatchService)))
(def registered-watches (atom #{}))

(defrecord EventSubscription [path subscribed-events function recursive?])
;; (defrecord PathEvent [subscribed-events function recursive?])
(defrecord Watch [watchkey pathevents functions])
(def registered-watch-path-events (atom {}))

(defn unregister-watch 
  "Remove a watch from the registered watch list, as well as cancelling any future
   event monitoring that was registered with it" 
  [^WatchKey watch]
  (.cancel watch)
  (swap! registered-watches disj watch)
  watch)

(defn register-with 
  "Register a directory to watch for the given event kinds"
  [^Path directory ^WatchService watch watch-kinds]
  ;; also should recurse sub-directories with the same thing
  ;; see http://codingjunkie.net/eventbus-watchservice/ for some inspiration
  (.register directory watch watch-kinds))

(defn unroll-event
  "Convert a WatchEvent into a map of the kind of event and path that changed"
  [^WatchEvent event ^WatchKey key]
  (let [dir (.watchable key)
        ;; This is how you get an absolute path, because, surprise!, #toAbsolutePath
        ;; doesn't work that way... See http://stackoverflow.com/a/7802029/7008
        ;; for my awful discovery.
        context (.context event)
        full_path (.resolve ^Path dir ^Path context)]
    {:kind (.kind event) :path (.toString full_path)}))

(defn process-events
  "Taking a WatchKey, process the events that occurred in it, and run the function across all of them"
  [^WatchKey key func]
  (if (.isValid key)
    (let [events (.pollEvents key)
          unrolled-events (map #(unroll-event %1 key) events)]
      ;; lookup function to call based on the :path and :kind
      (dorun (map func unrolled-events))
      (.reset key))))

(defn pipeline-events-with
  "Start a Lamina Pipeline on the watch and run function"
  [^WatchService watch func]
  (lamcore/run-pipeline 
      watch
      ;; blocks for all possible events
      #(lamexecutor/task (.take ^WatchService %))
      ;; needs to determine which path and event type was invoked,
      ;; then find the functions to call on it.
      #(process-events % func)
      (fn [restartable]
        (if restartable
          (lamcore/restart)))))

(defn register-watch
  "Make a watch on a path 'path', given a seq of kinds (see 'kinds')
   and passes these events to the `func` with any other arguments `args`"
  [path watch-types func & args]
  (let [p ^Path (files/make-path path)
        types (into-array (map kinds watch-types))
        watch (register-with p @watch-service types)
        ;; path-event (->PathEvent (.toString p) watch-types)
        event-sub (->EventSubscription (.toString p) types func true)
       ;; watch-rec (->Watch watch path-event func)
        ]
    ;; (swap! registered-watch-path-events conj watch-rec) ;; will eventually replace registered-watches
    (swap! registered-watches conj watch)
    ;; TODO: store the func in the registered-watches as an array of funcs to call on the watch.
    (pipeline-events-with @watch-service func)
    watch))
