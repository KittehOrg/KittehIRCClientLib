KICL supports connecting to a server via SSL.

To utilize this functionality, simply call the `secure(boolean)` method.

```
Client client = Client.builder().secure(true).server("irc.esper.net").server(6697).build();
```

*Don't forget that SSL is generally on a non-standard port (typically 6697).*

### Client Certificates
You can set the public and private key utilized in the connection using the following methods:

`ClientBuilder#secureKeyCertChain(File) // public key`

`ClientBuilder#secureKey(File) // private key`

`ClientBuilder#secureKeyPassword(String) // private key password`

### Validating the Server

Want to ensure you are connecting to the expected server? Utilize the `SSLCertificateAcceptEvent`!
It provides the authentication type and `X509Certificate` chain as provided by the `X509TrustManager`'s `checkServerTrusted` method.
By default the connection is accepted, so be sure to listen to this event if you have concerns about trusting the connection.