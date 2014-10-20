package org.opensource.clearpool;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import junit.framework.TestCase;

/**
 * Oracle Test.
 * 
 * Note: <br />
 * 1.replace jdbcClass with your database's jdbc-class please; <br />
 * 2.replace url with your database's url please; <br />
 * 3.replace user with your database's user please; <br />
 * 4.replace password with your database's password please; <br />
 * 5.replace preparedSql and statementSql with your valid sql please.
 * 
 * @author xionghui
 * @date 24.09.2014
 * @version 1.0
 */
public class CompareStatementsInOracle extends TestCase {
	private String jdbcClass = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@20.10.1.224:1521:ora10g";
	private String user = "cm65dev";
	private String password = "1";

	private String preparedSql = "select count(1) from cm_stuff where dr=?";
	private String statementSql = "select count(1) from cm_stuff where dr=";

	private int loop = 5;
	private int count = 5000;

	static {
		System.out.println("Oracle:");
	}

	@Override
	public void setUp() throws Exception {
		System.setProperty("jdbc.drivers", this.jdbcClass);
	}

	public void test_Statement() throws Exception {
		System.out.print("Statement cost: ");
		for (int i = 0; i < this.loop; i++) {
			Connection conn = DriverManager.getConnection(this.url, this.user,
					this.password);
			long begin = System.currentTimeMillis();
			for (int j = 0; j < this.count; j++) {
				Statement stm = conn.createStatement();
				stm.executeQuery(this.statementSql + j);
				stm.close();
			}
			System.out.print((System.currentTimeMillis() - begin) + "ms	");
			conn.close();
		}
		System.out.println();
	}

	public void test_PreparedStatement() throws Exception {
		System.out.print("PreparedStatement cost: ");
		Connection conn = DriverManager.getConnection(this.url, this.user,
				this.password);
		for (int i = 0; i < this.loop; i++) {
			long begin = System.currentTimeMillis();
			for (int j = 0; j < this.count; j++) {
				PreparedStatement stm = conn.prepareStatement(this.preparedSql);
				stm.setInt(1, j);
				stm.executeQuery();
				stm.close();
			}
			System.out.print((System.currentTimeMillis() - begin) + "ms	");
		}
		conn.close();
		System.out.println();
	}

	public void test_CallableStatement() throws Exception {
		System.out.print("CallableStatement cost: ");
		Connection conn = DriverManager.getConnection(this.url, this.user,
				this.password);
		for (int i = 0; i < this.loop; i++) {
			long begin = System.currentTimeMillis();
			for (int j = 0; j < this.count; j++) {
				CallableStatement stm = conn.prepareCall(this.preparedSql);
				stm.setInt(1, j);
				stm.executeQuery();
				stm.close();
			}
			System.out.print((System.currentTimeMillis() - begin) + "ms	");
		}
		conn.close();
		System.out.println();
	}

	@Override
	public void tearDown() throws Exception {
	}
}
