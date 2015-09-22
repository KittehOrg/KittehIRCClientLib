KICL supports connecting to a server via SSL and does so by default, connecting to servers
on [port 6697](https://tools.ietf.org/html/rfc7194).

### Client Certificates
You can set the public and private key utilized in the connection using the following methods:

`ClientBuilder#secureKeyCertChain(File) // public key`

`ClientBuilder#secureKey(File) // private key`

`ClientBuilder#secureKeyPassword(String) // private key password`

### Validating the Server

By default, when connecting securely, KICL will utilize the default `TrustManagerFactory`
provided by the JRE you're using. This factory *does not* accept certificates issued by
many common certificate authorities (such as StartCom, which is used by many IRC networks)
and self-signed certificates.

KICL lets you set your own `TrustManagerFactory` in the `ClientBuilder` so you may let KICL
connect to the network you desire. There is also the `AcceptingTrustManagerFactory` which,
if you set it in the `ClientBuilder`, will blindly allow all connections through.

For certificate or key pinning, you currently must implement this yourself in your custom
`TrustManager`, though discussions for adding a pinning API are underway.