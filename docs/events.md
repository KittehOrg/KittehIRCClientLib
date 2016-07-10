## Basics

KICL utilizes the [MBassador](https://github.com/bennidi/mbassador) event bus.

Listening to an event is as simple as follows:
```java
@Handler
public void meow(ClientConnectedEvent event) {
    System.out.println("I am connected!");
}
```

The requirements:
* The `@Handler` annotation indicates this method should receive events.
* The event (or super class) you wish to listen to should be the only parameter.
* The method must be public.

Methods can be named however you like, and can be inside classes of any scope.

## Event Firing

By default, events fired for a single Client block further processing of incoming lines.
Long processing of an event will not disconnect the client as this handling is on a separate
thread and the client happily continues to respond to PING requests (and send PINGs as necessary).

Handlers by default fire in series, blocking until all are complete. You can set your handler to
not block by utilizing the annotations `delivery` value to `Invoke.Asynchronously`.

Handlers can also have priority. If you wish to listen to the same event multiple times, priority
can be helpful to ensure the your handler methods fire in a consistent order. The `priority` value
on the annotation allows setting a priority. The default priority is 0.

Handlers by default listen to the parameter class as well as any subclasses. If you truly wished,
you could listen to `ClientEvent` and receive all events defined by KICL. If you only want to
listen to the exact class, utilize the annotation's `rejectSubtypes` value.

## KICL Events
KICL events cover nearly all common IRC interactions. See the
[JavaDocs](http://kittehorg.github.io/KittehIRCClientLib/) for the complete listing.

For all situations where KICL doesn't provide the event you need, you can listen to the server's
messages. The `ClientReceiveCommandEvent` and `ClientReceiveNumericEvent` classes let you listen
to all incoming messages. Filters exist to let you choose a specific command.
Below is an example from the JavaDocs:

```java
@CommandFilter("PRIVMSG")
@Handler
public void privmsg(ClientReceiveCommandEvent event) {
    System.out.println("We get signal!");
}
```
