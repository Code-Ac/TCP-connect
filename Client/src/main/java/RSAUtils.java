import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * A easy-to-use Java Utility all around RSA Encryption and Key handling.
 */
public class RSAUtils {

    /**
     * Generates a Keypair whit an public and an private Key.
     * This may take a while (approx. 10sec)
     *
     * @param keySize the Size the Key Should be. Recommended is a Size over 256 to make it safe.
     * @return finished Keypair
     */
    public static KeyPair generateKeyPair(int keySize) {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        generator.initialize(keySize, new SecureRandom());
        return generator.generateKeyPair();
    }

    /**
     * Encrypts an String whit the given Public Key.
     * Theoretically, an Private Key could be used too, whit the Public Key to decrypt, but this is NOT SAVE!
     *
     * @param text      the message to encrypt
     * @param publicKey the key to take
     * @return the encrypted message
     */
    public static String encrypt(String text, PublicKey publicKey) {
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] bytes = encryptCipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypts an String whit the given Public Key.
     * Theoretically, an Private Key could be used too, whit the Public Key to decrypt, but this is NOT SAVE!
     *
     * @param text       the message to decrypt
     * @param privateKey the key to take
     * @return the decrypted message
     */
    public static String decrypt(String text, PrivateKey privateKey) {
        try {
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] bytes = Base64.getDecoder().decode(text);
            return new String(decryptCipher.doFinal(bytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Returns the given Key (Public or Private) converted to a String (in example needed for Transmission)
     * In case of Problems, cast your Key to 'java.security.Key' .
     *
     * @param key key to encode
     * @return the key as a String
     */
    public static String getString(Key key) {
        byte[] keyBytes = key.getEncoded();
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    /**
     * Converts an String back to a Public Key (->getString() ).
     *
     * @param keyString String to decode
     * @return an Public Key
     */
    public static Key getPublicKey(String keyString) {
        byte[] publicKeyBytes = Base64.getDecoder().decode(keyString);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts an String back to a Public Key (->getString() ).
     *
     * @param keyString String to decode
     * @return an Private Key
     */
    public static Key getPrivateKey(String keyString) {
        byte[] privateKeyBytes = Base64.getDecoder().decode(keyString);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec privateKeySpec = new X509EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
