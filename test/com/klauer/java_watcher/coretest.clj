(ns com.klauer.java-watcher.coretest
  (:use [midje.sweet]
        [com.klauer.java-watcher.core]
        [fs.core :only [temp-dir]])
  (:import [java.nio.file StandardWatchEventKinds Files]))

(def dirs [(.toString (temp-dir "one")) (.toString (temp-dir "two"))])
(defn dummy-watch [dir]
  (register-watch dir [(:modify kinds)] #(println %)))

(fact "can make paths from strings"
      (instance? java.nio.file.Path (make-path "/usr/local")) => truthy)

(future-fact "each event is passed to the user function separately")
(future-fact "can unroll the event into a map")
(future-fact "functions cease being called after unregistering them")


(defn watches []
  @registered-watches)

(fact "can unregister a registered watch"
             (let [watch (dummy-watch (first dirs))]
               (unregister-watch watch)
               (count (watches)) => 0)
             (against-background (before :facts (reset! registered-watches #{})))
             (provided 
              (watches)  => #{}))
(future-fact "can register a watch")
(future-fact "can inspect the registered watches")
(future-fact "functions are called repeatedly on event changes")

(fact "registering a watch returns the same watch service in `registered-watches`"
      (let [watch (dummy-watch (first dirs))]
        watch => (first @registered-watches))
      (against-background (before :facts (reset! registered-watches #{}))))

(future-fact "unregistering one watch should NOT invalidate any others"
      (let [first (dummy-watch (first dirs))
            second (dummy-watch (second dirs))]
        (unregister-watch (first @registered-watches))
        (.isValid (first @registered-watches)) => true))