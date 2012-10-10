(ns com.klauer.java-watcher.coretest
  (:use [midje.sweet]
        [com.klauer.java-watcher.core])
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


(defn process [event]
  (println "hello event " event))

;;(future-cancel thing)
;;(def thing (future (make-watch "/Users/klauer/dev/clojure/watchabledir/" (vals kinds) process)))