/*
 * * Copyright (C) 2013-2018 Matt Baxter https://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.irc.client.library.feature.auth;

import net.engio.mbassy.listener.Handler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.feature.filter.CommandFilter;
import org.kitteh.irc.client.library.util.Sanity;
import org.kitteh.irc.client.library.util.ToStringer;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * SASL ECDSA-NIST256P-CHALLENGE authentication. Automatically attempts auth
 * during connection.
 */
public class SaslEcdsaNist256PChallenge extends AbstractSaslProtocol<ECPrivateKey> {
    /**
     * Holds a private and public key.
     */
    public static final class ECKeyPair {
        private final ECPrivateKey privateKey;
        private final ECPublicKey publicKey;

        private ECKeyPair(@NonNull ECPrivateKey privateKey, @NonNull ECPublicKey publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        /**
         * Returns a reference to the private key component of this key pair.
         *
         * @return a reference to the private key
         */
        public @NonNull ECPrivateKey getPrivate() {
            return this.privateKey;
        }

        /**
         * Returns a reference to the public key component of this key pair.
         *
         * @return a reference to the public key
         */
        public @NonNull ECPublicKey getPublic() {
            return this.publicKey;
        }

        @Override
        public @NonNull String toString() {
            return new ToStringer(this).add("privateKey", this.privateKey).add("publicKey", this.publicKey).toString();
        }
    }

    private class Listener extends AbstractSaslProtocol<ECPrivateKey>.Listener {
        @CommandFilter("AUTHENTICATE")
        @Handler
        @Override
        public void authenticate(ClientReceiveCommandEvent event) {
            if (!event.getParameters().isEmpty()) {
                String base64;
                if ("+".equals(event.getParameters().get(0))) {
                    base64 = Base64.getEncoder().encodeToString(SaslEcdsaNist256PChallenge.this.getAuthLine().getBytes());
                } else {
                    String challenge = event.getParameters().get(0);
                    try {
                        base64 = sign(SaslEcdsaNist256PChallenge.this.getAuthValue(), challenge);
                    } catch (Exception e) {
                        throw new RuntimeException(e); // TODO make this better
                    }
                }
                SaslEcdsaNist256PChallenge.this.getClient().sendRawLineImmediately("AUTHENTICATE " + base64);
            }
        }
    }

    private Listener listener;

    /**
     * Creates an instance.
     *
     * @param client client
     * @param accountName account name
     * @param privateKey private key
     */
    public SaslEcdsaNist256PChallenge(@NonNull Client client, @NonNull String accountName, @NonNull ECPrivateKey privateKey) {
        super(client, accountName, privateKey, "ECDSA-NIST256P-CHALLENGE");
    }

    @Override
    protected @NonNull String getAuthLine() {
        return this.getAccountName() + '\0' + this.getAccountName() + '\0';
    }

    @Override
    public @NonNull Object getEventListener() {
        return (this.listener == null) ? (this.listener = new Listener()) : this.listener;
    }

    /**
     * Encodes a given {@link ECPrivateKey} to base64.
     *
     * @param privateKey key to encode
     * @return encoded key
     * @throws IllegalArgumentException if privateKey is null
     * @see #getPrivateKey(String)
     */
    public @NonNull static String base64Encode(@NonNull ECPrivateKey privateKey) {
        Sanity.nullCheck(privateKey, "Private key cannot be null");
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Encodes a given {@link ECPublicKey} to base64.
     *
     * @param publicKey key to encode
     * @return encoded key
     * @throws IllegalArgumentException if publicKey is null
     * @see #getPublicKey(String)
     */
    public @NonNull static String base64Encode(@NonNull ECPublicKey publicKey) {
        Sanity.nullCheck(publicKey, "Public key cannot be null");
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Gets a {@link ECPrivateKey} from a base64 encoded String.
     *
     * @param base64Encoded encoded string
     * @return the key
     * @throws NoSuchAlgorithmException if the JVM doesn't support EC
     * @throws InvalidKeySpecException if the encoded key is invalid
     * @throws IllegalArgumentException if base64Encoded is null
     * @see #base64Encode(ECPrivateKey)
     */
    public @NonNull static ECPrivateKey getPrivateKey(@NonNull String base64Encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Sanity.nullCheck(base64Encoded, "Base64 encoded string cannot be null");
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64Encoded));
        return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    /**
     * Gets a {@link ECPublicKey} from a base64 encoded String.
     *
     * @param base64Encoded encoded string
     * @return the key
     * @throws NoSuchAlgorithmException if the JVM doesn't support EC
     * @throws InvalidKeySpecException if the encoded key is invalid
     * @throws IllegalArgumentException if base64Encoded is null
     * @see #base64Encode(ECPublicKey)
     */
    public @NonNull static ECPublicKey getPublicKey(@NonNull String base64Encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Sanity.nullCheck(base64Encoded, "Base64 encoded string cannot be null");
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64Encoded));
        return (ECPublicKey) keyFactory.generatePublic(keySpec);
    }

    /**
     * Applies point compression to a public key and returns the result,
     * encoded with base64.
     *
     * See http://www.secg.org/sec1-v2.pdf section 2.3.3 for more info.
     *
     * @param publicKey the public key to get the compressed X9.62
     * representation for
     * @return base64 encoded compressed public key
     * @throws IllegalArgumentException if publicKey is null
     */
    public @NonNull static String getCompressedBase64PublicKey(@NonNull ECPublicKey publicKey) {
        Sanity.nullCheck(publicKey, "Public key cannot be null");
        ECPoint ecPoint = publicKey.getW();
        byte[] xBytes = ecPoint.getAffineX().toByteArray();
        int overflow = xBytes.length - 32;
        byte[] yBytes = ecPoint.getAffineY().toByteArray();
        byte finalYByte = yBytes[yBytes.length - 1];
        byte header = (byte) ((finalYByte & 0x01) | 0x02);
        byte[] result = new byte[(xBytes.length + 1) - overflow];
        System.arraycopy(xBytes, overflow, result, 1, 32);
        result[0] = header;
        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * Signs a given base64'd challenge via ECDSA.
     *
     * @param privateKey private key for signing
     * @param base64Challenge challenge to sign
     * @return base64 encoded signature
     * @throws SignatureException if signing fails
     * @throws NoSuchAlgorithmException if the JVM doesn't support NONEwithECDSA
     * @throws InvalidKeyException if the key is invalid
     * @throws IllegalArgumentException if either parameter is null
     */
    public @NonNull static String sign(@NonNull ECPrivateKey privateKey, @NonNull String base64Challenge) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        Sanity.nullCheck(privateKey, "Private key cannot be null");
        Sanity.nullCheck(base64Challenge, "Base64 encoded challenge cannot be null");
        Signature signature = Signature.getInstance("NONEwithECDSA");
        signature.initSign(privateKey);
        signature.update(Base64.getDecoder().decode(base64Challenge));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * Generates a new {@link ECKeyPair} for use with this SASL protocol.
     *
     * @return a shiny new key pair
     * @throws NoSuchAlgorithmException if the JVM doesn't support NONEwithECDSA
     */
    public @NonNull static ECKeyPair getNewKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        keyPairGenerator.initialize(256, secureRandom);
        KeyPair pair = keyPairGenerator.generateKeyPair();
        return new ECKeyPair((ECPrivateKey) pair.getPrivate(), (ECPublicKey) pair.getPublic());
    }

    /**
     * Verifies a signature.
     *
     * @param publicKey public key to use in verification
     * @param base64Challenge the challenge that was signed
     * @param signature signature
     * @return true if verified
     * @throws SignatureException if something has gone horribly wrong
     * @throws NoSuchAlgorithmException if the JVM doesn't support NONEwithECDSA
     * @throws InvalidKeyException if the key is invalid
     * @throws IllegalArgumentException if any parameter is null
     */
    public static boolean verify(@NonNull ECPublicKey publicKey, @NonNull String base64Challenge, @NonNull String signature) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        Sanity.nullCheck(publicKey, "Public key cannot be null");
        Sanity.nullCheck(base64Challenge, "Base64 encoded challenge cannot be null");
        Sanity.nullCheck(signature, "Signature cannot be null");
        Signature ver = Signature.getInstance("NONEwithECDSA");
        ver.initVerify(publicKey);
        Base64.Decoder decoder = Base64.getDecoder();
        ver.update(decoder.decode(base64Challenge));
        return ver.verify(decoder.decode(signature));
    }
}
