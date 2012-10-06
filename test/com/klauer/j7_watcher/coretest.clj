(ns com.klauer.j7-watcher.coretest
  (:use [midje.sweet]
        [com.klauer.j7-watcher.core])
  (:import [java.nio.file StandardWatchEventKinds]))

(fact "can create watch type from clojure types"
      (type (make-watch-types-from ["One thing" "Two Things"])) => (class (into-array String []))
      (let [{:keys [create delete] :as ts} kinds]
        (class (make-watch-types-from [create delete]) => (class (into-array StandardWatchEventKinds [])))))


(fact "can make paths from strings"
      (instance? java.nio.file.Path (make-path "/usr/local")) => truthy)
;; (def t (future (make-watch "/Users/klauer/dev/clojure/java7-watcher.clj/watchabledir" [StandardWatchEventKinds/ENTRY_CREATE StandardWatchEventKinds/ENTRY_MODIFY StandardWatchEventKinds/ENTRY_DELETE] #(println "It happened" %))))
;; (def thing (future (make-watch "/Users/klauer/dev/clojure/java7-watcher.clj/watchabledir" (vals kinds) #(println "event happens" %))))