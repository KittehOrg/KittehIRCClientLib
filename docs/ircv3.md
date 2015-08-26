The following

### IRCv3.1

CAP | multi-prefix | sasl | account-notify | away-notify | extended-join | tls
:-: | :----------: | :--: | :------------: | :---------: | :-----------: | :-:
✔   | ✔            | ✔    | ✔              | ✔           | ✔             | ✘

##### Notes
* SASL PLAIN is the only mechanism currently supported
* TLS will likely never be supported. It is horrible and evil and should be destroyed!


### IRCv3.2

CAP | Metadata | Monitor | account-tag | batch | cap-notify | chghost | echo-message | invite-notify | sasl | server-time | userhost-in-names
:-: | :------: | :-----: | :---------: | :---: | :--------: | :-----: | :----------: | :-----------: | :--: | :---------: | :---------------:
✔   | ✘        | ✘       | ✘           | ✘     | ✔          | ✘       | ✘            | ✔             | ✔    | ✘           | ✘