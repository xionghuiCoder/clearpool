package org.opensource.clearpool.util;

import java.sql.Connection;

import javax.sql.XAConnection;
import javax.transaction.xa.XAException;

import oracle.jdbc.xa.client.OracleXAConnection;

import org.opensource.clearpool.exception.TransactionException;

public class OracleUtil {
  private OracleUtil() {}

  public static XAConnection oracleXAConnection(Connection oracleConnection) {
    try {
      return new OracleXAConnection(oracleConnection);
    } catch (XAException e) {
      throw new TransactionException(e);
    }
  }
}
