package database;

import static weibocrawler.WeiboCrawlerConstant.MYSQLURL;
import static weibocrawler.WeiboCrawlerConstant.MYSQL_PASSWORD;
import static weibocrawler.WeiboCrawlerConstant.MYSQL_USERNAME;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DataBaseOperation {

	private static Connection con = null;
	
	public static Connection getConnection() {
		
		if (con != null) {
			return con;
		}
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		Connection con = null;
		try {
			con = DriverManager.getConnection(MYSQLURL, MYSQL_USERNAME, MYSQL_PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return con;
	}
	
	public static void close() throws Exception {
		
		if (con != null) {
			if (!con.isClosed()) {
				con.close();
			}
		}
	}
}
