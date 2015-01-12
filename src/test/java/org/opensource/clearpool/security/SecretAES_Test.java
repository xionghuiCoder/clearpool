package org.opensource.clearpool.security;

import junit.framework.TestCase;

public class SecretAES_Test extends TestCase {

  public void test_AES() throws Exception {
    SecretAES sct = new SecretAES();
    String msg = "s2ad89!-=+%@#4";
    System.out.println(msg);
    String cipher = sct.encrypt(msg);
    System.out.println(cipher);
    System.out.println(sct.decrypt(cipher));
  }

  public void test_Chinese() throws Exception {
    SecretAES sct = new SecretAES();
    String msg = "业a!-=d達12+%@#4";
    System.out.println(msg);
    String cipher = sct.encrypt(msg);
    System.out.println(cipher);
    System.out.println(sct.decrypt(cipher));
  }
}
