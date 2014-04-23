package weibocrawler;

import static weibocrawler.WeiboCrawlerConstant.NEWUSER_TABLE;
import static weibocrawler.WeiboCrawlerConstant.OLDUSER_TABLE;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import database.DataBaseOperation;
import static weibocrawler.WeiboCrawlerConstant.*;

//调度线程
public class Schedule extends TimerTask implements Runnable{

	private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(THREAD_NUM);;
	public static Queue<User> users;
	public static ReentrantLock myLock = new ReentrantLock();
	private Map<String, String> cookies;
	private String uid;
	// 取得newuser表的第一条记录
	public Schedule(String uid, Map<String, String> cookies, ExecutorService fixedThreadPool)
	{
		this.uid = uid;
		this.cookies = cookies;
		this.fixedThreadPool = fixedThreadPool;
	}
//	public synchronized String getFirstId() throws Exception {
//
//		String id = null;
//		Connection con = DataBaseOperation.getConnection();
//		String sql = "select uid from " + NEWUSER_TABLE + " limit 1";
//		Statement st = con.createStatement();
//		ResultSet rs = st.executeQuery(sql);
//		if (rs.next()) {
//			id = rs.getString(1);
//		}
//		st.close();
//
//		if (id != null) {
//			sql = "delete from " + NEWUSER_TABLE + " where uid = " + "\'" + id + "\'";
//			st = con.createStatement();
//			st.executeUpdate(sql);
//			st.close();
//			
//			sql = "insert into " + OLDUSER_TABLE + " (uid) values(\'" + id + "\')";
//			st = con.createStatement();
//			st.executeUpdate(sql);
//			st.close();
//		}
//
//		
//		return id;
//	}
//	

	@Override
	public void run()  {
		FollowCrawler crawlerFollow = new FollowCrawler(uid, cookies, fixedThreadPool);
		fixedThreadPool.execute(crawlerFollow);
	}

	public static void test(String uid , Map<String, String> cookies ) //测试函数
	{
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4); 
		Timer timer = new Timer();
		timer.schedule(new UserWeiboCrawlerSchedule(uid,cookies, fixedThreadPool), new Date(), CRAWLER_WEIBO_PEROID);
		timer.schedule(new Schedule(uid, cookies, fixedThreadPool), new Date(), CRAWLER_WEIBO_PEROID);
	}
	public static void schedule(String uid)
	{
		String username = "liangchen1988915@gmail.com";
		String password = "12345678";
		Map<String, String> cookies = null;
		try {
			cookies = Loginer.getCookies(username, password);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Timer timer1 = new Timer();
		Date current = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(current);
		calendar.add(Calendar.HOUR_OF_DAY, CRAWLER_USER_PEROID);
		timer1.schedule(new UserCrawlerSchedule(uid, cookies, fixedThreadPool), new Date(), CRAWLER_USER_PEROID*60l*60l*1000l);

		calendar.setTime(current);
		calendar.add(Calendar.MINUTE, (int)(CRAWLER_WEIBO_PEROID / 1000 /60));
		Timer timer2 = new Timer();
		timer2.schedule(new UserWeiboCrawlerSchedule(uid,cookies, fixedThreadPool), new Date(), CRAWLER_WEIBO_PEROID);
		
	}
	public static void main(String[] args) {
		schedule("3202926715");
		schedule("2100689171");
//		String username = "liangchen1988915@gmail.com";
//		String password = "12345678";
////		String username = "dexctor@gmail.com";
////		String password = "dogeyes007";
//		String id1 = "3202926715";
//		String id2 = "2100689171";
// 		Map<String, String> cookies = null;
//		try {
//			cookies = Loginer.getCookies(username, password);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
		
//		Timer timer1 = new Timer();
//		timer1.schedule(new UserCrawlerSchedule(id2, cookies, fixedThreadPool), new Date(), CRAWLER_USER_PEROID*60l*60l*1000l);
//		timer1.schedule(new UserCrawlerSchedule(id1, cookies, fixedThreadPool), new Date(), CRAWLER_USER_PEROID*60l*60l*1000l);


//		Timer timer2 = new Timer();
//		timer2.schedule(new UserWeiboCrawlerSchedule(id1,cookies, fixedThreadPool), new Date(), CRAWLER_WEIBO_PEROID);
//		timer2.schedule(new UserWeiboCrawlerSchedule(id2,cookies, fixedThreadPool), new Date(), CRAWLER_WEIBO_PEROID);

	}
}
