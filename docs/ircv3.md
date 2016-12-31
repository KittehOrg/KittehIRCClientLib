The following IRCv3 features are supported out of the box.

See the `CapabilityManager` and `CapabilityRequestCommand` for more details.

### IRCv3.1

CAP | multi-prefix | sasl | account-notify | away-notify | extended-join | tls
:-: | :----------: | :--: | :------------: | :---------: | :-----------: | :-:
✔   | ✔            | ✔    | ✔              | ✔           | ✔             | ✘


### IRCv3.2

CAP | Message Tags | Metadata | Monitor | account-tag | batch | cap-notify
:-: | :----------: | :------: | :-----: | :---------: | :---: | :--------:
✔   | ✔            |✘         | ✔       | ✔           | ✘     | ✔

chghost | echo-message | invite-notify | sasl | server-time | userhost-in-names
:-----: | :----------: | :-----------: | :--: | :---------: | :---------------:
✔       | ✔            | ✔             | ✔    | ✔           | ✔

### IRCv3 draft specifications

STS | message-tags | SNI
:-: | :----------: | :-:
✔   | ✘            |✔

#### Notes
* The following capabilities are supported but are not automatically requested:
    * SASL
        * This is requested if the below mechanism classes are used and given to the AuthManager.
    * echo-message
        * After requesting you can also use the annotation filter `@EchoMessage` on events to only receive echoed messages.
    * invite-notify
* SASL mechanisms supported and the classes for using them:
    * PLAIN - `SaslPlain`
    * [ECDSA-NIST256P-CHALLENGE](advanced/ecdsa.md) - `SaslECDSANIST256PChallenge`
    * EXTERNAL - `SaslExternal`
* The TLS extension, which is a STARTTLS approach, will likely never be supported. It is horrible and evil and should be destroyed!
    * Use the [SSL](advanced/ssl.md) and [STS](advanced/sts.md) support that KICL has built-in instead.
* Metadata has been deprecated (IRCv3.2). It will not be implemented until the new spec is written.
