### Strict transport security

Due to recent development efforts, KICL now supports the IRCv3
[STS specification](https://ircv3.net/specs/core/sts-3.3.html) which adds support for strict transport security
policies to be set by the IRC server administrators.

These policies tell the client to connect **only via a secure TLS connection** for a given duration. They also specify
the port on which the client can reach the secure service. Further details on how the system works can be found in the
[GitHub issue](https://github.com/KittehOrg/KittehIRCClientLib/issues/139) and in the aforementioned specification.

### Getting started with KICL and STS

To use STS, the client needs some way to persist information about the policies it encounters during the connection
process. Within the codebase, an interface [org.kitteh.irc.client.library.feature.sts.STSStorageManager](http://kittehorg.github.io/KittehIRCClientLib/org/kitteh/irc/client/library/feature/sts/STSStorageManager.html)
is available which you can implement yourself.

Alternatively, a simple class `STSPropertiesStorageManager` which stores the STS policies in a properties file is
available to use and built-in to KICL.

```java

Client client = Client.builder().nick("Kitteh").serverHost("127.0.0.1").build();
client.getEventManager().registerEventListener(new Listener());
client.addChannel("#kicl");
