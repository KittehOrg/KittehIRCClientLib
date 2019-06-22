### Importing common root certificates

Java's default trust store may not include the certificate authority (such as self-signed) used by your IRC. This
means that connections to servers which make use of certificates signed by these CAs will
fail.

It's possible to manually import the root certificates into Java's trust store which will make such connections work.
This approach is preferable to making use of the `InsecureTrustManagerFactory` or otherwise disabling certificate
verification.

1. Download the certificate in DER form.

```sh
openssl s_client -showcerts -connect irc.host:6697 < /dev/null | openssl x509 -outform DER > cert.der
```

1. Locate the `cacerts` file. This is located in the `lib` of the JAVA_HOME directory. For example,
`/usr/lib/jvm/java-8-oracle/jre/lib/cacerts` on a Ubuntu system.

1. Issue the import command for each certificate you'd like to import. The trust store can be protected with a password
which is set by default to 'changeit'. If you've not changed it, you should be able to simply use this password. You can
supply an alias for each certificate via the `-alias` argument.

```sh
sudo keytool -trustcacerts -keystore /usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -noprompt -importcert -alias irchost -file cert.der
```

You can list all certificates in your trust store and verify the certificates were correctly added by issuing

```sh
sudo keytool -trustcacerts -keystore /usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -list
```

At this point, the certificates should now be imported and connections made using the JRE's default `TrustManagerFactory`
should work.

