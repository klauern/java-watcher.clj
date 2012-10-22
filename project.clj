(defproject com.klauer/java-watcher "0.1.0-SNAPSHOT"
  :description "Thin wrapper around Java 7's WatchService"
  :url "https://github.com/klauern/java-watcher.clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [name.stadig/polyfn "2.0.0"]
                 [lamina "0.5.0-beta6"]
                 [midje "1.4.0" :scope "test"]]
  :plugins [[lein-midje "2.0.0-SNAPSHOT"]
            [codox "0.6.1"]])
