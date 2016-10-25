package com.github.xionghuicoder.clearpool.testcase.security;

import org.junit.Test;

import com.github.xionghuicoder.clearpool.security.ISecret;
import com.github.xionghuicoder.clearpool.security.SecretImpl;

import junit.framework.TestCase;

public class SecretImplTest extends TestCase {

  @Test
  public void testEnglish() throws Exception {
    ISecret sct = new SecretImpl();
    String msg = "s2ad89!-=+%@#4";
    System.out.println(msg);
    String cipher = sct.encrypt(msg);
    System.out.println(cipher);
    String newMsg = sct.decrypt(cipher);
    System.out.println(newMsg);
    assert msg.equals(newMsg);
  }

  @Test
  public void testChinese() throws Exception {
    ISecret sct = new SecretImpl();
    String msg = "业a!-=d達12+%@#4";
    System.out.println(msg);
    String cipher = sct.encrypt(msg);
    System.out.println(cipher);
    String newMsg = sct.decrypt(cipher);
    System.out.println(newMsg);
    assert msg.equals(newMsg);
  }
}
