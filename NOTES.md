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



