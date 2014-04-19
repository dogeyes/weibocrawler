package weibocrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.nodes.Document;

public class CrawlerWeibo {
	private static Date lastWeiboTime;
	private static String datefileString = "src/lastWeiboTime.dat";
	private static DateFormat dateFormat;
	
	public static void crawlerWeibo() throws IOException
	{
		Scanner in = new Scanner(new File(datefileString));
		String timeString = in.nextLine();
		System.out.println(timeString);
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date lastestDate = lastWeiboTime;
		try {
			lastWeiboTime = dateFormat.parse(timeString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Scanner stdin = new Scanner(System.in);
			System.out.println("Enter your username: ");
			String username = stdin.next();
			System.out.println("Enter your password: ");
			String password = stdin.next();
			CrawlerFromPhone crawler = new CrawlerFromPhone(username, password);
			Map<String, String> cookies = crawler.getCookies();
			
			
			String index_url = "http://weibo.cn";
			String urlsuffix = "";
			lastestDate = lastWeiboTime;
			for(;;)
			{
				Document page = crawler.getPage(cookies, index_url + urlsuffix);
				List<Weibo> weibos = ResolvePage.resolvePage(page);
				Date currentPageTime = SaveWeibo.saveWeibos(weibos, lastWeiboTime);
				if(currentPageTime.after(lastestDate))
					lastestDate = currentPageTime;
				if(currentPageTime.before(lastWeiboTime))
					break;
				urlsuffix = crawler.nextPageUrlSuffix(page);
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			FileWriter out = new FileWriter(new File(datefileString));
			out.write(dateFormat.format(lastestDate));
			out.flush();
			out.close();
		}
 	}
	public static void main(String[] args) {
		try {
			crawlerWeibo();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("finish");
	}
}
