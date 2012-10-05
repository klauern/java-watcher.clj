# java7-watcher.clj

Stupidly simple java7 file watch service.  This is largely a Clojure-ified wrapper around [Java7's WatchService](http://docs.oracle.com/javase/tutorial/essential/io/notification.html), which works natively on Windows and Linux, but no so on OSX.  Don't use this if you run a Mac since OS X [has no native file watching service](http://stackoverflow.com/a/11182515/7008), nobody probably wrote anything to tie in to it yet.

Maybe I will create or modify this to use something like [JNotify](http://jnotify.sourceforge.net/), which has cross-platform binaries for native file changes, and it works without Java 7.

## Usage

I can't say I really made this into much of an API, but here's where we are so far:

1. Create a directory to watch for file changes:

```clj
    (def example-directory (make-path "/Users/klauer/dev/clojure/java7-watcher.clj/watchabledir"))
```

2. Create a watch service

```clj
    (def example-directory (make-path "/Users/me/whatever/directory/you/want"))
```


3. Register the service so it tracks whatever `kinds` of changes to whichever directory/ies you want: `:create`, `:modify`, or `:delete`.

```clj
    (def watch (make-watcher example-directory))
```


4. call `wait-for` and pass in a function to call when something changes:

```clj
    (wait-for watch #(println "Oh Happy Day"))
```

## Reference

See these docs around the Java API.  There's not much in the way of wrapping these Java types, so your best bet is just to adhere to whatever the JDK defined:

  * [WatchService](http://docs.oracle.com/javase/7/docs/api/index.html?java/nio/file/WatchService.html)
  * [Java tutorial on using it](http://docs.oracle.com/javase/tutorial/essential/io/notification.html) (in Java, of course)
  * [JNotify](http://jnotify.sourceforge.net/)  (in case you would like something that works on Java 6 or natively on the Mac)

## License

Copyright © 2012 Nick Klauer (klauer @ gmail)

Distributed under the Eclipse Public License, the same as Clojure.
