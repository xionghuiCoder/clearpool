package com.github.xionghuicoder.clearpool.security;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.util.Base64Utils;

/**
 * 使用AES加密解密
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class SecretImpl implements ISecret {
  private static final String KEY = "1b9aa0a0-8bd4-4d43-9d53-4222ad3dfbea";

  private final KeyGenerator keygen;

  private final SecretKey deskey;

  private final Cipher cipher;

  public SecretImpl() throws Exception {
    this.cipher = Cipher.getInstance("AES");
    this.keygen = KeyGenerator.getInstance("AES");
    SecureRandom securerandom = SecureRandom.getInstance("SHA1PRNG");
    securerandom.setSeed(KEY.getBytes());
    this.keygen.init(securerandom);
    this.deskey = this.keygen.generateKey();
  }

  @Override
  public String encrypt(String plainText) {
    if (plainText == null) {
      return null;
    }
    String result = null;
    try {
      this.cipher.init(Cipher.ENCRYPT_MODE, this.deskey);
      byte[] src = plainText.getBytes("UTF-8");
      byte[] cipherByte = this.cipher.doFinal(src);
      result = Base64Utils.byteArrayToBase64(cipherByte);
    } catch (Exception e) {
      throw new ConnectionPoolException(e.getMessage(), e);
    }
    return result;
  }

  @Override
  public String decrypt(String cipherText) {
    if (cipherText == null) {
      return null;
    }
    byte[] cipherByte = null;
    try {
      byte[] cipherBytes = Base64Utils.base64ToByteArray(cipherText);
      this.cipher.init(Cipher.DECRYPT_MODE, this.deskey);
      cipherByte = this.cipher.doFinal(cipherBytes);
    } catch (Exception e) {
      throw new ConnectionPoolException(e.getMessage(), e);
    }
    return new String(cipherByte);
  }
}
