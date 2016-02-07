### Importing common root certificates

Java's default trust store does not include root certificates from a number of popular certificate authorities. This means that connections to servers which make use of certificates signed by CAs such as StartCom (e.g. EsperNet) will fail. 

It's possible to manually import the root certificates into Java's trust store which will make such connections work. This approach is preferable to making use of the `AcceptingTrustManagerFactory` or otherwise disabling certificate verification.

1. Download the root certificates in DER form. For StartCom's roots, these can be obtained using wget:

`wget https://www.startssl.com/certs/der/ca.crt https://www.startssl.com/certs/der/ca-g2.crt`

If the certificates you want to import aren't available in DER form, you can convert one in PEM form using:

`openssl x509 -in certificate.crt -out certificate.der -outform DER`

1. Locate the `cacerts` file. This is located in the `lib` of the JAVA_HOME directory. For example, `/usr/lib/jvm/java-8-oracle/jre/lib/cacerts` on a Ubuntu system.

1. Issue the import command for each certificate you'd like to import. The trust store can be protected with a password which is set by default to 'changeit'. If you've not changed it, you should be able to simply use this password. You can supply an alias for each certificate via the `-alias` argument.

`sudo keytool -trustcacerts -keystore /usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -noprompt -importcert -alias StartCom1 -file ca.crt `

You can list all certificates in your trust store and verify the certificates were correctly added by issuing `sudo keytool -trustcacerts -keystore /usr/lib/jvm/java-8-oracle/jre/lib/security/cacerts -list`

At this point, the certificates should now be imported and connections made using the JRE's default `TrustManagerFactory` should work.

