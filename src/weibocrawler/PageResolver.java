package weibocrawler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 * @author daixing
 *	分析页面
 */
public class PageResolver {
	
	public static List<Weibo> resolvePage(Document doc)  //分析主用户微博页面
	{
		ArrayList<Weibo> weibos = new ArrayList<Weibo>();
		Elements weiboElements = doc.select("div.c[id]");
		for(Element weiboElement : weiboElements)
		{
			weibos.add(resolveWeibo(weiboElement));
		}
		return weibos;
	}
	public static Weibo resolveWeibo(Element weiboHtml) //提取主用户首页微博
	{
		System.out.println("_________________________________________");
//		System.out.println(weibo);
		String id = weiboHtml.attr("id");
		Element user = weiboHtml.select("a.nk").first();
		String userName = getNameFromUrl(user.attr("href"));
		String screenName = user.text();
		String content = contentDiscardUrl(weiboHtml.select("span.ctt").first().html());
		Element timeElement = weiboHtml.select("span.ct").first();
		Date time = calculateTime(timeElement.html());
		System.out.println("id: " + id + "\n username: " + userName + "\n "
				+"screenName: " + screenName + "\n content: " + content + "\n time: " + time);
		return new Weibo(id, userName, screenName, content, time);
	}
	
	public static List<Weibo> resolveUserPage(Document doc, String username, String screenname)  //分析用户微博页面
	{
		ArrayList<Weibo> weibos = new ArrayList<Weibo>();
		Elements weiboElements = doc.select("div.c[id]");
		for(Element weiboElement : weiboElements)
		{
			weibos.add(resolveUserWeibo(weiboElement, username, screenname));
		}
		return weibos;
	}
	
	public static Weibo resolveUserWeibo(Element weiboHtml, String username, String screenname) //分析用户微博
	{
		System.out.println("_________________________________________");
		String id = weiboHtml.attr("id");
		String content = contentDiscardUrl(weiboHtml.select("span.ctt").first().html());
		Element timeElement = weiboHtml.select("span.ct").first();
		Date time = calculateTime(timeElement.html());
		System.out.println("id: " + id + "\n username: " + username + "\n "
				+"screenName: " + screenname + "\n content: " + content + "\n time: " + time);
		return new Weibo(id, username, screenname, content, time);
	}
	

	public static String getNameFromUrl(String url)
	{
		Pattern pattern = Pattern.compile("http://weibo.cn/(u/)?(\\w*)");
		Matcher matcher = pattern.matcher(url);
		if(matcher.find())
			return matcher.group(2);
		return null;
	}
	public static Date calculateTime(String timeOrigin)	//从时间字符串分析出具体时间
	{
		Pattern pattern = Pattern.compile("(.*?)&nbsp;");
		Matcher matcher = pattern.matcher(timeOrigin);
		String timeString = null;
		if(matcher.find())
		{
			timeString = matcher.group(1);
			System.out.println(timeString);
			Pattern patternMin = Pattern.compile("(\\d+)分钟前");	 // **分钟前的形式
			Matcher matcherMin = patternMin.matcher(timeString);
			if(matcherMin.find())
			{
				Integer minute = Integer.parseInt(matcherMin.group(1));
				return adjustTime(new Date(), minute);
			}
			Pattern patternToday = Pattern.compile("今天 +(\\d+):(\\d+)"); //今天 **:**
			Matcher matcherToday = patternToday.matcher(timeString);
			if(matcherToday.find())
			{
				Integer hour = Integer.parseInt(matcherToday.group(1));
				Integer minute = Integer.parseInt(matcherToday.group(2));
				return adjustTime(new Date(), hour, minute);
			}
			Pattern patternDate = Pattern.compile("(\\d+)月(\\d+)日 +(\\d+):(\\d+)"); //**月**日 **:**
			Matcher matcherDate = patternDate.matcher(timeString);
			if(matcherDate.find())
			{
				Integer month = Integer.parseInt(matcherDate.group(1));
				Integer day = Integer.parseInt(matcherDate.group(2));
				Integer hour = Integer.parseInt(matcherDate.group(3));
				Integer minute = Integer.parseInt(matcherDate.group(4));
				return adjustTime(new Date(),month, day, hour, minute);
			}
				
		}
		
		return new Date();
	}
	public static Date adjustTime(Date date, Integer minute)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, -minute);
		return calendar.getTime();
	}
	public static Date adjustTime(Date date, Integer hour, Integer minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR, hour);
		calendar.set(Calendar.MINUTE, minute);
		return calendar.getTime();
	}
	public static Date adjustTime(Date date, Integer month, Integer day, Integer hour, Integer minute)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MONTH, month - 1); //貌似一月是0
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR, hour);
		calendar.set(Calendar.MINUTE, minute);
		return calendar.getTime();
	}

	public static String contentDiscardUrl(String content)   //去除内容中@其他人的链接
	{
		Pattern pattern = Pattern.compile("(<a +href.*?>)|(</a>)");
		Matcher matcher = pattern.matcher(content);
		
		return (matcher.replaceAll(""));
	}

	public static String nextPageUrlSuffix(Document doc)  //获得下一页地址的后缀
	{
		Element pageAction = doc.select("form[action] > div > a[href]").first();
		if(!pageAction.text().equals("下页"))
			return null;
		return pageAction.attr("href");
	}
}
