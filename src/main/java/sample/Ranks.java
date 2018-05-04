package sample;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class Ranks {
    public static final int SOLDIER_RANK = 1;
    public static final String SOLDIER_KEY = "a";
    public static final int SERGEANT_RANK = 2;
    public static final String SERGEANT_KEY = "b";
    public static final int LIEUTENANT_RANK = 3;
    public static final String LIEUTENANT_KEY = "c";
    public static final int GENERAL_RANK = 4;
    public static final String GENERAL_KEY = "G for Govno";

    public static String getRankName(int rank) {
        switch (rank) {
            case SOLDIER_RANK:
                return "Soldier";
            case SERGEANT_RANK:
                return "Sergeant";
            case LIEUTENANT_RANK:
                return "Lieutenant";
            case GENERAL_RANK:
                return "General";
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Optional<Integer> getRank(String name, String hash)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if (getMD5(name + SOLDIER_KEY).equals(hash)) {
            return Optional.of(SOLDIER_RANK);
        } else if (getMD5(name + SERGEANT_KEY).equals(hash)) {
            return Optional.of(SERGEANT_RANK);
        } else if (getMD5(name + LIEUTENANT_KEY).equals(hash)) {
            return Optional.of(LIEUTENANT_RANK);
        } else if (getMD5(name + GENERAL_KEY).equals(hash)) {
            return Optional.of(GENERAL_RANK);
        } else {
            return Optional.empty();
        }
    }

    private static String getMD5(String x) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] bytesOfMessage = x.getBytes("UTF-8");

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] thedigest = md.digest(bytesOfMessage);
        return new String(Base64.encodeBase64(thedigest));
    }
}
