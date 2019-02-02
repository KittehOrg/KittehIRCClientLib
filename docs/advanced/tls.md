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
connect to the network you desire. There is also the `AcceptingTrustManagerFactory` which,
if you set it in the `Client.Builder`, will blindly allow all connections through.

For certificate or key pinning, you currently must implement this yourself in your custom
`TrustManager`, though discussions for adding a pinning API are underway.
