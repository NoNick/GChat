package sample.unit.rsa;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import sample.utils.crypto.Cryptography;
import sample.utils.crypto.RSAKeysManager;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Slf4j
public class RSAEncodingDecodingTest {

    private static final int RANK_SOLDIER = 1;

    private RSAPublicKey rsaPublicKey;
    private RSAPrivateKey rsaPrivateKey;

    @Before
    public void setUp() {
        rsaPublicKey = RSAKeysManager.getPublicKeyWithRank(RANK_SOLDIER);
        rsaPrivateKey = RSAKeysManager.getPrivateKeyWithRank(RANK_SOLDIER);
    }

    @Test
    public void RSA_Testing_keysBelongEachOther() {
        String beforeEncryption = "Text before encryption";

        byte[] encryptedText = Cryptography.encryptMessageWithKey(beforeEncryption.getBytes(), rsaPublicKey);
        byte[] decryptedMessage = Cryptography.decryptMessageWithKey(encryptedText, rsaPrivateKey);

        String afterDecryption = new String(decryptedMessage);

        Assertions.assertThat(Cryptography.keysBelongEachOther(rsaPublicKey, rsaPrivateKey)).isTrue();
        Assertions.assertThat(afterDecryption).isEqualToIgnoringCase(beforeEncryption);
    }


}
