KICL supports connecting to a server via TLS and does so by default, connecting to servers
on [port 6697](https://tools.ietf.org/html/rfc7194).

### Client Certificates
You can set the public and private key utilized in the connection using the following methods:

`Client.Builder.Server#secureKeyCertChain(Path) // X.509 certificate chain file in PEM format`

`Client.Builder.Server#secureKey(Path) // PKCS#8 private key file in PEM format`

`Client.Builder.Server#secureKeyPassword(String) // private key password`

### Validating the Server

By default, when connecting securely, KICL will utilize the default `TrustManagerFactory`
provided by the JRE you're using. This factory *does not* necessarily accept certificates
issued by all certificate authorities (such as StartCom, which is still used by some IRC networks)
and self-signed certificates. If possible, you should consider [importing](tls_import.md)
the root certificates for these certificate authorities which will allow connections to be
made.

KICL lets you set your own `TrustManagerFactory` in the `Client.Builder` so you may let KICL
connect to the network you desire. For testing, there is also the `InsecureTrustManagerFactory`
which, if you set it in the `Client.Builder`, will blindly allow all connections through.

For certificate or key pinning, you currently must implement this yourself in your custom
`TrustManager`, though discussions for adding a pinning API are underway.

### TLS Exceptions

If something goes wrong, you may get a `ClientConnectionFailedEvent` with `getCause()`
containing a "SSLEngine problem" and a chain of causes. At the bottom should be the actual
cause of this situation

#### Certificate Expired

The exception may say something like `NotAfter: Mon Jun 10 08:15:43 EDT 2019` which means the
certificate was only valid until that point in time. You should let the admins of that IRC server
know of their mistake. In the mean time, you could work around it by connecting using the
`InsecureTrustManagerFactory`. If writing a client, you could prompt the user to decide if they
want to make this technically risky decision.

#### Self-signed certificate

You could import the certificate! Or, use your own trust manager.
