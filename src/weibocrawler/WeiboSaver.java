package weibocrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static weibocrawler.WeiboCrawlerConstant.*;

public class WeiboSaver {
	public static void saveWeibo(Weibo weibo)
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");
	        Connection conn;
	        conn = DriverManager.getConnection(MYSQLURL,MYSQL_USERNAME,MYSQL_PASSWORD);
	        String sql = "insert into " + WEIBO_TABLE + " values (?, ?, ?, ?, ?)";
	        PreparedStatement stmt = conn.prepareStatement(sql); 
	        stmt.setString(1, weibo.getId());
	        stmt.setString(2, weibo.getUserName());
	        stmt.setString(3, weibo.getScreenName());
	        stmt.setString(4, weibo.getContent());
	        stmt.setTimestamp(5, new Timestamp(weibo.getTime().getTime()));
	        System.out.println(stmt);
	        stmt.executeUpdate();
	        	        
	        stmt.close();
	        conn.close();
		} catch (Exception e) {
	        e.printStackTrace();
		}
	}
	public static Date saveWeibos(List<Weibo> weibos, Date lastWeiboTime)
	{
		Date lastestWeiboTime = weibos.get(0).getTime();
		try {
			Class.forName("com.mysql.jdbc.Driver");
	        Connection conn;
	        conn = DriverManager.getConnection(MYSQLURL,MYSQL_USERNAME,MYSQL_PASSWORD);
	        String sql = "insert into " + WEIBO_TABLE + " values (?, ?, ?, ?, ?)";
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        for(Weibo weibo : weibos)
	        {
	        	if(weibo.getTime().before(lastWeiboTime)) //如果当前微博时间早于前次保存微博最晚时间那么退出
	        		continue;
		        stmt.setString(1, weibo.getId());
		        stmt.setString(2, weibo.getUserName());
		        stmt.setString(3, weibo.getScreenName());
		        stmt.setString(4, weibo.getContent());
		        stmt.setTimestamp(5, new Timestamp(weibo.getTime().getTime()));
		        System.out.println(stmt);
		        if(stmt.executeUpdate() > 0 && weibo.getTime().after(lastestWeiboTime))
	        		lastestWeiboTime = weibo.getTime();
	        }
	        stmt.close();
	        conn.close();
		} catch (Exception e) {
	        e.printStackTrace();
		}
        return lastestWeiboTime;
	}
}
