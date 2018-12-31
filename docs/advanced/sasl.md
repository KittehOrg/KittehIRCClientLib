KICL supports using SASL to authenticate with an IRC server. As described in the
[IRCv3.1 spec](http://ircv3.net/specs/extensions/sasl-3.1.html), SASL allows the client to authenticate using a SASL
mechanism and the `AUTHENTICATE` command. The client must have requested the `sasl` capability to be able to use the
AUTHENTICATE command.

KICL's SASL support can be configured by calling the `Client#getAuthManager` method which returns an
[AuthManager](http://kittehorg.github.io/KittehIRCClientLib/org/kitteh/irc/client/library/auth/AuthManager.html). The
manager can be used to manage authentication protocols. For SASL, there are currently three available authentication
protocols which [extend](http://kittehorg.github.io/KittehIRCClientLib/org/kitteh/irc/client/library/auth/protocol/class-use/AbstractSaslProtocol.html)
the `AbstractSaslProtocol` class. It's also possible to add [non-SASL](alt_auth.md) authentication protocols.

Protocols can be added by calling the `AuthManager#addProtocol` method, for example:

```java
client.getAuthManager().addProtocol(new SaslPlain(client, "accountname", "password"));
```

The three available mechanisms are described below. The server-side SASL authentication logic is almost always performed
by the services package (e.g. Atheme) and these services packages may support authentication mechanisms which KICL does
not. If that's the case, it should be easy enough to extend `AbstractSaslProtocol`` yourself.

### PLAIN Mechanism

Formally described in [RFC 4616](https://tools.ietf.org/html/rfc4616), the PLAIN mechanism sends an authcid (account name
to login with), password and optionally an authzid (account name to impersonate) **in plaintext**. As mentioned in the RFC,
this mechanism **SHOULD NOT** be used without adequate socket security. In practice, this means the PLAIN mechanism
should never be used without TLS.

The authzid, authcid and password are separated by a null byte. If the authzid is omitted the null byte must still
appear at the start of the AUTHENTICATE argument. The argument is base64 encoded. To use the PLAIN mechanism with KICL,
call the `AuthManager#addProtocol` method and supply an instance of `SaslPlain`:

```java
Client client = Client.builder().nick("Kitteh").server().host("127.0.0.1").then().build();
client.getAuthManager().addProtocol(new SaslPlain(client, "Kitteh", "password"));
```

KICL does not currently support impersonation - this is only really applicable to services administrators in the context
of IRC.

### EXTERNAL Mechanism

For Atheme, the EXTERNAL mechanism looks at the client's SSL certificate and checks to see if the fingerprint has been
associated with a services account. The client will become authenticated if so.

```java
Client client = Client.builder().nick("Kitteh").server().host("127.0.0.1").secureKeyCertChain(null).secureKey(null).then().build();;
client.getAuthManager().addProtocol(new SaslExternal(client));
```

See the [SSL docs](ssl.md) for more information on the `secureKeyCertChain` and `secureKey` methods.

### ECDSA-NIST256P-CHALLENGE Mechanism

This mechanism uses an elliptic curve keypair and a challenge from the server, which the client signs. The result is
sent to the server and checked against the public key stored with the NickServ account. If the signature is valid, the
user is logged in to their account.

Please see the [specific docs](ecdsa.md) for this mechanism for more information.
