package com.github.xionghuicoder.clearpool.util;

import java.sql.Connection;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;

import com.github.xionghuicoder.clearpool.ConnectionPoolException;

import oracle.jdbc.xa.client.OracleXAConnection;

public class OracleUtils {

  private OracleUtils() {}

  public static XAConnection oracleXAConnection(Connection oracleConnection) {
    try {
      return new OracleXAConnection(oracleConnection);
    } catch (XAException e) {
      throw new ConnectionPoolException(e);
    }
  }
}
