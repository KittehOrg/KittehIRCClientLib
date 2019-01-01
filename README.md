# Kitteh IRC Client Library

The Kitteh IRC Client Library (KICL) is a powerful, modern Java IRC library built with NIO
using the Netty library to maximize performance and scalability.

[![Build Status](https://travis-ci.org/KittehOrg/KittehIRCClientLib.svg?branch=master)](https://travis-ci.org/KittehOrg/KittehIRCClientLib)
[![Coverage](https://img.shields.io/codecov/c/github/KittehOrg/KittehIRCClientLib/master.svg)](https://codecov.io/github/KittehOrg/KittehIRCClientLib)
![Powered by Kittens](https://img.shields.io/badge/powered%20by-kittens-blue.svg)

#### [Project News](http://kitteh.org/) | [Documentation](http://kicl.kitteh.org/) |  [JavaDocs](http://kittehorg.github.io/KittehIRCClientLib/) | [Issues](https://github.com/KittehOrg/KittehIRCClientLib/issues)

### Minimal example

```java
import net.engio.mbassy.listener.Handler;

import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent;

public class Example {
    public static class Listener {
        @Handler
        public void onUserJoinChannel(ChannelJoinEvent event) {
            if (event.getClient().isUser(event.getUser())) { // It's me!
                event.getChannel().sendMessage("Hello world! Kitteh's here for cuddles.");
                return;
            }
            // It's not me!
            event.getChannel().sendMessage("Welcome, " + event.getUser().getNick() + "! :3");
        }
    }

    public static void main(String[] args) {
        Client client = Client.builder().nick("Kitteh").server().host("127.0.0.1").then().buildAndConnect();
        client.getEventManager().registerEventListener(new Listener());
        client.addChannel("#kicl");
    }
}
```

### Maven
```xml
<dependency>
    <groupId>org.kitteh.irc</groupId>
    <artifactId>client-lib</artifactId>
    <version>5.0.0</version>
</dependency>
```

Releases are available on Maven Central.

Snapshots are available on the Sonatype OSSRH: https://oss.sonatype.org/content/repositories/snapshots

### Branches

**master** branch tracks releases and may occasionally contain bug fixes or JavaDoc changes prior to release.

**next** branch contains code for upcoming releases and is always working toward the next major or minor release.

### Logo?

![KICL HAS A BAD LOGO](http://i.imgur.com/KCUNexy.png)
