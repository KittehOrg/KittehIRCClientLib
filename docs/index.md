# Kitteh IRC Client Library Documentation

The Kitteh IRC Client Library (KICL) is a powerful, modern Java IRC library built with NIO
using the Netty library to maximize performance and scalability. KICL is built with Java 8
and utilizes Java 8's Optional to avoid returning null in the API.

Some niceties about KICL include:

* [Full SSL support](advanced/ssl.md)
* [WEBIRC authentication](advanced/webirc.md)
* Fully featured CTCP support
* [IRCv3.1 and IRCv3.2 support](ircv3.md)
    * CAP negotiation
    * SASL authentication
    * Account tracking with account-notify, extended-join, and WHOX
    * Away status tracking with away-notify
    * More accurate mode tracking with multi-prefix
* Flexible authentication
    * SASL, NickServ, and GameSurge's AuthServ supported out-of-the-box
    * Custom authentication protocols can be easily written and registered
* Events model for handling information from the server.


## Getting started

KICL is designed with a simple and intuitive API.
A hello world is as simple as:

```java
Client client = Client.builder().nick("KittehBot").serverHost("127.0.0.1").build();

client.addChannel("#kitteh.org");
client.sendMessage("#kitteh.org", "Hello World!");
```

*Note: By default KICL connects over [SSL, which requires additional setup on certain networks](advanced/ssl.md).*

### Debugging

It can be useful to see input, output, and exceptions thrown while developing.

Use the `ClientBuilder` methods `listenInput`, `listenOutput`, and `listenException` to catch these little surprises.
Here is a simple example, printing all of the info to console.

```java
SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
builder.listenInput(line -> System.out.println(sdf.format(new Date()) + ' ' + "[I] " + line));
builder.listenOutput(line -> System.out.println(sdf.format(new Date()) + ' ' + "[O] " + line));
builder.listenException(Throwable::printStackTrace);
```

## Using KICL in your maven project

KICL is built and deployed using Maven. Releases are available on Maven Central. Adding it as a dependency is simple as adding the lines below to your pom.xml file:

```xml
<dependency>
    <groupId>org.kitteh.irc</groupId>
    <artifactId>client-lib</artifactId>
    <version>2.0.0</version>
    <scope>...</scope>
</dependency>
```

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
        channel.sendMessage("Hi " + event.getUser().getNick() + "!");
    }
}
```

## More information

Consult the [JavaDocs](http://kittehorg.github.io/KittehIRCClientLib/) to answer most questions.

Visit us in `#kitteh.org` on `irc.esper.net` for a chat (click
[here](http://webchat.esper.net/?nick=kicl_...&channels=kitteh.org&prompt=1) to join), or check out the
[Issue Tracker](https://github.com/KittehOrg/KittehIRCClientLib/issues) if you have trouble.
