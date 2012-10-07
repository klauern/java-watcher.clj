(ns com.klauer.j7-watcher.coretest
  (:use [midje.sweet]
        [com.klauer.j7-watcher.core])
  (:import [java.nio.file StandardWatchEventKinds]))

(fact "can create watch type from clojure types"
      (type (make-watch-types-from ["One thing" "Two Things"])) => (class (into-array String []))
      (let [{:keys [create delete] :as ts} kinds]
        (class (make-watch-types-from [create delete]) =future=> (class (into-array StandardWatchEventKinds [])))))


(fact "can make paths from strings"
      (instance? java.nio.file.Path (make-path "/usr/local")) => truthy)

(future-fact "each event is passed to the user function separately"
      )

(future-fact "can unroll the event into a map")


;; (def t (future (make-watch "/Users/klauer/dev/clojure/java7-watcher.clj/watchabledir" [StandardWatchEventKinds/ENTRY_CREATE StandardWatchEventKinds/ENTRY_MODIFY StandardWatchEventKinds/ENTRY_DELETE] #(println "It happened" %))))
(defn process [event]
  (println "event being processed from user side as " event))

;(future-cancel thing)
(def thing (future (make-watch "/Users/klauer/dev/clojure/java7-watcher.clj/watchabledir" (vals kinds) process)))