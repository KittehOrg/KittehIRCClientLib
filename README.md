# Kitteh IRC Client Library

The Kitteh IRC Client Library (KICL) is a powerful, modern Java IRC library built with NIO
using the Netty library to maximize performance and scalability.

[![Build Status](https://travis-ci.org/KittehOrg/KittehIRCClientLib.svg?branch=master)](https://travis-ci.org/KittehOrg/KittehIRCClientLib)
[![Coverage](https://img.shields.io/codecov/c/github/KittehOrg/KittehIRCClientLib/master.svg)](https://codecov.io/github/KittehOrg/KittehIRCClientLib)
![Powered by Kittens](https://img.shields.io/badge/powered%20by-kittens-blue.svg)

#### [Project News](http://kitteh.org/) | [Documentation](http://kicl.kitteh.org/) |  [JavaDocs](http://kittehorg.github.io/KittehIRCClientLib/) | [Issues](https://github.com/KittehOrg/KittehIRCClientLib/issues)

### Minimal example

```java
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent;
import org.kitteh.irc.lib.net.engio.mbassy.listener.Handler;

public class Example {
    public static class Listener {
        @Handler
        public void onUserJoinChannel(ChannelJoinEvent event) {
            if (event.getClient().isUser(event.getUser())) { // It's me!
                event.getChannel().sendMessage("Hello world! Kitteh's here to demand cuddles.");
                return;
            }
            // It's not me!
            event.getChannel().sendMessage("Welcome, " + event.getUser().getNick() + "! :3");
        }
    }

    public static void main(String[] args) {
        // Calling build() starts connecting.
        Client client = Client.builder().nick("Kitteh").serverHost("127.0.0.1").build();
        client.getEventManager().registerEventListener(new Listener());
        client.addChannel("#kicl");
    }
}
```

### Maven
Repository URL: `http://repo.kitteh.org/content/groups/public`
```xml
<groupId>org.kitteh.irc</groupId>
<artifactId>client-lib</artifactId>
<version>0.3.6</version>
```

![KICL HAS A BAD LOGO](http://i.imgur.com/KCUNexy.png)
