/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
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
package org.kitteh.irc.client.library.auth.protocol;

import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Handler;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.event.client.ClientReceiveCommandEvent;
import org.kitteh.irc.client.library.util.CommandFilter;

import javax.annotation.Nonnull;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
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
public class SaslECDSANIST256PChallenge extends AbstractSaslProtocol<PrivateKey> {
    private class Listener extends AbstractSaslProtocol<PrivateKey>.Listener {
        @CommandFilter("AUTHENTICATE")
        @Handler(filters = @Filter(CommandFilter.Filter.class))
        @Override
        public void authenticate(ClientReceiveCommandEvent event) {
            if (!event.getParameters().isEmpty()) {
                String base64;
                if (event.getParameters().get(0).equals("+")) {
                    base64 = Base64.getEncoder().encodeToString(SaslECDSANIST256PChallenge.this.getAuthLine().getBytes());
                } else {
                    String challenge = event.getParameters().get(0);
                    try {
                        base64 = sign(SaslECDSANIST256PChallenge.this.getAuthValue(), challenge);
                    } catch (Exception e) {
                        throw new RuntimeException(e); // TODO make this better
                    }
                }
                SaslECDSANIST256PChallenge.this.getClient().sendRawLineImmediately("AUTHENTICATE " + base64);
            }
        }
    }

    private Listener listener;

    /**
     * Creates an instance.
     *
     * @param client client
     * @param username username
     * @param privateKey private key
     */
    public SaslECDSANIST256PChallenge(@Nonnull Client client, @Nonnull String username, @Nonnull PrivateKey privateKey) {
        super(client, username, privateKey, "ECDSA-NIST256P-CHALLENGE");
    }

    @Override
    protected String getAuthLine() {
        return this.getUsername() + '\0' + this.getUsername() + '\0';
    }

    @Nonnull
    @Override
    public Object getEventListener() {
        return (this.listener == null) ? (this.listener = new Listener()) : this.listener;
    }

    /**
     * Encodes a given {@link PrivateKey} to base64.
     *
     * @param privateKey key to encode
     * @return encoded key
     * @see #getPrivateKey(String)
     */
    public static String base64Encode(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Encodes a given {@link PublicKey} to base64.
     *
     * @param publicKey key to encode
     * @return encoded key
     * @see #getPublicKey(String)
     */
    public static String base64Encode(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Gets a {@link PrivateKey} from a base64 encoded String.
     *
     * @param base64Encoded encoded string
     * @return the key
     * @throws NoSuchAlgorithmException if the JVM doesn't support EC
     * @throws InvalidKeySpecException if the encoded key is invalid
     * @see #base64Encode(PrivateKey)
     */
    public static PrivateKey getPrivateKey(String base64Encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64Encoded));
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Gets a {@link PublicKey} from a base64 encoded String.
     *
     * @param base64Encoded encoded string
     * @return the key
     * @throws NoSuchAlgorithmException if the JVM doesn't support EC
     * @throws InvalidKeySpecException if the encoded key is invalid
     * @see #base64Encode(PublicKey)
     */
    public static PublicKey getPublicKey(String base64Encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64Encoded));
        return keyFactory.generatePublic(keySpec);
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
     */
    public static String getCompressedBase64PublicKey(ECPublicKey publicKey) {
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
     */
    public static String sign(PrivateKey privateKey, String base64Challenge) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance("NONEwithECDSA");
        signature.initSign(privateKey);
        signature.update(Base64.getDecoder().decode(base64Challenge));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     * Generates a new EC {@link KeyPair} for use with this SASL protocol.
     *
     * @return a shiny new key pair
     * @throws NoSuchAlgorithmException if the JVM doesn't support NONEwithECDSA
     */
    public static KeyPair getNewKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        keyPairGenerator.initialize(256, secureRandom);
        return keyPairGenerator.generateKeyPair();
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
     */
    public static boolean verify(PublicKey publicKey, String base64Challenge, String signature) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        Signature ver = Signature.getInstance("NONEwithECDSA");
        ver.initVerify(publicKey);
        Base64.Decoder decoder = Base64.getDecoder();
        ver.update(decoder.decode(base64Challenge));
        return ver.verify(decoder.decode(signature));
    }
}
