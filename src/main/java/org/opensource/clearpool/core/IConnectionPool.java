package org.opensource.clearpool.core;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.PooledConnection;

import org.opensource.clearpool.configuration.ConfigurationVO;

/**
 * This is the common interface of the pool, every poolImpl should implements it
 * so that they can realize the three method: getConnection(),closeAll() and
 * close(String name).
 * 
 * @author xionghui
 * @date 26.07.2014
 * @version 1.0
 */
public interface IConnectionPool {
	/**
	 * Get a connection from the pool.
	 * 
	 * @return a free connection from the pool
	 */
	public Connection getConnection() throws SQLException;

	/**
	 * Get a connection from the pool by name.
	 * 
	 * @param name
	 *            is the pool's name
	 * @return a free connection from the pool
	 */
	public Connection getConnection(String name) throws SQLException;

	/**
	 * Get a pooledConnection from the pool by name.
	 * 
	 * @param name
	 *            is the pool's name
	 * @return a free pooledConnection from the pool
	 */
	public PooledConnection getPooledConnection(String name)
			throws SQLException;

	/**
	 * Init pool by the default path.
	 * 
	 * Note:if the default path had been init and not removed,it will throw a
	 * exception.
	 */
	public void init();

	/**
	 * Init pool by the given path.
	 * 
	 * Note:if the path had been init and not removed,it will throw a exception.
	 */
	public void initPath(String path);

	/**
	 * Init pool by vo.
	 * 
	 * Note:if the vo's alias had been init and not removed,it will throw a
	 * exception.
	 */
	public void initVO(ConfigurationVO vo);

	/**
	 * Init pool by voList.
	 * 
	 * Note:if the vo's alias had been init and not removed,it will throw a
	 * exception.
	 */
	public void initVOList(List<ConfigurationVO> voList);

	/**
	 * Close the database and connection in the pool which has the name of
	 * {@code name}.
	 * 
	 * Note:the method does nothing if the name is not existed or the name of
	 * the database has been closed.
	 * 
	 * @param name
	 *            is the pool's name
	 */
	public void close(String name);

	/**
	 * Close all the database and connection in the pool.
	 */
	public void close() throws IOException;

	/**
	 * If we destroy the pool,we can't use it any more.
	 */
	public void destory();
}
