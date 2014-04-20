package weibocrawler;

import static weibocrawler.WeiboCrawlerConstant.NEWUSER_TABLE;
import static weibocrawler.WeiboCrawlerConstant.OLDUSER_TABLE;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import database.DataBaseOperation;

//调度线程
public class Schedule implements Runnable {

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
		
		String username = "yyb1989249@sohu.com";
		String password = "5234415";
		
		Map<String, String> cookies = null;
		try {
			cookies = Loginer.getCookies(username, password);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
//		while (true) {
			try {
				Schedule.myLock.lock();
				String id = getFirstId();
				Schedule.myLock.unlock();
				
				System.out.println(id);
				if (id != null) {
					FollowCrawler crawlerFollow = new FollowCrawler(id, cookies);
					// 启动线程
					new Thread(crawlerFollow).start();
				}
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
//		}
	}

	public static void main(String[] args) {
		new Thread(new Schedule()).start();
	}
}
