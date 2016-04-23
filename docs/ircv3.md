The following IRCv3 features are supported out of the box.

See the `CapabilityManager` and `CapabilityRequestCommand` for more details.

### IRCv3.1

CAP | multi-prefix | sasl | account-notify | away-notify | extended-join | tls
:-: | :----------: | :--: | :------------: | :---------: | :-----------: | :-:
✔   | ✔            | ✔    | ✔              | ✔           | ✔             | ✘

##### Notes
* SASL mechanisms supported and the classes for using them:
    * PLAIN - `SaslPlain`
    * [ECDSA-NIST256P-CHALLENGE](advanced/ecdsa.md) - `SaslECDSANIST256PChallenge`
    * EXTERNAL - `SaslExternal`
* The TLS extension, which is a STARTTLS approach, will likely never be supported. It is horrible and evil and should be destroyed!
    * Use the [SSL](advanced/ssl.md) support that KICL has built-in instead.


### IRCv3.2

CAP | Message Tags | Metadata | Monitor | account-tag | batch | cap-notify
:-: | :----------: | :------: | :-----: | :---------: | :---: | :--------:
✔   | ✔            |✘         | ✔       | ✔           | ✘     | ✔

chghost | echo-message | invite-notify | sasl | server-time | userhost-in-names
:-----: | :----------: | :-----------: | :--: | :---------: | :---------------:
✔       | ✔            | ✔             | ✔    | ✔           | ✔
