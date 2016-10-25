package com.github.xionghuicoder.clearpool.datasource;

import java.sql.Connection;

import javax.sql.XAConnection;

public abstract class CommonConnection {

  public abstract Connection getConnection();

  public abstract XAConnection getXAConnection();
}
