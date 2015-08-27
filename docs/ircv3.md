The following IRCv3 features are supported out of the box.

See the `CapabilityManager` and `CapabilityRequestCommand` for more details.

### IRCv3.1

CAP | multi-prefix | sasl | account-notify | away-notify | extended-join | tls
:-: | :----------: | :--: | :------------: | :---------: | :-----------: | :-:
✔   | ✔            | ✔    | ✔              | ✔           | ✔             | ✘

##### Notes
* SASL PLAIN is the only mechanism currently supported. See `SaslPlain` for more.
* The TLS extension, which is a STARTTLS approach, will likely never be supported. It is horrible and evil and should be destroyed!
    * Use the SSL support that KICL has built-in instead.


### IRCv3.2

CAP | Metadata | Monitor | account-tag | batch | cap-notify | chghost
:-: | :------: | :-----: | :---------: | :---: | :--------: | :-----:
✔   | ✘        | ✘       | ✘           | ✘     | ✔          | ✘

echo-message | invite-notify | sasl | server-time | userhost-in-names
:----------: | :-----------: | :--: | :---------: | :---------------:
✔            | ✔             | ✔    | ✘           | ✔