In addition to the [SASL mechanisms](sasl.md) which KICL supports, there are also authentication protocol classes for
popular authentication services NickServ (as implemented in Atheme) and [GameSurge](https://gamesurge.net/)'s AuthServ. These are described below:

### NickServ

The NickServ authentication protocol works by sending a private message to the "NickServ" user, using the `IDENTIFY`
command. The account name and password are provided:

```
PRIVMSG NickServ :IDENTIFY username password
```

It's possible to extend the `org.kitteh.irc.client.library.auth.protocol.NickServ` class and override the
`getNickServNick()` method if your network uses a different nick for the NickServ service (but the command syntax is
the same).

To configure the KICL client to use the NickServ authentication class:

```java
client.getAuthManager().addProtocol(new NickServ(client, "username", "password"));
```

NickServ is used with this syntax on networks which use Atheme. This includes Freenode and EsperNet amongst others.
Anope should also support this syntax.

### AuthServ

[GameSurge](https://gamesurge.net/)'s AuthServ protocol uses the "auth" command and sends it to user `AuthServ@services.gamesurge.net`:

```
PRIVMSG AuthServ@services.gamesurge.net :auth username password
```

To use the GameServ protocol with KICL:

```java
client.getAuthManager().addProtocol(new GameSurge(client, "username", "password"));
```
