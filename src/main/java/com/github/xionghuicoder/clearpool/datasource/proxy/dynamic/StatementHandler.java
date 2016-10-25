package com.github.xionghuicoder.clearpool.datasource.proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.sql.StatementEvent;
import javax.sql.StatementEventListener;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;

import com.github.xionghuicoder.clearpool.core.ConfigurationVO;
import com.github.xionghuicoder.clearpool.datasource.proxy.ConnectionProxy;
import com.github.xionghuicoder.clearpool.datasource.proxy.PoolConnectionImpl;
import com.github.xionghuicoder.clearpool.logging.PoolLogger;
import com.github.xionghuicoder.clearpool.logging.PoolLoggerFactory;

/**
 * {@link Statement Statement}的动态代理，该类还会记录sql日志
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
class StatementHandler implements InvocationHandler {
  private static final PoolLogger LOGGER = PoolLoggerFactory.getLogger(StatementHandler.class);

  private static final String TOSTRING_METHOD = "toString";
  private static final String EQUALS_METHOD = "equals";
  private static final String HASHCODE_METHOD = "hashCode";
  private static final String CLOSE_METHOD = "close";

  private static final String CLEARPARAMETERS_METHOD = "clearParameters";
  private static final String SETNULL_METHOD = "setNull";
  private static final String SET_PREFIX = "set";
  private static final String ADD_BATCH_METHOD = "addBatch";
  protected static final String EXECUTE_BATCH_METHOD = "executeBatch";
  protected static final String EXECUTE = "execute";

  private Statement statement;
  private PoolConnectionImpl pooledConnection;
  private ConnectionProxy conProxy;
  private String sql;
  private Set<String> sqlSet = new HashSet<String>();

  // 是否打印sql
  private boolean showSql;
  // 大于或等于该时间(ms)的sql才打印
  private long sqlTimeFilter;

  private StringBuilder sqlLog = new StringBuilder();

  private Map<Integer, Object> parameterMap;

  StatementHandler(Statement statement, PoolConnectionImpl pooledConnection,
      ConnectionProxy conProxy, String sql) {
    this.statement = statement;
    this.pooledConnection = pooledConnection;
    this.conProxy = conProxy;
    ConfigurationVO cfgVO = conProxy.getCfgVO();
    this.showSql = cfgVO.isShowSql();
    this.sqlTimeFilter = cfgVO.getSqlTimeFilter();
    this.sql = sql;
    this.sqlSet.add(sql);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object result = null;
    Throwable target = null;
    String methodName = method.getName();
    if (TOSTRING_METHOD.equals(methodName)) {
      result = this.toString();
    } else if (EQUALS_METHOD.equals(methodName)) {
      result = this.equals(args[0]);
    } else if (HASHCODE_METHOD.equals(methodName)) {
      result = this.hashCode();
    } else if (CLOSE_METHOD.equals(methodName)) {
      this.close();
    } else {
      this.beforeInvoke(methodName);
      method.setAccessible(true);
      long startTime = this.showSql ? System.currentTimeMillis() : 0;
      try {
        result = method.invoke(this.statement, args);
        this.dealSqlCount(methodName, args);
      } catch (InvocationTargetException e) {
        target = e.getTargetException();
        if (target instanceof SQLException) {
          this.handleException((SQLException) target);
        } else {
          throw target;
        }
      } finally {
        if (this.showSql) {
          long sqlTime = System.currentTimeMillis() - startTime;
          if (target != null || sqlTime >= this.sqlTimeFilter) {
            this.dealLogSql(methodName, target != null, sqlTime, args);
          }
        }
      }
    }
    return result;
  }

  protected void beforeInvoke(String methodName) throws XAException, SystemException {

  }

  private void dealSqlCount(String methodName, Object[] args) {
    if (ADD_BATCH_METHOD.equals(methodName)) {
      int argCount = args != null ? args.length : 0;
      if (argCount > 0 && args[0] instanceof String && this.sql == null) {
        this.sqlSet.add((String) args[0]);
      }
    } else if (methodName.startsWith(EXECUTE)) {
      for (String sql : this.sqlSet) {
        this.conProxy.dealSqlCount(sql);
      }
      this.sqlSet.clear();
    }
  }

  private void close() throws SQLException {
    try {
      this.statement.close();
    } catch (SQLException e) {
      this.handleException(e);
    }
    this.pooledConnection.removeStatement(this.statement);
    if (this.statement instanceof PreparedStatement) {
      List<StatementEventListener> statementEventListeners =
          this.pooledConnection.getStatementEventListeners();
      if (statementEventListeners != null) {
        StatementEvent event =
            new StatementEvent(this.pooledConnection, (PreparedStatement) this.statement);
        for (StatementEventListener listener : statementEventListeners) {
          listener.statementClosed(event);
        }
      }
    }
  }

  private SQLException handleException(SQLException e) throws SQLException {
    if (this.statement instanceof PreparedStatement) {
      List<StatementEventListener> statementEventListeners =
          this.pooledConnection.getStatementEventListeners();
      if (statementEventListeners != null) {
        StatementEvent event =
            new StatementEvent(this.pooledConnection, (PreparedStatement) this.statement);
        for (StatementEventListener listener : statementEventListeners) {
          listener.statementErrorOccurred(event);
        }
      }
    }
    throw e;
  }

  private void dealLogSql(String methodName, boolean isError, long sqlTime, Object[] args) {
    if (CLEARPARAMETERS_METHOD.equals(methodName)) {
      if (this.parameterMap != null) {
        this.parameterMap.clear();
      }
    } else if (SETNULL_METHOD.equals(methodName)) {
      int argCount = args != null ? args.length : 0;
      if (argCount >= 1) {
        int index = ((Integer) args[0]).intValue();
        this.saveParameter(index, null);
      }
    } else if (methodName.startsWith(SET_PREFIX)) {
      int argCount = args != null ? args.length : 0;
      if (argCount >= 2) {
        int index = ((Integer) args[0]).intValue();
        this.saveParameter(index, args[1]);
      }
    } else if (ADD_BATCH_METHOD.equals(methodName)) {
      int argCount = args != null ? args.length : 0;
      if (argCount > 0 && args[0] instanceof String) {
        this.setSqlStatementIfNull((String) args[0]);
      }
      this.appendToSqlLog();
    } else if (EXECUTE_BATCH_METHOD.equals(methodName)) {
      this.trace(isError, sqlTime);
    } else if (methodName.startsWith(EXECUTE)) {
      int argCount = args != null ? args.length : 0;
      if (argCount > 0 && args[0] instanceof String) {
        this.setSqlStatementIfNull((String) args[0]);
      }
      this.appendToSqlLog();
      this.trace(isError, sqlTime);
    }
  }

  private void saveParameter(int index, Object value) {
    if (this.parameterMap == null) {
      this.parameterMap = new TreeMap<Integer, Object>(new Comparator<Integer>() {

        @Override
        public int compare(Integer i1, Integer i2) {
          return this.compareOrigin(i1, i2);
        }

        private int compareOrigin(int x, int y) {
          return x < y ? -1 : x == y ? 0 : 1;
        }
      });
    }
    if (value == null) {
      this.parameterMap.put(index, "NULL");
    } else if (value instanceof String) {
      value = ((String) value).replaceAll("'", "''");
      this.parameterMap.put(index, "'" + value + "'");
    } else if (value instanceof Boolean) {
      this.parameterMap.put(index, value);
    } else if (value instanceof Number) {
      this.parameterMap.put(index, value);
    } else {
      String className = value.getClass().getName();
      int position = className.lastIndexOf(".") + 1;
      String name = className.substring(position);
      this.parameterMap.put(index, name + ":" + value.toString());
    }
  }

  private void setSqlStatementIfNull(String sql) {
    if (this.sql == null) {
      this.sql = sql;
    }
  }

  private void appendToSqlLog() {
    if (this.sql != null && this.sql.length() > 0) {
      String[] sqlFrag = this.sql.split("\\?");
      int index = 0;
      for (String s : sqlFrag) {
        if (index > 0) {
          if (this.parameterMap != null) {
            Object value = this.parameterMap.get(index);
            if (value != null) {
              this.sqlLog.append(value);
            } else {
              this.sqlLog.append("?");
            }
          } else {
            this.sqlLog.append("?");
          }
        }
        this.sqlLog.append(s);
        index++;
      }
      if (this.sql.endsWith("?")) {
        if (this.parameterMap != null) {
          Object value = this.parameterMap.get(index);
          if (value != null) {
            this.sqlLog.append(value);
          } else {
            this.sqlLog.append("?");
          }
        } else {
          this.sqlLog.append("?");
        }
      }
      this.sqlLog.append("\n");
    }
    if (this.parameterMap != null) {
      this.parameterMap.clear();
    }
  }

  private void trace(boolean isError, long sqlTime) {
    int len = this.sqlLog.length();
    if (len > 0) {
      this.sqlLog.deleteCharAt(len - 1);
    }
    String logMsg = "SHOWSQL(" + sqlTime + "ms):\n" + this.sqlLog.toString();
    if (isError) {
      LOGGER.error(logMsg);
    } else {
      LOGGER.info(logMsg);
    }
    if (this.parameterMap != null) {
      this.parameterMap.clear();
    }
    this.sql = null;
    this.sqlLog.setLength(0);
  }
}
