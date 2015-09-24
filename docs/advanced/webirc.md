KICL supports the WEBIRC command.

```
InetAddress ip = ...; // User's IP address
Client client = Client.builder().webirc("password", "user", "host", ip).build();
```
