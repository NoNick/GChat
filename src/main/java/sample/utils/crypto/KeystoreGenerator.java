package sample.utils.crypto;

public class KeystoreGenerator {

    //CHANGE TO YOUR PROJECT DIRECTORY!!!
    private static final String GLOBAL_KEY_STORAGE_PATH = "C:\\Users\\evgenii_vanchugov\\IdeaProjects\\GChat\\" + RSAKeysManager.KEYSTORE_FOLDER_NAME;
    private static final String KEYSTORE_PATH = RSAKeysManager.KEYSTORE_FOLDER_NAME + "keystore.ks";

    public static void main(String[] args) throws Exception {
        RSAKeysManager keysManager = new RSAKeysManager(GLOBAL_KEY_STORAGE_PATH, KEYSTORE_PATH);
        keysManager.generateKeyStore();
    }
}
