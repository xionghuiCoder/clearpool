package org.opensource.clearpool.security;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.opensource.clearpool.util.Base64;

/**
 * Encrypt and Decrypt the text by AES.
 * 
 * @author xionghui
 * @date 24.09.2014
 * @version 1.0
 */
public class SecretAES implements Secret {
  private static final String KEY = "SecretAES";

  private KeyGenerator keygen;

  private SecretKey deskey;

  // get cipher by AES
  private Cipher cipher = Cipher.getInstance("AES");

  public SecretAES() throws Exception {
    // Security.addProvider(new SunJCE());
    this.keygen = KeyGenerator.getInstance("AES");
    SecureRandom securerandom = new SecureRandom(KEY.getBytes());
    this.keygen.init(securerandom);
    // generate key
    this.deskey = this.keygen.generateKey();
  }

  /**
   * encrypt the text.
   */
  @Override
  public String encrypt(String plainText) throws Exception {
    this.cipher.init(Cipher.ENCRYPT_MODE, this.deskey);
    byte[] src = plainText.getBytes("UTF-8");
    byte[] cipherByte = this.cipher.doFinal(src);
    String result = Base64.byteArrayToBase64(cipherByte);
    return result;
  }

  /**
   * decrypt the text.
   */
  @Override
  public String decrypt(String cipherText) throws Exception {
    byte[] cipherBytes = Base64.base64ToByteArray(cipherText);
    this.cipher.init(Cipher.DECRYPT_MODE, this.deskey);
    byte[] cipherByte = this.cipher.doFinal(cipherBytes);
    return new String(cipherByte);
  }
}
