package com.mingz.billing.utils;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public abstract class LocalEncryption {
    private static final String AES_NAME = "AES";
    private static final int AES_KEY_BYTES = 32;

    protected abstract void savePassword(@NonNull byte[] password);

    @Nullable
    protected abstract byte[] readPassword();

    protected abstract void saveProtection(@NonNull byte[] protection);

    @Nullable
    protected abstract byte[] readProtection();

    protected abstract void saveBiometrics(@NonNull byte[] biometrics);

    @Nullable
    protected abstract byte[] readBiometrics();

    @NonNull
    protected abstract byte[] verify();

    protected abstract void passwordError();

    protected abstract void biometricsError();

    protected abstract void protectionError();

    //******核心方法******
    public void addPassword(byte[] password) {
        password = formatSize(password);
        byte[] key = generateAESKey();
        byte[] ciphertext = aesEncrypt(key, verify());
        key = xor(key, password);
        password = new byte[AES_KEY_BYTES + ciphertext.length];
        System.arraycopy(key, 0, password, 0, AES_KEY_BYTES);
        System.arraycopy(ciphertext, 0, password, AES_KEY_BYTES, ciphertext.length);
        // (passwdKey ^ userPasswd)、verifyCiphertext
        savePassword(password);
    }

    public void addBiometrics(byte[] inputPasswd, byte[] biometrics) {
        byte[] result = addSubstituteParty(inputPasswd, biometrics);
        if (result != null) {
            saveBiometrics(result);
        }
    }

    public void addProtection(byte[] inputPasswd, byte[] protection) {
        byte[] result = addSubstituteParty(inputPasswd, protection);
        if (result != null) {
            saveProtection(result);
        }
    }

    public byte[] verifyPassword(byte[] inputPasswd) {
        return verifyPassword(inputPasswd, readPassword());
    }

    public byte[] verifyBiometrics(byte[] inputBiometrics) {
        byte[] key = verifySubstituteParty(inputBiometrics, readBiometrics());
        if (key == null) {
            run(this::biometricsError);
        }
        return key;
    }

    public byte[] verifyProtection(byte[] inputProtection) {
        byte[] key = verifySubstituteParty(inputProtection, readProtection());
        if (key == null) {
            run(this::protectionError);
        }
        return key;
    }

    public void updatePasswd(byte[] inputOldPasswd, byte[] newPasswd) {
        byte[] password = readPassword();
        byte[] key = verifyPassword(inputOldPasswd, password);
        if (key == null) {
            return;
        }
        key = xor(key, formatSize(newPasswd));
        System.arraycopy(key, 0, password, 0, AES_KEY_BYTES);
        savePassword(password);
    }

    public void updateBiometrics(byte[] inputPasswd, byte[] newBiometrics) {
        byte[] result = updateSubstituteParty(inputPasswd, newBiometrics, readBiometrics());
        if (result == null) {
            return;
        }
        saveBiometrics(result);
    }

    public void updateProtection(byte[] inputPasswd, byte[] newProtection) {
        byte[] result = updateSubstituteParty(inputPasswd, newProtection, readProtection());
        if (result == null) {
            return;
        }
        saveProtection(result);
    }

    public byte[] updateAESKey(byte[] oldKey) {
        byte[] passwd = readPassword();
        if (passwd == null) {
            return null;
        }
        byte[] oldKeyCiphertext = new byte[AES_KEY_BYTES];
        System.arraycopy(passwd, 0, oldKeyCiphertext, 0, AES_KEY_BYTES);
        byte[] userPasswd = xor(oldKeyCiphertext, oldKey);
        byte[] newKey = generateAESKey();
        byte[] cipherText = aesEncrypt(newKey, verify());
        byte[] newPasswd = new byte[AES_KEY_BYTES + cipherText.length];
        System.arraycopy(xor(newKey, userPasswd), 0, newPasswd, 0, AES_KEY_BYTES);
        System.arraycopy(cipherText, 0, newPasswd, AES_KEY_BYTES, cipherText.length);
        savePassword(newPasswd);
        byte[] biometrics = readBiometrics();
        if (biometrics != null) {
            saveBiometrics(updateSubstitutePartyKey(oldKey, newKey, biometrics));
        }
        byte[] protection = readProtection();
        if (protection != null) {
            saveProtection(updateSubstitutePartyKey(oldKey, newKey, protection));
        }
        return newKey;
    }

    private byte[] addSubstituteParty(byte[] inputPasswd, byte[] replace) {
        byte[] passwdKey = verifyPassword(inputPasswd);
        if (passwdKey != null) {
            byte[] key = generateAESKey();
            byte[] diff = xor(key, passwdKey);
            byte[] ciphertext = aesEncrypt(key, verify());
            key = xor(key, formatSize(replace));
            byte[] result = new byte[AES_KEY_BYTES * 2 + ciphertext.length];
            System.arraycopy(diff, 0, result, 0, AES_KEY_BYTES);
            System.arraycopy(key, 0, result, AES_KEY_BYTES, AES_KEY_BYTES);
            System.arraycopy(ciphertext, 0, result, AES_KEY_BYTES * 2, ciphertext.length);
            // (partyKey ^ passwdKey)、(partyKey ^ userParty)、verifyCiphertext
            return result;
        }
        return null;
    }

    private byte[] verifyPassword(byte[] inputPasswd, byte[] savePasswd) {
        if (savePasswd == null) {
            return null;
        }
        byte[] passwdKey = new byte[AES_KEY_BYTES];
        System.arraycopy(savePasswd, 0, passwdKey, 0, AES_KEY_BYTES);
        passwdKey = xor(passwdKey, formatSize(inputPasswd));
        byte[] passwdCipher = new byte[savePasswd.length - AES_KEY_BYTES];
        System.arraycopy(savePasswd, AES_KEY_BYTES, passwdCipher, 0, passwdCipher.length);
        if (equal(aesDecrypt(passwdKey, passwdCipher), verify())) {
            return passwdKey;
        } else {
            run(this::passwordError);
            return null;
        }
    }

    private byte[] verifySubstituteParty(byte[] inputReplace, byte[] saveReplace) {
        if (saveReplace == null) {
            return null;
        }
        byte[] diff = new byte[AES_KEY_BYTES];
        System.arraycopy(saveReplace, 0, diff, 0, AES_KEY_BYTES);
        byte[] key = new byte[AES_KEY_BYTES];
        System.arraycopy(saveReplace, AES_KEY_BYTES, key, 0, AES_KEY_BYTES);
        key = xor(key, formatSize(inputReplace));
        byte[] ciphertext = new byte[saveReplace.length - AES_KEY_BYTES * 2];
        System.arraycopy(saveReplace, AES_KEY_BYTES * 2, ciphertext, 0, ciphertext.length);
        if (equal(aesDecrypt(key, ciphertext), verify())) {
            return xor(diff, key);
        } else {
            return null;
        }
    }

    private byte[] updateSubstituteParty(byte[] inputPasswd, byte[] newReplace, byte[] saveReplace) {
        byte[] passwdKey = verifyPassword(inputPasswd);
        if (passwdKey == null) {
            return null;
        }
        byte[] diff = new byte[AES_KEY_BYTES];
        System.arraycopy(saveReplace, 0, diff, 0, AES_KEY_BYTES);
        byte[] partyKey = xor(diff, passwdKey);
        partyKey = xor(partyKey, formatSize(newReplace));
        System.arraycopy(partyKey, 0, saveReplace, AES_KEY_BYTES, AES_KEY_BYTES);
        return saveReplace;
    }

    private byte[] updateSubstitutePartyKey(byte[] oldKey, byte[] newKey, byte[] saveReplace) {
        byte[] diff = new byte[AES_KEY_BYTES];
        System.arraycopy(saveReplace, 0, diff, 0, AES_KEY_BYTES);
        byte[] partyKey = xor(diff, oldKey);
        byte[] userParty = new byte[AES_KEY_BYTES];
        System.arraycopy(saveReplace, AES_KEY_BYTES, userParty, 0, AES_KEY_BYTES);
        userParty = xor(userParty, partyKey);
        partyKey = generateAESKey();
        diff = xor(partyKey, newKey);
        byte[] ciphertext = aesEncrypt(partyKey, verify());
        partyKey = xor(partyKey, userParty);
        byte[] result = new byte[AES_KEY_BYTES * 2 + ciphertext.length];
        System.arraycopy(diff, 0, result, 0, AES_KEY_BYTES);
        System.arraycopy(partyKey, 0, result, AES_KEY_BYTES, AES_KEY_BYTES);
        System.arraycopy(ciphertext, 0, result, AES_KEY_BYTES * 2, ciphertext.length);
        return result;
    }

    //******AES******
    private byte[] generateAESKey() {
        Random random = new Random();
        byte[] key = new byte[AES_KEY_BYTES];
        for (int i = 0; i < AES_KEY_BYTES; i++) {
            key[i] = (byte) random.nextInt(256);
        }
        return key;
    }

    private byte[] aesEncrypt(byte[] key, byte[] data) {
        try {
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance(AES_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, AES_NAME));
            return cipher.doFinal(data);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private byte[] aesDecrypt(byte[] key, byte[] ciphertext) {
        try {
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance(AES_NAME);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, AES_NAME));
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    //******工具方法******
    private byte[] formatSize(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            return new byte[0];
        }
    }

    private byte[] xor(byte[] a, byte[] b) {
        byte[] r;
        int start;
        int aStart;
        int bStart;
        if (a.length > b.length) {
            r = new byte[a.length];
            int diff = a.length - b.length;
            System.arraycopy(a, 0, r, 0, diff);
            start = diff;
            aStart = diff;
            bStart = 0;
        } else if (a.length < b.length) {
            r = new byte[b.length];
            int diff = b.length - a.length;
            System.arraycopy(b, 0, r, 0, diff);
            start = diff;
            aStart = 0;
            bStart = diff;
        } else {
            r = new byte[a.length];
            start = 0;
            aStart = 0;
            bStart = 0;
        }
        while (start < r.length) {
            r[start] = (byte) (a[aStart] ^ b[bStart]);
            start++;
            aStart++;
            bStart++;
        }
        return r;
    }

    private boolean equal(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private void run(Runnable run) {
        new Thread(run).start();
    }
}
