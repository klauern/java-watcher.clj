# java-watcher.clj
[![Build Status](https://secure.travis-ci.org/klauern/java7-watcher.clj.png)](http://travis-ci.org/klauern/java-watcher.clj)

Stupidly simple java7 file watch service.  This is largely a Clojure-ified wrapper around [Java 7's WatchService](http://docs.oracle.com/javase/tutorial/essential/io/notification.html), which works natively on Windows and Linux, but not entirely on OSX.  Don't use this if you run a Mac since OS X [has no native file watching service](http://stackoverflow.com/a/11182515/7008), but rather uses a poll-based approach.  Or rather, if you don't care that it uses a polling-based event tracker, go ahead and use this, but it won't be as performant as other options.

Maybe I will create or modify this to use something like [JNotify](http://jnotify.sourceforge.net/), which has cross-platform binaries for native file changes, and it works without Java 7.

## Creating Watches

Include this in your `project.clj`:

```clj
[com.klauer/java-watcher "0.1.0-SNAPSHOT"]
```

There are two ways to go about creating a watch:

### Syntax-sugary way

```clj
(register-watch "/some/path/directory/here" [:modify] #(println "hello event " %))
```

The arguments are "path", [:modify :create :delete], function.  The vector can be one of :

    * :create (creation events)
    * :modify (create and change events)
    * :delete (file/directory deletion)

Where these `kinds` mirror Java's `StandardWatchEventKinds/ENTRY_CREATE`, `ENTRY_DELETE`, and/or `ENTRY_MODIFY`.

When something changes in that directory:

```
$ cd /some/path/directory/here
$ touch make a couple files
```

You'll be greeted by your oh-so-fancy function you passed in for each of the events created (in the above example, 4 files were created):

```
hello event  {:kind #<StdWatchEventKind ENTRY_MODIFY>, :path /some/path/directory/here/a}
hello event  {:kind #<StdWatchEventKind ENTRY_MODIFY>, :path /some/path/directory/here/couple}
hello event  {:kind #<StdWatchEventKind ENTRY_MODIFY>, :path /some/path/directory/here/files}
hello event  {:kind #<StdWatchEventKind ENTRY_MODIFY>, :path /some/path/directory/here/make}
```

So define a method to call which will take in the event map of `:kind` and `:path`, so you know what file changed and how.

### More manual-control, choose-your-own-adventure way

The above is just sugar around these calls:

1. Create a directory to watch for file changes:

   ```clj
   (def example-directory (make-path "/Users/klauer/dev/clojure/java7-watcher.clj/watchabledir"))
   ```
2. Register the service so it tracks whatever `kinds` of changes to whichever directory/ies you want: `:create`, `:modify`, or `:delete`.

   ```clj
   ;; can use any combination of elements in `kinds`, even `(vals kinds)` itself
   ;; `@watch-service` is an atom for the FileSystem watch service.
   (register-with example-directory @watch-service [(:create kinds) (:delete kinds)])
   ```
   
3. call `pipeline-events-with` and pass in a function to call when something changes:

   ```clj
   (pipeline-events-with @watch-service #(println "Oh Happy Day") extra args here)
   ;; This is asynchronous through the use of the ever-nifty Lamina project (https://github.com/ztellman/lamina).
   ```
4. All registered watches are stored in the `*registered-watches*` atom, and can be removed by calling `unregister-watch`:

   ```clj
   (unregister-watch (first @*registered-watches*))
   ;; this not only removes it from the atom, but unregisters your passed-in function and any
   ;; handling of future file-system events it was registered for.
   ```


## Things Missing

There's actually a bit more I haven't gotten to:

  1. tests
  2. more fine-grained control of these watches:
     - start
     - stop
     - pause
     - etc.
  3. defining multiple functions to be called with the same watch
  4. redefining a watch and it's function to be called
  5. better granularity on functions and directory events:
     - be able to call a function for one directory event (:delete) and another for a different
       event (:create) in the same directory

## Reference

See these docs around the Java API.  There's not much in the way of wrapping these Java types, so your best bet is just to adhere to whatever the JDK defined:

  * [WatchService](http://docs.oracle.com/javase/7/docs/api/index.html?java/nio/file/WatchService.html)
  * [Java tutorial on using it](http://docs.oracle.com/javase/tutorial/essential/io/notification.html) (in Java, of course)
  * [JNotify](http://jnotify.sourceforge.net/)  (in case you would like something that works on Java 6 or natively on the Mac)
  * [JPathWatch](http://jpathwatch.wordpress.com/) is similar to JNotify, but wraps the Java 7 API, so you could drop this in your project and not have to change the API much.

## Compatibility

As this uses a Java 7 API, it is assumed you'll be running on Java 7+.

Mac OS X compatibility with this is lacking.  It works, but it's poll-based only, which at this time means you can't really track alot of changes at once (renaming the same file twice might only trigger one event).  See [this link](https://wikis.oracle.com/display/OpenJDK/Mac+OS+X+Port+Project+Status) for more information on OS X compatibility with JDK7 features.  It's coming along, but not there yet.

## License

Copyright © 2012 Nick Klauer (klauer @ gmail)

Distributed under the Eclipse Public License, the same as Clojure.
