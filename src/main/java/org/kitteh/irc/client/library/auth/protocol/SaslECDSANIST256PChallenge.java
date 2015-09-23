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
import java.io.UnsupportedEncodingException;
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
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * SASL ECDSA-NIST256P-CHALLENGE authentication. Automatically attempts auth
 * during connection.
 */
public class SaslECDSANIST256PChallenge extends AbstractSaslProtocol<PrivateKey> {
    private class Listener extends AbstractSaslProtocol.Listener {
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
        return this.getUsername() + "\0" + this.getUsername() + "\0";
    }

    @Nonnull
    @Override
    public Object getEventListener() {
        return this.listener == null ? this.listener = new Listener() : this.listener;
    }

    public static String base64Encode(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public static String base64Encode(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static PrivateKey getPrivateKey(String base64Encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64Encoded));
        return keyFactory.generatePrivate(keySpec);
    }

    public static PublicKey getPublicKey(String base64Encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(base64Encoded));
        return keyFactory.generatePublic(keySpec);
    }

    public static String sign(PrivateKey privateKey, String base64Challenge) throws SignatureException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(Base64.getDecoder().decode(base64Challenge));
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    public static KeyPair getNewKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        keyPairGenerator.initialize(256, secureRandom);
        return keyPairGenerator.generateKeyPair();
    }

    // TODO UNIT TEST BELOW
    public static void signAndVerify(PrivateKey privateKey, String challenge, PublicKey publicKey) throws SignatureException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(Base64.getDecoder().decode(challenge));
        byte[] signed = signature.sign();
        Signature ver = Signature.getInstance("SHA256withECDSA");
        ver.initVerify(publicKey);
        ver.update(Base64.getDecoder().decode(challenge));
        System.out.println("Verified: " + ver.verify(signed));
    }

    public static void main(String[] args) throws Throwable {
        KeyPair keyPair = getNewKey();
        PublicKey publicKeyO = keyPair.getPublic();
        PrivateKey privateKeyO = keyPair.getPrivate();
        System.out.println(((ECPrivateKey) privateKeyO).getParams());

        signAndVerify(privateKeyO, "MwRW+/KpEiEWcA+d5d19t9imD4h+DJbLXORcHSSSwl0=", publicKeyO);

        String publicKeyS = base64Encode(publicKeyO);
        String privateKeyS = base64Encode(privateKeyO);
        System.out.println();
        System.out.println(publicKeyS);
        System.out.println();
        System.out.println(privateKeyS);
        System.out.println();

        PublicKey publicKeyN = getPublicKey(publicKeyS);
        PrivateKey privateKeyN = getPrivateKey(privateKeyS);

        signAndVerify(privateKeyN, "MwRW+/KpEiEWcA+d5d19t9imD4h+DJbLXORcHSSSwl0=", publicKeyO);
        signAndVerify(privateKeyN, "MwRW+/KpEiEWcA+d5d19t9imD4h+DJbLXORcHSSSwl0=", publicKeyN);
        signAndVerify(privateKeyO, "MwRW+/KpEiEWcA+d5d19t9imD4h+DJbLXORcHSSSwl0=", publicKeyN);
    }
    // TODO UNIT TEST ABOVE
}
