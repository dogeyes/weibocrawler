package weibocrawler;

import static weibocrawler.WeiboCrawlerConstant.NEWUSER_TABLE;
import static weibocrawler.WeiboCrawlerConstant.WEIBO_TABLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import database.DataBaseOperation;
import static weibocrawler.WeiboCrawlerConstant.*;

public class UserFetcher {
	public static Queue<User> getUsers()
	{
		Queue<User> users = new LinkedList<User>();
		Connection conn = DataBaseOperation.getConnection();
		String sql = "select *  from " + USER_TABLE;
		try{
	        PreparedStatement stmt = conn.prepareStatement(sql); 
	        System.out.println(stmt);
	        
	        ResultSet rs = stmt.executeQuery();
	        while(rs.next())
	        {
	        	User user = new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4),rs.getTimestamp(5));
	        	users.add(user);
	        }
	        rs.close();
	        stmt.close();	
	        return users;
		}catch(SQLException e)
		{
			e.printStackTrace();
		}

		return users;
	}
	public static Queue<User> getUsers(String id)
	{
		Queue<User> users = new LinkedList<User>();
		Connection conn = DataBaseOperation.getConnection();
		String sql = "select " + USER_TABLE + ".id, " + USER_TABLE + ".uid, " + USER_TABLE + ".screenname, " + 
						USER_TABLE + ".lastTime, " + USER_TABLE + ".lastUserTime " + " from " + USER_TABLE +
						 ", " + USER_RELATIONSHIP_TABLE + " where " + USER_TABLE + ".uid=" + USER_RELATIONSHIP_TABLE +
						".uid2 and " + USER_RELATIONSHIP_TABLE + ".uid1=?";
		try{
	        PreparedStatement stmt = conn.prepareStatement(sql); 
	        stmt.setString(1, id);
	        System.out.println(stmt);
	        
	        ResultSet rs = stmt.executeQuery();
	        while(rs.next())
	        {
	        	User user = new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4), rs.getTimestamp(5));
	        	users.add(user);
	        }
	        rs.close();
	        stmt.close();	
	        return users;
		}catch(SQLException e)
		{
			e.printStackTrace();
		}

		return users;
	}
}
