package com.github.xionghuicoder.clearpool.security;

/**
 * 加密解密接口
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ISecret {

  String encrypt(String plainText);

  String decrypt(String cipherText);
}
