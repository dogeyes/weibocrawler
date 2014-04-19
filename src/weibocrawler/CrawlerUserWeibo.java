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
import java.util.Scanner;

import org.apache.ibatis.annotations.Update;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CrawlerUserWeibo implements Runnable {
	
	private String userName;
	private String screenName;
	private String baseUrl = "http://weibo.cn";
	private Map<String, String> cookies;
	private Date userTime;
	
	public CrawlerUserWeibo(String url, String username, String screenName, Map<String, String> cookies)
	{
		this.baseUrl = url;
		this.userName = username;
		this.cookies = cookies;
		this.screenName = screenName;
		init();
	}
	public CrawlerUserWeibo(String username,String screenName, Map<String, String> cookies)
	{
		this.userName = username;
		this.cookies = cookies;
		this.screenName = screenName;
		init();
	}
	
	private void init()
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String mysqlUrl="jdbc:mysql://127.0.0.1:3306/weibo?useUnicode=true&characterEncoding=utf8";
	        Connection conn;
	        conn = DriverManager.getConnection(mysqlUrl,"root","");
	        String sql = "select usertime from user where username=?";
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setString(1, userName);
	        System.out.println(stmt);
	        ResultSet rs = stmt.executeQuery();
	        if(rs.next()) //获取上次抓取的时间
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
	
		CrawlerFromPhone crawler = new CrawlerFromPhone("dexctor@gmail.com", "dogeyes007");
		try {
			Map<String, String> cookies = crawler.getCookies();
			CrawlerUserWeibo crawlerUserWeibo1 = new CrawlerUserWeibo("http://weibo.cn", "1597889497", "璇_二月", cookies);
			CrawlerUserWeibo crawlerUserWeibo2 = new CrawlerUserWeibo("http://weibo.cn", "tianchunbinghe", "田春冰河", cookies);
			CrawlerUserWeibo crawlerUserWeibo3 = new CrawlerUserWeibo("http://weibo.cn", "jeffz", "老赵", cookies);
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
				Document page = CrawlerFromPhone.getPage(cookies, baseUrl + urlsuffix);
				List<Weibo> weibos = ResolvePage.resolveUserPage(page, userName, screenName);
				Date currentPageTime = SaveWeibo.saveWeibos(weibos, userTime);
				if(currentPageTime.after(lastestDate))
					lastestDate = currentPageTime;
				if(currentPageTime.before(userTime))  //当前爬到的微博已经比上一次的要晚，退出
					break;
				urlsuffix = CrawlerFromPhone.nextPageUrlSuffix(page); //下一页地址的后半部分
				if(urlsuffix == null)   //没有下一页
					break;
				try {
					Thread.sleep(1000);
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
			String mysqlUrl="jdbc:mysql://127.0.0.1:3306/weibo?useUnicode=true&characterEncoding=utf8";
	        Connection conn;
	        conn = DriverManager.getConnection(mysqlUrl,"root","");
	        String sql = "update user set usertime=? where username=?";
	        
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
