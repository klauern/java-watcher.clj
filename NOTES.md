Notes on storage and persistence of the watches

- Data to potentially store:

  + watch service
  + watch keys (which register the events to watch and what directory to watch)

When processing events:

  - poll, poll(timeout), or take returns a key
  - key.pollEvents() returns a list of WatchEvents
  - event.kind returns the kind of event, and in it the path that was changed
  - key.cancel() will cancel the key
    - isValid() checks to see if the key is still valid for processing.


11/7/2012 thoughts
------------------
I've had a bit of time to work through some of the API that I wanted, and I have to say that it got
a bit unwieldy for me, so I'm considering a scrap of the API and do-over.  However, since this is
Clojure, much of the API can probably stay.  I will leverage more of the WatchService API and less
of the hand-built management of events.

Here's a rundown of thoughts I've discovered, uncovered and plan to integrate:

1. WatchService doesn't handle recursive sub-directory monitoring.  So, while you can watch for `create` events on a directory, you may see a directory get created, but it's contents will not be
triggered ever.  This is a problem in any event, and I will need to figure out a workaround.  In
looking at a link for [Walking a File Tree](http://docs.oracle.com/javase/tutorial/essential/io/walk.html), I saw that there was an example [WatchDir](http://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java) implementation that recursively adds and registers subdirectories.  I will probably use this in some spirit in `java-watcher.clj`.
2. **Registering file events shouldn't be so hard**. This is apparent in the example code:

    ```clj
    (register-watch "directory" [:create :modify :delete] #(function))
    ```

while the code is simple enough to understand, it's messy in implementation:

* how do I store events?
* How does WatchService differentiate between :modify and :create and :delete events?
* How do I unregister a watch for one type but not others?

THese are all solvable, but the implementation I chose made them very complicated.  I think in the
spirit of simplicity, it's best to just register a directory and pass a function in.  The function
SHOULD be capable of handling :create :modify and :delete events on it's own, rather than having
the service it's registered to delegate functions to call on it.  The implementation I'm thinking
would be something similar to the following:

```clj
(register-dir "Directory" #(function))
```

Where `#(function)` should expect to be passed in an `EventType` or something similar.  This
delegates responsibility to the function to handle or ignore the kinds of events it wants to.  I'm
not sure if this is simpler than having the API just call out the functions that registered for
the event types, but I think it would give the function that the user passes in more flexibility
to figure out how it wants to handle things.
