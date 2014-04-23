package weibocrawler;

import static weibocrawler.WeiboCrawlerConstant.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.nodes.Document;

import database.DataBaseOperation;

// 爬取用户朋友的信息的线程
public class FollowCrawler implements Runnable {
	
	private String id;
	
	private String screenName;
	private ExecutorService fixedThreadPool;

	List<String> follow_ids = new ArrayList<String>();

	Map<String, String> cookies = new HashMap<String, String>();

	private String baseUrl = "http://weibo.cn";
	
	
	public FollowCrawler(String id, Map<String, String> cookies, ExecutorService fixedThreadPool) {
		this.id = id;
		this.screenName = null;
		this.cookies = cookies;
		this.fixedThreadPool = fixedThreadPool;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	private User getOwner(String uid)
	{
		Connection con = DataBaseOperation.getConnection();
		String sql = "select * from " + USER_TABLE + " where uid=?";
		try {
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, uid);
			System.out.println(stmt);
			ResultSet rs = stmt.executeQuery();
			if(rs.next())
			{
				Integer id = rs.getInt(1);
				String screenname = rs.getString(3);
				Date lastTime = rs.getTimestamp(4);
				Date lastUserTime = rs.getTimestamp(5);
				User user = new User(id, uid, screenname, lastTime, lastUserTime);
				return user;
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void insertOwner(User owner)
	{
		Connection con = DataBaseOperation.getConnection();
		String sql = "insert into " + USER_TABLE + " values (?,?,?,?,?)";
		try {
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setInt(1, 0);
			stmt.setString(2, owner.getUid());
			stmt.setString(3, owner.getScreenname());
			stmt.setTimestamp(4, new Timestamp(owner.getLastTime().getTime()));
			stmt.setTimestamp(5, new Timestamp(owner.getLastUserTime().getTime()));
			System.out.println(stmt);
			stmt.executeUpdate();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateOwner(User owner)
	{
		Connection con = DataBaseOperation.getConnection();
		String sql = "update " + USER_TABLE + " set lastUserTime=? where uid=?";
		try {
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setTimestamp(1, new Timestamp(owner.getLastUserTime().getTime()));
			stmt.setString(2, owner.getUid());
			System.out.println(stmt);
			stmt.executeUpdate();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void crawler() {

		User owner = getOwner(id);
		if(owner != null)	//如果owner不为空
		{
			Date current = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(current);
			cal.add(Calendar.DAY_OF_MONTH, -1);
			if(owner.getLastUserTime().after(cal.getTime()))  //如果最近爬过该用户，爬取用户好友的微博但不爬用户的好友
			{
				Queue<User> users = UserFetcher.getUsers(id);
				while(!users.isEmpty()) //爬取用户的同时去爬取用户的微博
				{
					User user = users.poll();
					fixedThreadPool.execute(new UserWeiboCrawler(user.getUid(), user.getScreenname(), cookies));
				}
				return;
			}
		}
		if(owner == null) //如果主用户不再user表中，说明主用户是新的
		{
			owner = new User(0, id, null, null, null);
			insertOwner(owner);
		}
		// 初始值
		String urlsuffix = "/" +  id + "/follow";
		// 查询用户朋友相关的信息
		String beginString = "http://weibo.cn/attention";
		
		List<User> waitForProcess_list = new LinkedList<User>();
		
		while (true) {
			try {
				// 爬取用户好友列表页面
				Document followPage = PageFetcher.getPage(cookies, baseUrl + urlsuffix);
				
				List<User> users = PageResolver.resolverUserPage(followPage);
				//查找并插入
				for(User user: users)
					if (!waitForProcess_list.contains(user))
						waitForProcess_list.add(user);
				
				// 下一页地址的后缀地址部分
				urlsuffix = PageResolver.nextPageUrlSuffix(followPage); 
				System.out.println(urlsuffix);
				if (urlsuffix == null) {// 没有下一页
					//批量插入处理
					List<User> allUsers = new ArrayList<User>(waitForProcess_list); 
					batchInsertUserRelationTable(id, allUsers);  //将新的用户关系插入到数据库中
					batchFindUsers(USER_TABLE, waitForProcess_list);   //去除已经在数据库里的用户
//					batchFindUsers(OLDUSER_TABLE, waitForProcess_list);
//					batchInsertNewUserTable(waitForProcess_list);      
					batchInsertUserTable(waitForProcess_list);			//将新用户加入到数据库中
					owner.setLastUserTime(new Date());
					updateOwner(owner); //更新主用户信息，主要是更新主用户的最后爬取用户时间信息。
					for(User user: allUsers) //爬取用户的同时去爬取用户的微博
					{
						fixedThreadPool.execute(new UserWeiboCrawler(user.getUid(), user.getScreenname(), cookies));
					}
					break;
				}
				try {
					Thread.sleep(WAIT_TIME);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// 批量查找
	public synchronized void batchFindUsers(String tableName, List<User> follows) throws Exception {
		
		List<User> list = new ArrayList<User>();
		Connection conn = DataBaseOperation.getConnection();
		String sql = "select * from " + tableName + " where uId = ";
		Statement statement = conn.createStatement();
		// 批量插入
		for (User follow : follows) {
			String mySql = sql  + follow.getUid() ;
			System.out.println(mySql);
			ResultSet rs = statement.executeQuery(mySql);
			if (rs.next()) {
				Integer id = rs.getInt(1);
				String uid = rs.getString(2);
				String screenname = rs.getString(3);
				Date lastTime = rs.getTimestamp(4);
				User user = new User(id, uid, screenname, lastTime);
				list.add(user);
			}
		}
		follows.removeAll(list);
	}
	
	// 向用户表中批量插入新用户信息
	public synchronized void batchInsertUserTable(List<User> follows) {
		Connection conn = DataBaseOperation.getConnection();
		String sql = "insert into " + USER_TABLE + " values (?, ?, ?, ?,?)";
		PreparedStatement stmt = null;
		try {
			// 批量插入
			stmt = conn.prepareStatement(sql);
			for (User follow : follows) {
				stmt.setInt(1, 0);
				stmt.setString(2, follow.getUid());
				stmt.setString(3, follow.getScreenname());
				stmt.setTimestamp(4,new Timestamp(follow.getLastTime().getTime()));
				stmt.setTimestamp(5, new Timestamp(follow.getLastUserTime().getTime()));
				System.out.println(stmt);
				stmt.executeUpdate();
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// 向用户表中批量插入用户好友的信息
	public synchronized void batchInsertNewUserTable(List<User> follows) {
		Connection conn = DataBaseOperation.getConnection();
		String sql = "insert into " + NEWUSER_TABLE + " values (?, ?, ?, ?)";
		PreparedStatement stmt = null;
//		System.out.println(123);
		try {
			stmt = conn.prepareStatement(sql);
			// 批量插入
			for (User follow : follows) {
				stmt.setInt(1, 0);
				stmt.setString(2, follow.getUid());
				stmt.setString(3, follow.getScreenname());
				stmt.setTimestamp(4, new Timestamp(follow.getLastTime().getTime()));
				System.out.println(stmt);
				stmt.executeUpdate();
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// 批量插入用户关系列表(有bug)
	public synchronized void batchInsertUserRelationTable(String first_id, List<User> follows) {
		Set<String> followerUids = new HashSet<String>();
		for(User user : follows)
			followerUids.add(user.getUid());
		Connection conn = DataBaseOperation.getConnection();
		PreparedStatement stmt;

		String sql = "select uid2 from " + USER_RELATIONSHIP_TABLE + " where uid1=?"; //去除已有的好友关系
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, first_id);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
			{
				followerUids.remove(rs.getString(1));
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		sql = "insert into " + USER_RELATIONSHIP_TABLE + " values (?, ?, ?)";
		try {
			// 批量插入
			stmt = conn.prepareStatement(sql);
			for (String followerUid: followerUids) {
				stmt.setInt(1, 0);
				stmt.setString(2, first_id);
				stmt.setString(3, followerUid);
				System.out.println(stmt);
				stmt.executeUpdate();
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		crawler();
	}

	public static void main(String[] args) throws Exception {
		
		String id = "2143722067";
		String username = "liangchen1988915@gmail.com";
		String password = "12345678";
		Map<String, String> cookies = Loginer.getCookies(username, password);
		FollowCrawler crawlerFollow = new FollowCrawler(id, cookies, Executors.newFixedThreadPool(4));
		// 启动线程
		new Thread(crawlerFollow).start();
	}
}
