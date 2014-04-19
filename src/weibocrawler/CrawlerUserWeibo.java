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

import org.jsoup.nodes.Document;

public class CrawlerUserWeibo implements Runnable {
	
	private String userName;
	private String baseUrl = "http://weibo.cn";
	private Map<String, String> cookies;
	private Date userTime;
	
	public CrawlerUserWeibo(String url, String username, Map<String, String> cookies)
	{
		this.baseUrl = url;
		this.userName = username;
		this.cookies = cookies;
		init();
	}
	public CrawlerUserWeibo(String username, Map<String, String> cookies)
	{
		this.userName = username;
		this.cookies = cookies;
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
			CrawlerUserWeibo crawlerUserWeibo = new CrawlerUserWeibo("http://weibo.cn", "vczh", cookies);
			crawlerUserWeibo.crawler();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	public void crawler()
	{
		Date lastestDate = userTime;
		String urlsuffix = '/' + userName;
		for(;;)
		{
			try {
				Document page = CrawlerFromPhone.getPage(cookies, baseUrl + urlsuffix);
				List<Weibo> weibos = ResolvePage.resolveUserPage(page, userName);
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
	}
	
}
