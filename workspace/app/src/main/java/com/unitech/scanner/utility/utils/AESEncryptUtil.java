package com.unitech.scanner.utility.utils;


import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class AESEncryptUtil {

    private final static String IvAES = "unitechusuusuusu";
    private static final String keyAES ="nnewnowfgzjkvbsrkfmgtbpeawbsqbtf";

    public static String decrypt(String text) {
        try {
            byte[] TextByte = decryptAES(
                    IvAES.getBytes(StandardCharsets.UTF_8)
                    , keyAES.getBytes(StandardCharsets.UTF_8)
                    , Base64.decode(text.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT)
            );
            return TextByte != null ? new String(TextByte, StandardCharsets.UTF_8) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String encrypt(String text) {
        try {
            byte[] TextByte = encryptAES(
                    IvAES.getBytes(StandardCharsets.UTF_8)
                    , keyAES.getBytes(StandardCharsets.UTF_8)
                    , text.getBytes(StandardCharsets.UTF_8)
            );
            return TextByte != null ? Base64.encodeToString(TextByte, Base64.DEFAULT) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String password2Key(String password) {
        if (password == null) {
            password = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(password);
        String s;
        while (sb.length() < 32) {
            sb.append(" ");//--密码长度不够32补足到32
        }
        s = sb.substring(0, 32);//--截取32位密码
        return s;
    }


    private static byte[] encryptAES(byte[] iv, byte[] key, byte[] text) {
        try {
            AlgorithmParameterSpec mAlgorithmParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec mSecretKeySpec = new SecretKeySpec(key, "AES");
            Cipher mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            mCipher.init(Cipher.ENCRYPT_MODE,
                    mSecretKeySpec,
                    mAlgorithmParameterSpec);

            return mCipher.doFinal(text);
        } catch (Exception ex) {
            return null;
        }
    }

    //AES解密，帶入byte[]型態的16位英數組合文字、32位英數組合Key、需解密文字
    private static byte[] decryptAES(byte[] iv, byte[] key, byte[] text) {
        try {
            AlgorithmParameterSpec mAlgorithmParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec mSecretKeySpec = new SecretKeySpec(key, "AES");
            Cipher mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            mCipher.init(Cipher.DECRYPT_MODE,
                    mSecretKeySpec,
                    mAlgorithmParameterSpec);

            return mCipher.doFinal(text);
        } catch (Exception ex) {
            return null;
        }
    }
}
