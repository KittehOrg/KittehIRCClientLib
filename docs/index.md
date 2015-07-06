# Kitteh IRC Client Library Documentation

KICL is a powerful, modern Java IRC library built with NIO 
using the Netty library to maximize performance and scalability.

Some niceties about KICL include:

* Full SSL support
* WEBIRC authentication
* Fully featured CTCP support
* CAP negotiation


## Getting started

KICL designed with a simple and intuitive API,
a hello world is as simple as:

```java
Client client = new ClientBuilder().name("KittehBot").server("irc.gamesurge.net").build();

client.addChannel("#kitteh.org");
client.sendMessage("#kitteh.org", "Hello World!");
```


## Using KICL in your maven project

KICL is built and deployed using maven, adding it as a dependency is simple as
including the kitteh repo with

```xml
<repository>
    <id>kitteh-repo</id>
    <url>http://repo.kitteh.org/content/groups/public/</ulr>
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

## Events

KICL uses a simple event system driven by ```@Handler``` annotations, a simple
event listener example is shown below

```java
public class Bot {
    private Client client;

    public void connect() {
        ...;

        client.getEventManager().registerEventListener(this);
    }

    @Handler
    public void onJoin(ChannelJoinEvent event) {
        Channel channel = event.getChannel();
        client.sendMessage(channel, "Hi " + event.getUser().getNick() + "!");
    }
}
```