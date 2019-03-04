# Kitteh IRC Client Library Documentation

The Kitteh IRC Client Library (KICL) is a powerful, modern IRC library written in Java.
Built with NIO using the Netty library, KICL aims to maximize performance and scalability.

Some niceties about KICL include:

* Events model for handling information from the server.
* Built with Java 8. Supporting streams, functions, and Optional.
* [Full TLS support](advanced/tls.md).
* [WEBIRC authentication](advanced/webirc.md).
* Fully featured CTCP support.
* Rather comprehensive [IRCv3 support](ircv3.md) including:
    * CAP negotiation.
    * Account tracking with combined power of `account-notify`, `extended-join`, and WHOX.
    * Away status tracking with `away-notify`.
    * More accurate mode tracking with `multi-prefix`.
* Flexible authentication:
    * SASL, NickServ, and GameSurge's AuthServ supported out-of-the-box.
    * Custom authentication protocols can be easily written and registered.


## Getting started

KICL is designed with a simple and intuitive API.
A hello world is as simple as:

```java
public class HelloKitteh {
    public static void main(String[] args) {
        Client client = Client.builder().nick("KittehBot").server().host("127.0.0.1").then().buildAndConnect();

        client.addChannel("#kitteh.org");
        client.sendMessage("#kitteh.org", "Hello World!");
    }
}
```

*Note: By default KICL connects over
[TLS, which requires additional setup on certain networks](advanced/tls.md).*

### Debugging

Check the [common problems](common_problems.md) page for some common situations.

It can be useful to see input, output, and exceptions thrown while developing.
Use the `Client.Builder` to set listeners to catch these little surprises.
Here is a simple example, printing all of the info to the console:

```java
public class DebugKitteh {
    public static void main(String[] args) {
        Client.Builder builder = Client.builder();

        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        builder.listeners().input(line -> System.out.println(sdf.format(new Date()) + ' ' + "[I] " + line));
        builder.listeners().output(line -> System.out.println(sdf.format(new Date()) + ' ' + "[O] " + line));
        builder.listeners().exception(Throwable::printStackTrace);
        // and then build() or buildAndConnect()
    }
}
```

## Using KICL in your project

KICL is built and deployed using Maven. Releases are available on Maven Central. Adding it as a dependency is
simple as adding the lines below to your pom.xml file:

```xml
<dependency>
    <groupId>org.kitteh.irc</groupId>
    <artifactId>client-lib</artifactId>
    <version>6.0.1</version>
    <scope>...</scope>
</dependency>
```

Or, for Gradle:

```
compile "org.kitteh.irc:client-lib:6.0.1"
```

## Events

KICL uses a simple event system driven by `@Handler` annotations.
A simple event listener example is shown below.
For more information on events, see the [Events](events.md) documentation.

```java
public class FriendlyBot {
    private Client client;

    public void connect() {
        // Set up this.client before the below line!

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
[here](https://webchat.esper.net/?nick=kicl_...&channels=%23kitteh.org) to join), or check out the
[Issue Tracker](https://github.com/KittehOrg/KittehIRCClientLib/issues) if you have trouble.
