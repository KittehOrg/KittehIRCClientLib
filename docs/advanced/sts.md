### Strict transport security

Due to recent development efforts, KICL now supports the draft IRCv3
[STS specification](https://ircv3.net/specs/core/sts-3.3.html) which adds support for strict transport security
policies to be set by the IRC server administrators.

These policies tell the client to connect **only via a secure TLS connection** for a given duration. They also specify
the port on which the client can reach the secure service. Further details on how the system works can be found in the
[GitHub issue](https://github.com/KittehOrg/KittehIRCClientLib/issues/139) and in the aforementioned specification.

Currently, STS support in KICL is opt-in but in the future this may change to give new developers protection by default
whilst ensuring experienced developers are able to turn off the functionality.

#### Why should I care?

With modern libraries, encryption of communication can be performed seamlessly to increase the privacy of IRC messaging.
This is well-aligned with current best practices with other technologies, such as HTTP, and the internet as a whole.

Servers and clients that support this new capability will connect in a secure manner, even if the user misconfigures
the client to connect over a plaintext port. It will also allow server operators/admins to seamlessly upgrade users to
a secure connection if they haven't yet rolled out TLS.

#### Getting started with KICL and STS

To use STS, the client needs some way to persist information about the policies it encounters during the connection
process. Within the codebase, an interface
[org.kitteh.irc.client.library.feature.sts.StsStorageManager](http://kittehorg.github.io/KittehIRCClientLib/org/kitteh/irc/client/library/feature/sts/StsStorageManager.html)
is available which you can implement yourself for complete control over policy storage.

Alternatively, a default class (currently `StsPropertiesStorageManager`) which stores the STS policies in a properties
file is available to use and built-in to KICL. A utility method makes using this default implementation very straightforward:

```java
Client client = Client.builder().serverHost("irc.kitteh.org").stsStorageManager(StsUtil.getDefaultStorageManager()).build();
client.addChannel("#kicl");
```

The STS policies will be persisted in a file in the home directory with name `.kicl_sts.properties`. If you'd prefer to
store them elsewhere, you can specify a `java.nio.Path` instance when calling `StsUtil.getDefaultStorageManager`.

Now, when the client connects it will automatically obey any relevant policies it has found.

#### Adding your own policies

```java
StsPolicy policy = StsUtil.getStsPolicyFromString(",", "port=6697");
client.getStsMachine().get().getStorageManager().addEntry("irc.kitteh.org", 5000, policy);
```

### Testing STS

**This section is aimed at KICL developers/contributors**

The InspIRCd test network has support for STS, currently using a CAP key of "draft/sts". Due to a
[recent spec change](https://github.com/ircv3/ircv3-specifications/commit/c0fcd05aceaa7f117d438ebc31814e1d49226967),
KICL's implementation currently also uses this key.

There is a simple [Charybdis module](https://github.com/lol768/charybdis/blob/release/4/extensions/sts_module.c)
that was created as part of the work on this functionality for testing purposes only. This has also been updated
to use the draft key.
