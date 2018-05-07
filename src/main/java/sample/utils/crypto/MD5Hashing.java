package sample.utils.crypto;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;

public class MD5Hashing {

    public static String getMD5(String x) {
        byte[] bytesOfMessage = new byte[0];
        MessageDigest md = null;

        try {
            bytesOfMessage = x.getBytes("UTF-8");
            md = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert md != null;
        byte[] digest = md.digest(bytesOfMessage);
        return new String(Base64.encodeBase64(digest));
    }

    public static void main(String[] args) {
        System.out.println(getMD5(null));

    }
}
