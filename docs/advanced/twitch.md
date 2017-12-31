KICL supports connecting to Twitch IRC as well as the various Twitch-specific features there.

### Usage

```java
public class TwitchExample {
    public static void main(String[] args) {
        Client client = Client.builder()
                .serverHost("irc.chat.twitch.tv").serverPort(443)
                .serverPassword("oauth:your_twitch_oath_token_here")
                .nick("twitch_username_here")
                .messageSendingQueueSupplier(TwitchDelaySender.getSupplier(false))
                .build();
        client.getEventManager().registerEventListener(new TwitchListener(client));
        client.connect();
        // etc.
    }
}
```

* The server password has to be the OAuth token, with `oath:` as a prefix
* Nickname has to be the twitch username.
* The TwitchDelaySender obeys Twitch rules on message sending.
* The `TwitchListener` class registers Twitch-specific message tags and listens
for Twitch-specific events like commands.

### Capabilities

With `TwitchListener`, KICL requests the following capabilities:

* `twitch.tv/commands`
  * Enables Twitch-specific commands.
* `twitch.tv/membership`
  * Enables receiving JOIN/MODE/NAMES/PART.
* `twitch.tv/tags`
  * Enables receiving Twitch-specific message tags.

The various Twitch features in KICL can be found in the package
`org.kitteh.irc.client.library.feature.twitch`