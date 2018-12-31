*Please note that some server software refers to the WebIRC protocol as cgiirc in configuration files.*

KICL supports the WebIRC command which is used for indirect connections such that the IRC server is made
aware of the user's own IP address. For example, consider a webchat client. Without WebIRC support, all
connections made through the web client would appear to originate from the webserver's IP address. With
WebIRC support, the (trusted) webchat client sends the following information on connect:

* A password that is kept secret between the IRC server and the client requesting the spoofing.
* The name of the client doing the proxying and requesting the spoof (e.g. this is 'qwebirc' for Iris and
 'KiwiIRC' for Kiwi IRC). This is given as the `user` argument.
* The hostname of the user connecting to the proxy.
  * Note: The hostname and IP should resolve to each other.
* The user's IPv4/IPv6 address.

KICL supports the IRCv3-defined specification, documented [here](https://ircv3.net/specs/extensions/webirc.html).
The code sample below shows how to configure KICL to send a WEBIRC command on connect. In the example, "MyBot" is
the name of the client requesting the spoof.

```java
InetAddress ip = ...; // User's IP address
Client client = Client.builder().webIrc().password("password").gateway("MyBot").hostname("host").ip(ip).build();
```

### Further reading

* [IRCv3 WebIRC spec](https://ircv3.net/specs/extensions/webirc.html).
* [Mibbit WebIRC page](https://wiki.mibbit.com/index.php/WebIRC) - covers the structure of the WEBIRC command,
 expectations for clients and servers supporting the protocol and more.
* [qwebirc WEBIRC code](https://bitbucket.org/qwebirc/qwebirc/src/default/qwebirc/ircclient.py) - Python code for a
  WEBIRC client.
* [Atheme Iris WEBIRC code](https://github.com/atheme-legacy/iris/blob/master/qwebirc/ircclient.py) -
Python code for a WEBIRC client.
* [Charybdis WEBIRC code](https://github.com/charybdis-ircd/charybdis/blob/release/4/extensions/m_webirc.c) - Server-side code
that handles the WEBIRC command and sets the user's hostname and IP address appropriately.
