package weibocrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;


import static weibocrawler.WeiboCrawlerConstant.*;
public class UserWeiboCrawler implements Runnable {
	
	private String userName;
	private String screenName;
	private String baseUrl = INDEX_PAGE;
	private Map<String, String> cookies;
	private Date userTime;
	
	public UserWeiboCrawler(String url, String username, String screenName, Map<String, String> cookies)
	{
		this.baseUrl = url;
		this.userName = username;
		this.cookies = cookies;
		this.screenName = screenName;
		init();
	}
	public UserWeiboCrawler(String username,String screenName, Map<String, String> cookies)
	{
		this.userName = username;
		this.cookies = cookies;
		this.screenName = screenName;
		init();
	}
	
	private void init()  //初始化，从数据库中读取上次抓取微博的时间
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");
	        Connection conn;
	        conn = DriverManager.getConnection(MYSQLURL,MYSQL_USERNAME, MYSQL_PASSWORD);
	        String sql = "select usertime from " + USER_TABLE + " where username=?";
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setString(1, userName);
	        System.out.println(stmt);
	        ResultSet rs = stmt.executeQuery();
	        if(rs != null && rs.next()) //获取上次抓取的时间
	        	this.userTime = rs.getTimestamp(1);
	        else {   	//数据库中没有用户的时间，时间设置位7天以前
	        	Calendar calendar = Calendar.getInstance();
	        	calendar.setTime(new Date());
	        	calendar.add(Calendar.DAY_OF_YEAR, -7);
				this.userTime = calendar.getTime();
			}
	        rs.close();
	        stmt.close();
	        conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
//		Scanner stdin = new Scanner(System.in);
//		System.out.println("Enter your username: ");
//		String username = stdin.next();
//		System.out.println("Enter your password: ");
//		String password = stdin.next();
	
		try {
			Map<String, String> cookies = Loginer.getCookies("dexctor@gmail.com", "dogeyes007");
			UserWeiboCrawler crawlerUserWeibo1 = new UserWeiboCrawler("http://weibo.cn", "1597889497", "璇_二月", cookies);
			UserWeiboCrawler crawlerUserWeibo2 = new UserWeiboCrawler("http://weibo.cn", "tianchunbinghe", "田春冰河", cookies);
			UserWeiboCrawler crawlerUserWeibo3 = new UserWeiboCrawler("http://weibo.cn", "jeffz", "老赵", cookies);
			Thread thread1 = new Thread(crawlerUserWeibo1);
			Thread thread2 = new Thread(crawlerUserWeibo2);
			Thread thread3 = new Thread(crawlerUserWeibo3);
			
			thread1.start();
			thread2.start();
			thread3.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void run() {
		crawler();		
	}
	public void crawler()
	{
		Date lastestDate = userTime;
		String urlsuffix = '/' + userName;
				
		for(;;)
		{
			try {
				Document page = PageFetcher.getPage(cookies, baseUrl + urlsuffix);   //获取页面
				List<Weibo> weibos = PageResolver.resolveUserPage(page, userName, screenName); //分析页面，得到微博
				Date currentPageTime = WeiboSaver.saveWeibos(weibos, userTime);  //存储微博
				if(currentPageTime.after(lastestDate))
					lastestDate = currentPageTime;
				if(currentPageTime.before(userTime))  //当前爬到的微博已经比上一次的要晚，退出
					break;
				urlsuffix = PageResolver.nextPageUrlSuffix(page); //下一页地址的后半部分
				if(urlsuffix == null)   //没有下一页
					break;
				try {
					Thread.sleep(WAIT_TIME);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		close(userName, lastestDate);
	}
	
	public static void close(String userName, Date lastestDate)
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");
	        Connection conn;
	        conn = DriverManager.getConnection(MYSQLURL,MYSQL_USERNAME, MYSQL_PASSWORD);
	        String sql = "update " + USER_TABLE + " set usertime=? where username=?";
	        
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        stmt.setTimestamp(1, Timestamp.valueOf(dateFormat.format(lastestDate)));
	        stmt.setString(2, userName);
	        
	        System.out.println(stmt);
	        if(stmt.executeUpdate() <= 0)
	        	throw new Exception();
	        stmt.close();
	        conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
