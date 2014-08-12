package org.opensource.clearpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.TestCase;

public class TryWithResourcesCase extends TestCase {
	private String jdbcUrl = "jdbc:mysql://127.0.0.1:3306/test?characterEncoding=gbk";
	private String user = "root";
	private String password = "1";

	public void testNormal() throws Exception {
		System.out.println("test normal situtation:");
		Connection con = null;
		try (Connection conn = DriverManager.getConnection(this.jdbcUrl,
				this.user, this.password)) {
			con = conn;
			System.out.println("in the try:" + conn.isClosed());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("after try:" + con.isClosed());
	}

	public void testAccident() throws Exception {
		System.out.println("test accident situtation:");
		Connection con = null;
		try (Connection conn = DriverManager.getConnection(this.jdbcUrl,
				this.user, this.password)) {
			con = conn;
			System.out.println("in the try:" + conn.isClosed());
			throw new RuntimeException("wrong");
		} catch (Exception e) {
			System.out.println("in the catch:" + con.isClosed());
		}
		System.out.println("after try:" + con.isClosed());
	}
}
