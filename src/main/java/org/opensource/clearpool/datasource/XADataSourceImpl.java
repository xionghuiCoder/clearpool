package org.opensource.clearpool.datasource;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.opensource.clearpool.datasource.connection.XAConnectionWrapper;
import org.opensource.clearpool.datasource.connection.CommonConnection;

public class XADataSourceImpl extends AbstractDataSource {
  private XADataSource ds;

  public XADataSourceImpl(XADataSource ds) {
    this.ds = ds;
  }

  @Override
  public CommonConnection getCommonConnection() throws SQLException {
    XAConnection xaCon = this.ds.getXAConnection();
    CommonConnection cmnCon = new XAConnectionWrapper(xaCon);
    return cmnCon;
  }
}
