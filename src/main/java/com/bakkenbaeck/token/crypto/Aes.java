package com.bakkenbaeck.token.crypto;

//import android.content.Context;
//import android.content.SharedPreferences;
//import android.util.Base64;

//import com.bakkenbaeck.token.view.BaseApplication;

//import org.spongycastle.util.encoders.Base64;

import org.spongycastle.util.encoders.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

public class Aes
{

    private static final String IV = "00";

    //private final SharedPreferences preferences;
    private Cipher cipher;
    private SecretKeySpec key;
    private AlgorithmParameterSpec spec;

    public Aes() {
        //this.preferences = BaseApplication.get().getSharedPreferences("ae", Context.MODE_PRIVATE);
    }

    public String encrypt(final String plainText, final String password) {
        if (this.cipher == null) {
            initWithPassword(password);
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            return new String(Base64.encode(encrypted), "UTF-8");
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(final String cryptedText, final String password) {
        if (this.cipher == null) {
            initWithPassword(password);
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            final byte[] bytes = Base64.decode(cryptedText);
            final byte[] decrypted = cipher.doFinal(bytes);
            return new String(decrypted, "UTF-8");
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void initWithPassword(final String password) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(password.getBytes("UTF-8"));
            byte[] keyBytes = new byte[32];
            System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);

            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            key = new SecretKeySpec(keyBytes, "AES");
            spec = getIV();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private AlgorithmParameterSpec getIV() {
        final byte[] iv = readIvFromFileOrGenerateNew();
        return new IvParameterSpec(iv);
    }

    private byte[] readIvFromFileOrGenerateNew() {
        final String encoded = null;// = this.preferences.getString(IV, null);
        if (encoded == null) {
            return generateAndSaveIv();
        }
        return Base64.decode(encoded);
    }

    private byte[] generateAndSaveIv() {
        final SecureRandom random = new SecureRandom();
        final byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        saveIvToFile(bytes);
        return bytes;
    }

    private void saveIvToFile(final byte[] iv) {
        final String toWrite = Base64.toBase64String(iv);
        //this.preferences.edit().putString(IV, toWrite).apply();
    }

}