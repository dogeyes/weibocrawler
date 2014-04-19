package weibocrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SaveWeibo {
	public static void saveWeibo(Weibo weibo)
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String mysqlUrl="jdbc:mysql://127.0.0.1:3306/weibo?useUnicode=true&characterEncoding=utf8";
	        Connection conn;
	        conn = DriverManager.getConnection(mysqlUrl,"root","");
	        String sql = "insert into weibo_1 values (?, ?, ?, ?, ?)";
	        PreparedStatement stmt = conn.prepareStatement(sql); 
	        stmt.setString(1, weibo.getId());
	        stmt.setString(2, weibo.getUserName());
	        stmt.setString(3, weibo.getScreenName());
	        stmt.setString(4, weibo.getContent());
	        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        stmt.setTimestamp(5, Timestamp.valueOf(dateFormat.format(weibo.getTime())));
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
			String mysqlUrl="jdbc:mysql://127.0.0.1:3306/weibo?useUnicode=true&characterEncoding=utf8";
	        Connection conn;
	        conn = DriverManager.getConnection(mysqlUrl,"root","");
	        String sql = "insert into weibo_1 values (?, ?, ?, ?, ?)";
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        for(Weibo weibo : weibos)
	        {
	        	if(weibo.getTime().before(lastWeiboTime)) //如果当前微博时间早于前次保存微博最晚时间那么退出
	        		return lastestWeiboTime;
		        stmt.setString(1, weibo.getId());
		        stmt.setString(2, weibo.getUserName());
		        stmt.setString(3, weibo.getScreenName());
		        stmt.setString(4, weibo.getContent());
		        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		        stmt.setTimestamp(5, Timestamp.valueOf(dateFormat.format(weibo.getTime())));
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
