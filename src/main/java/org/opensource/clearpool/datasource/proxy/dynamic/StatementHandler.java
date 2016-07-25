package org.opensource.clearpool.datasource.proxy.dynamic;

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

import org.opensource.clearpool.configuration.ConfigurationVO;
import org.opensource.clearpool.datasource.proxy.ConnectionProxy;
import org.opensource.clearpool.datasource.proxy.PoolConnectionImpl;
import org.opensource.clearpool.logging.PoolLogger;
import org.opensource.clearpool.logging.PoolLoggerFactory;

/**
 * This class is the dynamic proxy of the {@link Statement},it used to trace and record sql.
 *
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
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

  // need to show sql?
  private boolean showSql;
  // filter sql
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
      /**
       * setAccessible could improve the performance of the reflection.you want the reason? ha,check
       * the source of jdk please.<br />
       * With simple test,it shows that setAccessible(true) is about 3 times faster than original.
       */
      method.setAccessible(true);
      long startTime = this.showSql ? System.currentTimeMillis() : 0;
      try {
        result = method.invoke(this.statement, args);
        // deal sqlCount if there is no exception
        this.dealSqlCount(methodName, args);
      } catch (InvocationTargetException e) {
        LOGGER.error("StatementHandler.invoke error: ", e);
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

  /**
   * This method invoked before execute update.
   */
  protected void beforeInvoke(String methodName) throws XAException, SystemException {

  }

  /**
   * deal sqlCount by {@link ConnectionProxy}
   */
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

  /**
   * Close the statement and remove it from {@link #conProxy}
   */
  private void close() throws SQLException {
    try {
      this.statement.close();
    } catch (SQLException e) {
      LOGGER.error("close statement error: ", e);
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

  /**
   * Handle the SQLException
   */
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

  /**
   * Set sql parameters and log the sql.
   */
  private void dealLogSql(String methodName, boolean isError, long sqlTime, Object[] args) {
    if (CLEARPARAMETERS_METHOD.equals(methodName)) {
      // clear parameters
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
      // If we have just added a batch call then we need to
      // update the sql log
      int argCount = args != null ? args.length : 0;
      if (argCount > 0 && args[0] instanceof String) {
        this.setSqlStatementIfNull((String) args[0]);
      }
      this.appendToSqlLog();
    } else if (EXECUTE_BATCH_METHOD.equals(methodName)) {
      // executing a batch should do a trace
      this.trace(isError, sqlTime);
    } else if (methodName.startsWith(EXECUTE)) {
      // executing should update the log and do a trace
      int argCount = args != null ? args.length : 0;
      if (argCount > 0 && args[0] instanceof String) {
        this.setSqlStatementIfNull((String) args[0]);
      }
      this.appendToSqlLog();
      this.trace(isError, sqlTime);
    }
  }

  /**
   * Add a parameter so that we can show its value when tracing
   *
   * @param index within the procedure
   * @param value an object describing its value
   */
  private void saveParameter(int index, Object value) {
    // Lazily instantiate parameterMap if necessary
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
      // in case SQL injection
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

  /**
   * Set sql if it isn't already set
   */
  private void setSqlStatementIfNull(String sql) {
    if (this.sql == null) {
      this.sql = sql;
    }
  }

  /**
   * Append sql to {@link #sqlLog} and set parameters if necessary.
   */
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
      this.sqlLog.append(";\n");
    }
    // Clear parameterMap for next time
    if (this.parameterMap != null) {
      this.parameterMap.clear();
    }
  }

  /**
   * Trace the sql and the time it cost.
   *
   * @param sqlTime so we can log how long the sql cost
   */
  private void trace(boolean isError, long sqlTime) {
    int len = this.sqlLog.length();
    if (len > 0) {
      // delete the last "\n"
      this.sqlLog.deleteCharAt(len - 1);
    }
    String logMsg = "SHOWSQL(" + sqlTime + "ms):\n" + this.sqlLog.toString();
    if (isError) {
      // log sql and the time it cost
      LOGGER.error(logMsg);
    } else {
      // log sql and the time it cost
      LOGGER.info(logMsg);
    }
    // Clear parameterMap for next time
    if (this.parameterMap != null) {
      this.parameterMap.clear();
    }
    this.sql = null;
    this.sqlLog.setLength(0);
  }
}
