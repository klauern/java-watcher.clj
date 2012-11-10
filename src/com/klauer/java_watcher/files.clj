(ns com.klauer.java-watcher.files
  (:import [java.nio.file Files Path Paths LinkOption]
           [java.io File]))

(set! *warn-on-reflection* true)


;(defn make-path 
;  "Creates a java.nio.file.Path object from a string because Paths#get doesn't work that way (surprise!)"
;  [^String directory]
;  (Paths/get directory (into-array String "")))

(defprotocol MakesPath
  "Makes a Path"
  (make-path [path]))

(extend-protocol MakesPath
  String
  (make-path
    [path]
    (Paths/get path (into-array String "")))
  File
  (make-path
    [path]
    (.toPath path)))

(defprotocol FileExistsCheck
  "Check for file existence based on the type passed in"
  (exists? [path]))

(extend-protocol FileExistsCheck
  String
  (exists?
    [path]
    (Files/exists (make-path path) (into-array LinkOption nil)))
  Path
  (exists?
    [path]
    (Files/exists path (into-array LinkOption nil)))
  File
  (exists?
    [path]
    (.exists path)))

(defn create-temp-file
  [^Path path ^String filename]
  )
