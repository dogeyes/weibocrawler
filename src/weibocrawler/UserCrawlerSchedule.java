package weibocrawler;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class UserCrawlerSchedule extends TimerTask implements Runnable {
	private Map<String, String> cookies;
	private String uid;
	private ExecutorService fixedThreadPool;
	
	public UserCrawlerSchedule(String uid, Map<String, String> cookies, ExecutorService fixedThreadPool)
	{
		this.uid = uid;
		this.cookies = cookies;
		this.fixedThreadPool = fixedThreadPool;
	}
	
	@Override
	public void run()  {
		FollowCrawler crawlerFollow = new FollowCrawler(uid, cookies, fixedThreadPool);
		fixedThreadPool.execute(crawlerFollow);
	}

}
