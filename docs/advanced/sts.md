### Strict transport security

Due to recent development efforts, KICL now supports the IRCv3
[STS specification](https://ircv3.net/specs/core/sts-3.3.html) which adds support for strict transport security
policies to be set by the IRC server administrators.

These policies tell the client to connect **only via a secure TLS connection** for a given duration. They also specify
the port on which the client can reach the secure service. Further details on how the system works can be found in the
[GitHub issue](https://github.com/KittehOrg/KittehIRCClientLib/issues/139) and in the aforementioned specification.

Currently, STS support in KICL is opt-in but in the future this may change to give new developers protection by default
whilst ensuring experienced developers are able to turn off the functionality.

#### Why should I care?

The internet is a scary place now. We can't afford to do things in plaintext anymore, and IRC is no exception.
There's been great success with HSTS on the web (although it's not as widespread yet as some would hope) and
IRC should follow suit.

Servers and clients that support this new capability will connect in a secure manner, even if the user misconfigures
the client to connect over a plaintext port. It will also allow server operators/admins to seamlessly upgrade users to
a secure connection if they haven't yet rolled out TLS.

#### Getting started with KICL and STS

To use STS, the client needs some way to persist information about the policies it encounters during the connection
process. Within the codebase, an interface [org.kitteh.irc.client.library.feature.sts.STSStorageManager](http://kittehorg.github.io/KittehIRCClientLib/org/kitteh/irc/client/library/feature/sts/STSStorageManager.html)
is available which you can implement yourself for complete control over policy storage.

Alternatively, a default class (currently `STSPropertiesStorageManager`) which stores the STS policies in a properties
file is available to use and built-in to KICL. A utility method makes using this default implementation very straightforward:

```java
Client client = Client.builder().nick("Kitteh").serverHost("127.0.0.1").stsStorageManager(STSUtil.getDefaultStorageManager()).build();
client.addChannel("#kicl");
```

The STS policies will be persisted in a file in the home directory with name `.kicl_sts.properties`. If you'd prefer to
store them elsewhere, you can specify a `java.nio.Path` instance when calling `STSUtil.getDefaultStorageManager`.
