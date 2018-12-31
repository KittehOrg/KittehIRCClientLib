In addition to the [SASL mechanisms](sasl.md) which KICL supports, there are also authentication protocol classes for
popular authentication services NickServ (as implemented in Atheme) and [GameSurge](https://gamesurge.net/)'s AuthServ. These are described below:

### NickServ

The NickServ authentication protocol works by sending a private message to the "NickServ" nickname, using the `IDENTIFY`
command. The account name and password are provided:

```
PRIVMSG NickServ :IDENTIFY accountname password
```

To configure the KICL client to use the NickServ authentication class:

```java
client.getAuthManager().addProtocol(NickServ.builder(client).account("accountname").password("password").build());
```

NickServ is used with this syntax on networks which use Atheme. This includes Freenode and EsperNet amongst others.
Anope should also support this syntax. See the javadocs for more customization.

### AuthServ

[GameSurge](https://gamesurge.net/)'s AuthServ protocol uses the "auth" command and sends it to `AuthServ@services.gamesurge.net`:

```
PRIVMSG AuthServ@services.gamesurge.net :auth accountname password
```

To use the GameServ protocol with KICL:

```java
client.getAuthManager().addProtocol(new GameSurge(client, "accountname", "password"));
```
