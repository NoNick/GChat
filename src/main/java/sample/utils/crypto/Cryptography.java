package sample.utils.crypto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import sample.model.Message;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Slf4j
public class Cryptography {

    private static Cipher cipher = initCipher();


    public static byte[] decryptMessageWithRank(Message message, Integer rank) {
        PrivateKey keyByRank = RSAKeysManager.getPrivateKeyWithRank(rank);
        return Cryptography.decryptMessageWithKey(message.getTextBytes(), keyByRank);
    }

    public static byte[] encryptMessageWithRank(Message message, Integer rank) {
        PublicKey publicKeyWithRank = RSAKeysManager.getPublicKeyWithRank(rank);
        return Cryptography.encryptMessageWithKey(message.getTextBytes(), publicKeyWithRank);
    }

    public static boolean keysBelongEachOther(PublicKey publicKey, PrivateKey privateKey) {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
        return rsaPublicKey.getModulus().equals(rsaPrivateKey.getModulus())
                && BigInteger.valueOf(2).modPow(rsaPublicKey.getPublicExponent()
                        .multiply(rsaPrivateKey.getPrivateExponent()).subtract(BigInteger.ONE),
                rsaPublicKey.getModulus()).equals(BigInteger.ONE);
    }

    public static byte[] encryptMessageWithKey(byte[] messageTextToEncrypt, PublicKey publicKey) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            String invalidKey = "INVALID KEY";
            log.error(invalidKey);
            e.printStackTrace();
        }
        return encryptText(cipher, messageTextToEncrypt);
    }

    public static byte[] decryptMessageWithKey(byte[] messageTextToDecrypt, PrivateKey privateKey) {
        byte[] encrypted = Base64.decodeBase64(messageTextToDecrypt);
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            String invalidKey = "INVALID KEY";
            log.error(invalidKey);
            e.printStackTrace();
        }
        return decryptText(cipher, encrypted);
    }

    private static byte[] encryptText(Cipher cipher, byte[] messageBytes) {
        byte[] encrypted = null;
        try {
            encrypted = cipher.doFinal(messageBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            String errorWhileEncrypting = "ERROR WHILE ENCRYPTING";
            log.error(errorWhileEncrypting);
            e.printStackTrace();
        }
        return Base64.encodeBase64(encrypted);
    }

    private static byte[] decryptText(Cipher cipher, byte[] encrypted) {
        byte[] decrypted;
        try {
            decrypted = cipher.doFinal(encrypted);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            String errorWhileDecrypting = "ERROR WHILE DECRYPTING";
            log.error(errorWhileDecrypting);
            e.printStackTrace();
            decrypted = errorWhileDecrypting.getBytes();
        }
        return decrypted;
    }

    private static Cipher initCipher() {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA");
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            String cipherInitError = "ERROR WHILE INIT CIPHER";
            log.error(cipherInitError);
            e.printStackTrace();
            return null;
        }
    }

}
