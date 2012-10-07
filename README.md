# java7-watcher.clj

Stupidly simple java7 file watch service.  This is largely a Clojure-ified wrapper around [Java7's WatchService](http://docs.oracle.com/javase/tutorial/essential/io/notification.html), which works natively on Windows and Linux, but not entirely on OSX.  Don't use this if you run a Mac since OS X [has no native file watching service](http://stackoverflow.com/a/11182515/7008), but rather uses a poll-based approach.

Maybe I will create or modify this to use something like [JNotify](http://jnotify.sourceforge.net/), which has cross-platform binaries for native file changes, and it works without Java 7.

## Usage

This will create a watch that will block until an event happens, then execute your passed-in function:

### Syntax-sugary way

```clj
(make-watch "/some/path/directory/here" [watch event kinds here] #(println "hello event " %))
```
Where `kinds` are one or more of `StandardWatchEventKinds/ENTRY_CREATE`, `ENTRY_DELETE`, and/or `ENTRY_MODIFY`, all under `kinds`.

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
2. Create a watch service

   ```clj
   (def watcher (make-watcher example-directory))  ;; needs to be a java.nio.file.Path object (as of right now)
   ```
   
3. Register the service so it tracks whatever `kinds` of changes to whichever directory/ies you want: `:create`, `:modify`, or `:delete`.

   ```clj
   ;; can use any combination of elements in `kinds`, even `(vals kinds)` itself
   (register-with watcher [(:create kinds) (:delete kinds)] example-directory)
   ```
   
4. call `wait-for` and pass in a function to call when something changes:

   ```clj
   (wait-for watcher #(println "Oh Happy Day") extra args here)
   ;; this is recursive and blocking.  It will execute your block on the event-change,
   ;; then start over to wait for the next event
   ```

## Things Missing

There's actually a bit more I haven't gotten to:

  1. pass in the event itself (with potential wrapping around the file or directory modified and what happened to it)
  2. simplify the blocking nature of it (make it non-blocking or wrap that and allow user
  to write their own handlers or start/stop handlers)
  3. more…?

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
