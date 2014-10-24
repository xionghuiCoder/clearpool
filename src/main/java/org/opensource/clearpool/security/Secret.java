package org.opensource.clearpool.security;

/**
 * The interface to encrypt or decrypt the text.
 * 
 * @author xionghui
 * @date 24.09.2014
 * @version 1.0
 */
public interface Secret {
	public String encrypt(String plainText) throws Exception;

	public String decrypt(String cipherText) throws Exception;
}
