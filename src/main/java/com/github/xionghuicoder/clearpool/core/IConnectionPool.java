package com.github.xionghuicoder.clearpool.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.PooledConnection;

/**
 * 数据库连接池接口
 *
 * @author xionghui
 * @version 1.0.0
 * @since 1.0.0
 */
public interface IConnectionPool {

  /**
   * 默认初始化
   */
  void init();

  /**
   * 初始化单个数据库连接池
   *
   * @param vo 数据库连接池配置
   */
  void initVO(ConfigurationVO vo);

  /**
   * 初始化多个数据库连接池
   *
   * @param voList 数据库连接池配置List
   */
  void initVOList(List<ConfigurationVO> voList);

  /**
   * 获取数据库连接
   *
   * @return 数据库连接
   * @throws SQLException SQL异常
   */
  Connection getConnection() throws SQLException;

  /**
   * 从名称为<tt>name</tt>的数据库连接池内获取连接
   *
   * @param name 数据库连接池名称
   * @return 数据库连接
   * @throws SQLException SQL异常
   */
  Connection getConnection(String name) throws SQLException;

  /**
   * 获取数据库连接
   *
   * @param maxWait 最大等待时间，<tt>maxWait</tt>小于0时无效，超过<tt>maxWait</tt>(ms)后不再等待
   * @return 数据库连接，如果等待超过<tt>maxWait</tt>(ms)后直接返回<tt>null</tt>
   * @throws SQLException SQL异常
   */
  Connection getConnection(long maxWait) throws SQLException;

  /**
   * 从名称为<tt>name</tt>的数据库连接池内获取连接
   *
   * @param name 数据库连接池名称
   * @param maxWait 最大等待时间，<tt>maxWait</tt>小于0时无效，超过<tt>maxWait</tt>(ms)后不再等待
   * @return 数据库连接，如果等待超过<tt>maxWait</tt>(ms)后直接返回<tt>null</tt>
   * @throws SQLException SQL异常
   */
  Connection getConnection(String name, long maxWait) throws SQLException;

  /**
   * 获取数据库连接池连接
   *
   * @return 数据库连接池连接
   * @throws SQLException SQL异常
   */
  PooledConnection getPooledConnection() throws SQLException;

  /**
   * 从名称为<tt>name</tt>的数据库连接池内获取连接池连接
   *
   * @param name 数据库连接池名称
   * @return 数据库连接池连接
   * @throws SQLException SQL异常
   */
  PooledConnection getPooledConnection(String name) throws SQLException;

  /**
   * 获取数据库连接池连接
   *
   * @param maxWait 最大等待时间，<tt>maxWait</tt>小于0时无效，超过<tt>maxWait</tt>(ms)后不再等待
   * @return 数据库连接池连接，如果等待超过<tt>maxWait</tt>(ms)后直接返回<tt>null</tt>
   * @throws SQLException SQL异常
   */
  PooledConnection getPooledConnection(long maxWait) throws SQLException;

  /**
   * 从名称为<tt>name</tt>的数据库连接池内获取连接池连接
   *
   * @param name 数据库连接池名称
   * @param maxWait 最大等待时间，<tt>maxWait</tt>小于0时无效，超过<tt>maxWait</tt>(ms)后不再等待
   * @return 数据库连接池连接，如果等待超过<tt>maxWait</tt>(ms)后直接返回<tt>null</tt>
   * @throws SQLException SQL异常
   */
  PooledConnection getPooledConnection(String name, long maxWait) throws SQLException;

  /**
   * 根据数据库用户名和密码获取连接池连接
   *
   * @param user 数据库用户名
   * @param password 数据库密码
   * @return 数据库连接池连接
   * @throws SQLException SQL异常
   */
  PooledConnection getPooledConnection(String user, String password) throws SQLException;

  /**
   * 关闭所有数据库连接池
   */
  void close();

  /**
   * 关闭名称为<tt>name</tt>的数据库连接池
   *
   * @param name 数据库连接池名称
   */
  void close(String name);
}
