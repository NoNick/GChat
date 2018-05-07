package sample.utils.crypto;

import lombok.extern.slf4j.Slf4j;
import sample.model.RanksEnum;
import sample.service.exceptions.InvalidReturnValue;
import sample.service.exceptions.errors.ServiceErrorCode;
import sample.utils.Ranks;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RSAKeysManager {
    private static final String SECRET_KEY_SUFFIX = "_SECRET_KEY";
    private static final char[] KEY_STORE_PASSWORD = "5b+ixDGsTePTX8Ff0ONXtg==".toCharArray(); //KeyStorePassword

    private static final Integer[] RANKS = new Integer[]{
            Ranks.SOLDIER_RANK,
            Ranks.SERGEANT_RANK,
            Ranks.LIEUTENANT_RANK,
            Ranks.GENERAL_RANK};

    private static final RanksEnum[] RANKS_ENUMS = new RanksEnum[]{
            RanksEnum.SOLDIER,
            RanksEnum.SERGEANT,
            RanksEnum.LIEUTENANT,
            RanksEnum.GENERAL};

    private static final Map<Integer, RanksEnum> RANK_NAME_BY_RANK_INT = new HashMap<Integer, RanksEnum>() {
        {
            put(RANKS[0], RANKS_ENUMS[0]);
            put(RANKS[1], RANKS_ENUMS[1]);
            put(RANKS[2], RANKS_ENUMS[2]);
            put(RANKS[3], RANKS_ENUMS[3]);
        }
    };

    private static String GLOBAL_KEY_STORAGE_PATH;
    private static String KEYSTORE_PATH;

    RSAKeysManager(String globalKeyStoragePath, String keystorePath) {
        GLOBAL_KEY_STORAGE_PATH = globalKeyStoragePath;
        KEYSTORE_PATH = keystorePath;
    }

    public static RSAPublicKey getPublicKeyWithRank(Integer rank) {
        RanksEnum rankName = RANK_NAME_BY_RANK_INT.get(rank);
        byte[] pkBytes = null;

        try {
            Path path = new File(getPathToPublicKey(rankName)).toPath();

            System.out.println(path.toString());

            pkBytes = Files.readAllBytes(path.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        X509EncodedKeySpec spec = new X509EncodedKeySpec(pkBytes);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(spec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static RSAPrivateKey getPrivateKeyWithRank(Integer rank) {
        KeyStore keyStore = initKeyStore();

        RanksEnum rankName = RANK_NAME_BY_RANK_INT.get(rank);
        KeyStore.PrivateKeyEntry privateKeyEntry = null;

        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(KEY_STORE_PASSWORD);
        try {
            String alias = rankName + SECRET_KEY_SUFFIX;
            assert keyStore == null;
            privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, passwordProtection);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            e.printStackTrace();
        }
        if (privateKeyEntry != null) {
            return (RSAPrivateKey) privateKeyEntry.getPrivateKey();
        } else return null;
    }

    private static KeyStore initKeyStore() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try (InputStream fis = new FileInputStream(new File(getKeystorePath()))) {
            keyStore.load(fis, KEY_STORE_PASSWORD);
            return keyStore;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    void generateKeyStore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        keyStore.load(null, KEY_STORE_PASSWORD);

        String[] aliases = new String[]{
                "SOLDIER" + SECRET_KEY_SUFFIX,
                "SERGEANT" + SECRET_KEY_SUFFIX,
                "LIEUTENANT" + SECRET_KEY_SUFFIX,
                "GENERAL" + SECRET_KEY_SUFFIX};

        log.info("Before: {}", keyStore.size());

        for (int i = 0; i < aliases.length; i++) {
            if (!keyStore.containsAlias(aliases[i])) {
                KeyPair keyPair = initKeyPair();
                PrivateKey privateKey = keyPair.getPrivate();
                PublicKey publicKey = keyPair.getPublic();

                if (!Cryptography.keysBelongEachOther(publicKey, privateKey)) {
                    throw new IllegalArgumentException();
                }

                Path publicKeyPath = Paths.get(getPathToPublicKey(RANKS_ENUMS[i]));
                Files.createDirectories(publicKeyPath.getParent());
                try (FileOutputStream fos = new FileOutputStream(Files.createFile(publicKeyPath).toFile())) {
                    fos.write(publicKey.getEncoded());
                    fos.flush();
                    log.info("Stored public key for {}", RANKS_ENUMS[i]);
                }

                X509Certificate[] x509Certificate = {generateCertificate()};
                keyStore.setKeyEntry(aliases[i], privateKey, KEY_STORE_PASSWORD, x509Certificate);
            }
        }

        log.info("After: {}", keyStore.size());

        Path keyStorePath = Paths.get(KEYSTORE_PATH);
        Files.createDirectories(keyStorePath.getParent());

        try (FileOutputStream fos = new FileOutputStream(Files.createFile(keyStorePath).toFile())) {
            keyStore.store(fos, KEY_STORE_PASSWORD);
            log.info("STORED!");
        }
    }

    private static X509Certificate generateCertificate() {
        try {
            CertAndKeyGen certGen = new CertAndKeyGen("RSA", "SHA256WithRSA");
            certGen.generate(2048);
            long valid = 365 * 24 * 60 * 60;
            return certGen.getSelfCertificate(new X500Name("CN=GChat,O=Eugene Vanchugov,L=St.Petersburg,C=RU"), valid);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getPathToPublicKey(RanksEnum rankName) {
        return GLOBAL_KEY_STORAGE_PATH + rankName + "_publicKey.pk";
    }

    private static String getKeystorePath() {
        return GLOBAL_KEY_STORAGE_PATH + "keystore.ks";
    }

    private static KeyPair initKeyPair() {
        KeyPairGenerator pairGenerator = null;
        try {
            pairGenerator = KeyPairGenerator.getInstance("RSA");
            pairGenerator.initialize(512);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (pairGenerator != null) {
            return pairGenerator.generateKeyPair();
        } else throw new InvalidReturnValue(ServiceErrorCode.NULL_RETURN_VALUE, null);
    }
}
