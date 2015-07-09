### WebIRC

KICL supports the WEBIRC command.

```
InetAddress ip = ...; // User's IP address
Client client = new ClientBuilder().webirc("password", "user", "host", ip).build();
```