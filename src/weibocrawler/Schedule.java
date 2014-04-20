package weibocrawler;

import static weibocrawler.WeiboCrawlerConstant.NEWUSER_TABLE;
import static weibocrawler.WeiboCrawlerConstant.OLDUSER_TABLE;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import database.DataBaseOperation;
import static weibocrawler.WeiboCrawlerConstant.*;

//调度线程
public class Schedule implements Runnable {

	public static Queue<User> users;
	public static ReentrantLock myLock = new ReentrantLock();
	// 取得newuser表的第一条记录
	public synchronized String getFirstId() throws Exception {

		String id = null;
		Connection con = DataBaseOperation.getConnection();
		String sql = "select uid from " + NEWUSER_TABLE + " limit 1";
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery(sql);
		if (rs.next()) {
			id = rs.getString(1);
		}
		st.close();

		if (id != null) {
			sql = "delete from " + NEWUSER_TABLE + " where uid = " + "\'" + id + "\'";
			st = con.createStatement();
			st.executeUpdate(sql);
			st.close();
			
			sql = "insert into " + OLDUSER_TABLE + " (uid) values(\'" + id + "\')";
			st = con.createStatement();
			st.executeUpdate(sql);
			st.close();
		}

		
		return id;
	}
	

	@Override
	public void run()  {
		
		String username = "liangchen1988915@gmail.com";
		String password = "12345678";
		String id = "3202926715";
		Map<String, String> cookies = null;
		try {
			cookies = Loginer.getCookies(username, password);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
//		while (true) {
//			try {
//				Schedule.myLock.lock();
//				String id = getFirstId();
//				Schedule.myLock.unlock();
				
//				System.out.println(id);
//				if (id != null) {
//					FollowCrawler crawlerFollow = new FollowCrawler(id, cookies);
					// 启动线程
//					crawlerFollow.run();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		
		users = UserFetcher.getUsers();
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4); 
		while(!users.isEmpty())
		{
			User user = users.poll();
			fixedThreadPool.execute(new UserWeiboCrawler(user.getUid(), user.getScreenname(), cookies));
		}
	}

	public static void main(String[] args) {
		new Thread(new Schedule()).start();
	}
}
