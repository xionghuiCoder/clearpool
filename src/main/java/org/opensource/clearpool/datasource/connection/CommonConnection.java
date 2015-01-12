package org.opensource.clearpool.datasource.connection;

import java.sql.Connection;

import javax.sql.XAConnection;

public abstract class CommonConnection {
  public abstract Connection getConnection();

  public abstract XAConnection getXAConnection();
}
