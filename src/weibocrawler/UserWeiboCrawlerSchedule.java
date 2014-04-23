package weibocrawler;

import java.util.Map;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class UserWeiboCrawlerSchedule extends TimerTask implements Runnable{
	private static Queue<User> users;
	private Map<String, String> cookies;
	private String uid;
	private ExecutorService fixedThreadPool;
	
	public UserWeiboCrawlerSchedule(String uid, Map<String, String> cookies, ExecutorService fixedThreadPool)
	{
		this.uid = uid;
		this.cookies = cookies;
		this.fixedThreadPool = fixedThreadPool;
	}
	
	@Override
	public void run()  {
		users = UserFetcher.getUsers(uid);
		while(!users.isEmpty())
		{
			User user = users.poll();
			fixedThreadPool.execute(new UserWeiboCrawler(user.getUid(), user.getScreenname(), cookies));
		}
	}
}
