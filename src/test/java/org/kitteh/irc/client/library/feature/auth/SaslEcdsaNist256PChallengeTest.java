package org.kitteh.irc.client.library.feature.auth;

import org.junit.Assert;
import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Tests the ECDSA-NIST256P-CHALLENGE support.
 */
public class SaslEcdsaNist256PChallengeTest {
    private static final String CHALLENGE = "MwRW+/KpEiEWcA+d5d19t9imD4h+DJbLXORcHSSSwl0=";

    /**
     * Signs and verifies signature from created keys.
     *
     * @throws InvalidKeyException if sadness
     * @throws NoSuchAlgorithmException if sadness
     * @throws SignatureException if sadness
     */
    @Test
    public void signAndVerify() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        SaslEcdsaNist256PChallenge.ECKeyPair keyPair = SaslEcdsaNist256PChallenge.getNewKey();
        ECPublicKey publicKeyO = keyPair.getPublic();
        ECPrivateKey privateKeyO = keyPair.getPrivate();
        String signature = SaslEcdsaNist256PChallenge.sign(privateKeyO, CHALLENGE);
        Assert.assertTrue("Failed to verify signed challenge", SaslEcdsaNist256PChallenge.verify(publicKeyO, CHALLENGE, signature));
    }

    /**
     * Encodes the created private key and verifies a signature against the
     * original public key.
     *
     * @throws InvalidKeyException if sadness
     * @throws InvalidKeySpecException if sadness
     * @throws NoSuchAlgorithmException if sadness
     * @throws SignatureException if sadness
     */
    @Test
    public void encodePrivateKey() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException {
        SaslEcdsaNist256PChallenge.ECKeyPair keyPair = SaslEcdsaNist256PChallenge.getNewKey();
        String encodedPrivateKey = SaslEcdsaNist256PChallenge.base64Encode(keyPair.getPrivate());
        ECPrivateKey recreatedPrivateKey = SaslEcdsaNist256PChallenge.getPrivateKey(encodedPrivateKey);
        String signature = SaslEcdsaNist256PChallenge.sign(recreatedPrivateKey, CHALLENGE);
        Assert.assertTrue("Failed to verify signed challenge", SaslEcdsaNist256PChallenge.verify(keyPair.getPublic(), CHALLENGE, signature));
    }

    /**
     * Encodes the created public key and verifies a signature from the
     * original private key against it.
     *
     * @throws InvalidKeyException if sadness
     * @throws InvalidKeySpecException if sadness
     * @throws NoSuchAlgorithmException if sadness
     * @throws SignatureException if sadness
     */
    @Test
    public void encodePublicKey() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, SignatureException {
        SaslEcdsaNist256PChallenge.ECKeyPair keyPair = SaslEcdsaNist256PChallenge.getNewKey();
        String signature = SaslEcdsaNist256PChallenge.sign(keyPair.getPrivate(), CHALLENGE);
        String encodedPublicKey = SaslEcdsaNist256PChallenge.base64Encode(keyPair.getPublic());
        ECPublicKey recreatedPublicKey = SaslEcdsaNist256PChallenge.getPublicKey(encodedPublicKey);
        Assert.assertTrue("Failed to verify signed challenge", SaslEcdsaNist256PChallenge.verify(recreatedPublicKey, CHALLENGE, signature));
    }
}
