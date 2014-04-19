package Test;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import weibocrawler.Weibo;

public class TestWeiboSave {
	public static void main(String[] args) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String mysqlUrl="jdbc:mysql://127.0.0.1:3306/weibo";
            Connection conn;
            conn = DriverManager.getConnection(mysqlUrl,"root","");
            Statement stmt = conn.createStatement(); 
            String sql = "select * from weibo_1";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()){
                System.out.print(rs.getString(1) + "\t");
                System.out.print(rs.getString(2) + "\t");
                System.out.print(rs.getString(3) + "\t");
                System.out.print(rs.getString(4) + "\t");
                System.out.print(rs.getTimestamp(5) + "\t");


                System.out.println();
            }
            rs.close();
            stmt.close();
            conn.close();
		} catch (Exception e) {
            e.printStackTrace();
		}
		
	}

}
