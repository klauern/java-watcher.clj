(defproject com.klauer/java-watcher "0.1.0-SNAPSHOT"
  :description "Thin wrapper around Java 7's WatchService"
  :url "https://github.com/klauern/java-watcher.clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [lamina "0.5.0-beta8" :exclusions [useful]]
                 [potemkin "0.1.6"]
                 [midje "1.5-alpha5" :scope "test"]
                 [fs "1.3.2" :scope "test"]
                 [com.stuartsierra/lazytest "1.2.3" :scope "test"]
                 ]
  :repositories {"stuart" "http://stuartsierra.com/maven2"}
  :plugins [[lein-midje "3.0-alpha1"]
            [codox "0.6.1"]]
  
  )
