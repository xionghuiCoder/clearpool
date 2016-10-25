package com.github.xionghuicoder.clearpool.console;

import java.util.HashMap;
import java.util.Map;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;

/**
 * JMX相关配置
 *
 * <p>
 * 每个<tt>ClearpoolDataSource</tt>对应一个<tt>Console</tt>，而一个 <tt>Console</tt>管理多个数据库连接池。
 * </p>
 *
 * <p>
 * 配置信息：端口{@link #port port}，<tt>port</tt>范围为0~0xFFFF，用户名密码{@link #securityMap securityMap}；<br>
 * <tt>securityMap</tt>的key为用户名，value为密码；<br>
 * <tt>securityMap</tt>长度为0时表示不需要用户名和密码登录，长度大于1时表示支持多个用户名和密码。
 * </p>
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public class Console {
  private int port = 8082;
  private Map<String, String> securityMap = new HashMap<String, String>();

  public int getPort() {
    return this.port;
  }

  public void setPort(int port) {
    if (port < 0 || port > 0xFFFF) {
      throw new ConnectionPoolException("port " + port + " is out of range");
    }
    this.port = port;
  }

  public Map<String, String> getSecurityMap() {
    return this.securityMap;
  }

  public void setSecurityMap(Map<String, String> securityMap) {
    if (securityMap != null) {
      for (Map.Entry<String, String> entry : securityMap.entrySet()) {
        String user = entry.getKey();
        String password = entry.getValue();
        this.validate(user, password);
      }
    }
    this.securityMap = securityMap;
  }

  private void validate(String user, String password) {
    if (user == null) {
      throw new ConnectionPoolException("user shouldn't be null");
    }
    if (password == null) {
      throw new ConnectionPoolException("password shouldn't be null");
    }
  }
}
