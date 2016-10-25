package com.github.xionghuicoder.clearpool.util;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.CommonDataSource;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;
import com.github.xionghuicoder.clearpool.datasource.JDBCDataSource;

public class DataSourceUtils {

  public static CommonDataSource getJDBCDataSource(String clazz, String url, String user,
      String password) {
    if (url == null) {
      throw new ConnectionPoolException("url is null");
    }
    Driver driver;
    try {
      if (clazz == null) {
        clazz = JdbcUtils.getDriverClassName(url);
      }
      driver = JdbcUtils.createDriver(clazz);
    } catch (SQLException e) {
      throw new ConnectionPoolException(e);
    }
    Properties connectProperties = new Properties();
    if (user != null) {
      connectProperties.put("user", user);
    }
    if (password != null) {
      connectProperties.put("password", password);
    }
    return new JDBCDataSource(clazz, url, driver, connectProperties);
  }

  public static CommonDataSource getJndiDataSource(String jndiName) {
    if (jndiName == null) {
      throw new ConnectionPoolException("jndiName is illegal");
    }
    CommonDataSource ds;
    try {
      Context initial = new InitialContext();
      ds = (CommonDataSource) initial.lookup(jndiName);
    } catch (NamingException e) {
      throw new ConnectionPoolException(e);
    }
    return ds;
  }
}
