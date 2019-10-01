Below are some common problems encountered by users of KICL, and solutions to those problems.

#### A KittehServerMessageException printed to console

If KICL cannot parse a message type that it is written to handle, it will create a detailed
exception for this problem. This sort of exception does not halt processing of further messages,
and does not cause KICL to disconnect from the server or otherwise die. By default, KICL outputs
its various exceptions to console. To change this behavior, see the Debugging section of the
[main documentation page](index.md).

#### I'm getting nagged about my connection?

If you aren't using TLS, connecting over plain old 6667 without encryption, you will get nagged
about how insecure this is. You won't encounter this issue if you enable [STS](advanced/sts.md),
which will ensure your connection is upgraded to a secure one when possible. Alternatively, you
may find yourself getting a nag when you utilize a known insecure trust manager factory. You
really shouldn't be doing that! ;)

#### I'm getting weird exceptions about the certificate.

If the server you are trying to connect to is signed using StartCom/WoSign, they are a bit
out of date and using a certificate not really trusted by anyone anymore. For more information
[click here](https://security.googleblog.com/2016/10/distrusting-wosign-and-startcom.html).
If the server is using LetsEncrypt, you may be using a very old JRE. Try updating your JRE to
a more recent release.

#### Exceptions connecting to Twitch

Make sure you utilize the `TwitchSupport` class. Check out [advanced/twitch.md] for more details.

#### It's responding to CTCP queries!

By default, KICL has the following responses to CTCP:

* **VERSION** responds with "I am Kitteh!"
* **TIME** responds with the current time
* **FINGER** responds with "om nom nom tasty finger"
* **PING** responds with the same message

These can all be modified or prevented through `PrivateCtcpQueryEvent` using `getReply()` and `setReply()`.

#### I still need help!

Head on over to the [GitHub Issues](https://github.com/KittehOrg/KittehIRCClientLib/issues) page
and look for an existing ticket or create your own.
