# Kitteh IRC Client Library Documentation

The Kitteh IRC Client Library (KICL) is a powerful, modern Java IRC library built with NIO
using the Netty library to maximize performance and scalability. KICL is built with Java 8
and utilizes Java 8's Optional to avoid returning null in the API.

Some niceties about KICL include:

* Full SSL support
* WEBIRC authentication
* Fully featured CTCP support
* CAP negotiation


## Getting started

KICL is designed with a simple and intuitive API.
A hello world is as simple as:

```java
Client client = Client.builder().nick("KittehBot").server("irc.esper.net").build();

client.addChannel("#kitteh.org");
client.sendMessage("#kitteh.org", "Hello World!");
```


## Using KICL in your maven project

KICL is built and deployed using maven, adding it as a dependency is simple as
including the Kitteh repo with

```xml
<repository>
    <id>kitteh-repo</id>
    <url>http://repo.kitteh.org/content/groups/public/</url>
</repository>
```

and adding the dependency with

```xml
<dependency>
    <groupId>org.kitteh.irc</groupId>
    <artifactId>client-lib</artifactId>
    <version>...</version>
    <scope>...</scope>
</dependency>
```

It is planned to submit KICL to Maven Central for 1.0.0 release.

## Events

KICL uses a simple event system driven by ```@Handler``` annotations.
A simple event listener example is shown below.
For more information on events, see the [Events](events.md) documentation.

```java
public class FriendlyBot {
    private Client client;

    public void connect() {
        ...; // Setting up this.client

        this.client.getEventManager().registerEventListener(this);
    }

    @Handler
    public void onJoin(ChannelJoinEvent event) {
        Channel channel = event.getChannel();
        this.client.sendMessage(channel, "Hi " + event.getUser().getNick() + "!");
    }
}
```

## More information

Consult the [JavaDocs](http://kittehorg.github.io/KittehIRCClientLib/) to answer most questions.

Visit us in `#kitteh.org` on `irc.esper.net` for a chat, or check out the
[Issue Tracker](https://github.com/KittehOrg/KittehIRCClientLib/issues) if you have trouble.