package weibocrawler;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import Log.HtmlFileLog;

import static weibocrawler.WeiboCrawlerConstant.*;

public class PageFetcher {
	public static Document getPage(Map<String, String> cookies, String url) throws IOException //获得给定url的页面
	{
		System.out.println("fetching ---- " + url);
		Random random = new Random();
    	Document docPage = Jsoup.connect(url).userAgent(USER_AGENT).cookies(cookies).get();
    	HtmlFileLog.htmlFileLog(docPage.html(), "page"+random.nextLong()+".html");  //得到页面
    	return docPage;
	}

}
