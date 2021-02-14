KICL supports the ECDSA-NIST256P-CHALLENGE SASL authentication mechanism. This
document describes both how the mechanism works and how the library can be used
to authenticate with this method.

### Using ECDSA with KICL

To get started, a public-private keypair is required. You can do this with KICL
or by using OpenSSL commands and importing the key.

#### Generating a keypair using KICL

It's possible to programmatically generate a keypair and retrieve the encoded
public key for use with NickServ:

```java
SaslEcdsaNist256PChallenge.ECKeyPair ecKeyPair = SaslEcdsaNist256PChallenge.getNewKey();
System.out.println("/msg NickServ SET PUBKEY " + SaslEcdsaNist256PChallenge.getCompressedBase64PublicKey(ecKeyPair.getPublic()));
```

You can get a base64 representation of the private key. This could then be
stored in e.g. a config file:

```java
String base64Representation = SaslEcdsaNist256PChallenge.base64Encode(ecKeyPair.getPrivate());
```

You can get a private key back from a base64 representation using:

```java
ECPrivateKey privateKey = SaslEcdsaNist256PChallenge.getPrivateKey(base64Representation);
```

#### Generating a keypair using OpenSSL

First, we generate the public and private keys in PEM format:

```sh
openssl ecparam -genkey -out private_key.pem -outform PEM -name prime256v1
```

```sh
openssl ec -in private_key.pem -inform PEM -out public_key.pem -outform PEM -pubout
```

Next, we convert the PEM files into DER files which can be read with Java:

```sh
openssl pkey -inform pem -outform der -in private_key.pem -out public_key.der -pubout
```

```sh
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
```

Now, we base64 the two DER files so they can be used as arguments to the `getPublicKey`
and `getPrivateKey` static methods on the `SaslEcdsaNist256PChallenge` class:

```sh
cat public_key.der | base64 | tr -d "\n"
```

```sh
cat private_key.der | base64 | tr -d "\n"
```

Finally, we retrieve the public key using point compression in a form Atheme will understand:

```sh
openssl ec -noout -text -conv_form compressed -in private_key.pem | grep '^pub:' -A 3 | tail -n 3 | tr -d ' \n:' | xxd -r -p | base64 | tr -d "\n"
```

You can use `/msg NickServ SET PUBKEY <pubkey>` where `<pubkey>` is the output from the command above.

#### Telling KICL to use the key

To get KICL to do the authentication, call the `addProtocol` method on the `AuthManager` instance:

```java
client.getAuthManager().addProtocol(new SaslEcdsaNist256PChallenge(client, "accountname", privateKey));
```

### Protocol description

This section of the document describes the inner workings of the ECDSA SASL authentication
mechanism. If you're just looking to use KICL to do this authentication and aren't too
concerned about how it works under the hood, you can ignore this section. From here on in
the description of the authentication mechanism is not specific to KICL.

"ecdsa-nist256p-challenge" is a SASL authentication mechanism which is supported by KICL
and some other IRC clients (e.g. weechat). In order to use this mechanism, SASL must be
requested by the client prior to any authentication attempts (`CAP REQ :sasl`). Once the
server sends an `CAP ACK`, authentication can begin.

The mechanism makes use of the NIST P-256 curve (`NID_X9_62_prime256v1` in OpenSSL) which
(per the name) is defined over a 256 bit prime finite field. The parameters for this curve
are available [from the NSA](https://www.nsa.gov/ia/_files/nist-routines.pdf) in section 4.3.
The curve itself doesn't fit the [SafeCurves requirements](https://safecurves.cr.yp.to/).

Atheme is the only IRC services software that appears to implement this authentication method.
 Atheme's implementation requires that:

* Users make use of the `/msg NickServ SET PUBKEY <pubkey>` command to tell the services
package their public key
    * `<pubkey>` in this case is the **point compressed** X9.62 representation of the public
     key which is then base64 encoded. Please see section 2.3.3 of the [SEC1](https://www.secg.org/sec1-v2.pdf)
     spec for more information. Public keys making use of the uncompressed or hybrid forms will
     **not work**. Java users may wish to implement their own method for applying point compression
     since it is unavailable in the standard library. See the implementation in [KICL](https://github.com/KittehOrg/KittehIRCClientLib/commit/448ae6bf18956b5a38e0da8f87486c5db85db880).

#### Step 1 - Begin authentication

The client sends `AUTHENTICATE ECDSA-NIST256P-CHALLENGE` to request that the authentication
process be started. See the [SASL documentation](https://ircv3.net/specs/extensions/sasl-3.1.html)
for more information on the SASL process.

#### Step 2 - Wait for server acknowledgement

The server will send back a `AUTHENTICATE +` message. The + here denotes an empty message.
Once this message has been seen, the client can begin sending the account information.

#### Step 3 - Send encapsulated account name information

The account name to authenticate with, along with the identity to impersonate are concatenated
and joined together by \0 (NUL byte, U+0000) characters. For example, logging in as account Example and impersonating Test:

`Example\0Test\0`

In the vast majority of cases, the identity to impersonate will be identical to the
identity used to authenticate:

`Example\0Example\0`

KICL does not support impersonation at this time and it appears to only be relevant to
services administrators.

Once the string has been built, it is base64 encoded and sent via the `AUTHENTICATE` command:

`AUTHENTICATE RXhhbXBsZQBFeGFtcGxlAA==`

#### Step 4 - Receive challenge from server

The server will now send a 32 byte challenge (this matches the length of a SHA256 signature),
encoded as base64. The client must **decode** the challenge (do not sign the base64 text
representation directly) and sign it, using the account's private key. The signature should be stored.

**Important:** Many signature APIs accept data and then perform a cryptographic hash function
(e.g. SHA1 or SHA256) over the data and sign the message digest instead of the provided data.
This is a good approach for many usecases, especially when a lot of data is involved but it's
important you **sign the provided challenge directly** for this process - Atheme does not
support checking a cryptographic signature of the challenge. Java users should use `NoneWithECDSA`
signature algorithm to achieve this.

#### Step 5 - Send signature to server

Now that the challenge has been signed, the signature should be encoded using base64 and sent back
to the server, using the AUTHENTICATE command again. If everything went to plan, the authentication
should succeed.

### Tools

* [ecdsatool](https://github.com/kaniini/ecdsatool) - command line tool for creating and making
use of ECC NISTP256 keypairs.
