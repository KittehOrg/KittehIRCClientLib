KICL supports connecting to Twitch IRC as well as the various Twitch-specific features there.

### Usage

```java
public class TwitchExample {
    public static void main(String[] args) {
        Client client = Client.builder()
                .server().host("irc.chat.twitch.tv").port(443)
                .password("oauth:your_twitch_oath_token_here").then()
                .nick("twitch_username_here")
                .build();
        TwitchSupport.addSupport(client);
        client.connect();
        // etc.
    }
}
```

* The server password has to be the OAuth token, with `oauth:` as a prefix.
* Nickname has to be the twitch username.
* See the `TwitchSupport` javadocs for more details.

### Capabilities

With `TwitchSupport` used, KICL requests the following capabilities:

* `twitch.tv/commands`
    * Enables Twitch-specific commands.
* `twitch.tv/membership`
    * Enables receiving JOIN/MODE/NAMES/PART.
* `twitch.tv/tags`
    * Enables receiving Twitch-specific message tags.

The various Twitch features in KICL can be found in the package
`org.kitteh.irc.client.library.feature.twitch`
