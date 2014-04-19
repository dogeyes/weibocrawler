package weibocrawler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import Log.HtmlFileLog;

public class CrawlerFromPhone { //从手机网页上爬数据
	private String username; //用户名
	private String password; //密码
	private final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36";
	private final String INDEX_PAGE ="http://weibo.cn";  //手机微博首页
	
	public CrawlerFromPhone(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
	//取得登陆的认证cookie，后续请求加入该cookie无需登陆
	public Map<String, String> getCookies() throws IOException
	{
		Document indexPage = Jsoup.connect(INDEX_PAGE).userAgent(USER_AGENT).get();  //获取首页
    	HtmlFileLog.htmlFileLog(indexPage.html(), "indexPage.html");   //输出首页到文件，备份
    	
    	String loginString = indexPage.select("div.ut > a[href]").first().attr("href");  //获取登陆页面地址
    	Document docLogin = Jsoup.connect(loginString).userAgent(USER_AGENT).get();      //获取登陆页面
    	HtmlFileLog.htmlFileLog(docLogin.html(), "login.html");
    	Element loginForm = docLogin.select("form[action^=?rand]").first();              //获取登陆的表
    	String loginAction = loginForm.attr("action");									
    	String loginUrlString = "http://login.weibo.cn/login/";
    	
    	//登陆递交的表的参数
    	String passName = loginForm.getElementsByAttributeValueStarting("name", "password").attr("name"); //用户名对应的表项名是随机的，要提取一下
    	String passValue = password;
    	String userName = "mobile";
    	String userValue = username;
    	String backURLName = "backURL";
    	String backURLValue = loginForm.getElementsByAttributeValue("name", backURLName).attr("value");
    	String backTitleName = "backTitle";
    	String backTitleValue = loginForm.getElementsByAttributeValue("name", backTitleName).attr("value");
    	String tryCountName = "tryCount";
    	String tryCountValue = loginForm.getElementsByAttributeValue("name", tryCountName).attr("value");
    	String vkName = "vk";
    	String vkValue = loginForm.getElementsByAttributeValue("name", vkName).attr("value");
    	String rememberName = "remember";
    	String rememberValue = "on";
    	String submitName = "submit";
    	String submitValue = "登陆";

    	//构造递交的表单form
    	List<NameValuePair> formparams = new ArrayList<NameValuePair>();
    	formparams.add(new BasicNameValuePair(passName, passValue));
    	formparams.add(new BasicNameValuePair(userName, userValue));
    	formparams.add(new BasicNameValuePair(backURLName, backURLValue));
    	formparams.add(new BasicNameValuePair(backTitleName, backTitleValue));
    	formparams.add(new BasicNameValuePair(tryCountName, tryCountValue));
    	formparams.add(new BasicNameValuePair(vkName, vkValue));
    	formparams.add(new BasicNameValuePair(rememberName, rememberValue));
    	formparams.add(new BasicNameValuePair(submitName, submitValue));

    	//对表单进行编码
    	UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
    	
    	//递交登陆
    	HttpClient httpClient = HttpClientBuilder.create().build();
    	HttpPost post = new HttpPost(loginUrlString + loginAction);
    	post.addHeader("User-Agent", USER_AGENT);
    	
    	post.setEntity(entity);
    	
    	HttpResponse response = httpClient.execute(post);  	//递交请求获得response
    	
    	Header[] headers = response.getHeaders("Set-Cookie");	//获得返回的头部，找到Set-Cookie属性
    	Map<String, String> cookies = new HashMap<String, String>();
    	for(Header header: headers)
    	{
    		String[] keyValue = header.getValue().split(";")[0].split("=");  //分离出需要的cookie部分
    		cookies.put(keyValue[0], keyValue[1]);
    	}
    	System.out.println("login cookies " + cookies);
    	
    	return cookies;
	}
	
	public void getPages(Map<String, String> cookies) throws IOException 
	{
    	Document docPage = getPage(cookies, INDEX_PAGE);
    	
    	for(int i = 0; i < 60; ++i)
    	{
        	String nextPageString = INDEX_PAGE + nextPageUrlSuffix(docPage);  //得到下一页地址
        	docPage = getPage(cookies, nextPageString);
        	try{
            	Thread.sleep(5000);
        	}catch(Exception e)
        	{
        		e.printStackTrace();
        	}
    	}
	}
	public Document getPage(Map<String, String> cookies, String url) throws IOException //获得给定url的页面
	{
		System.out.println("fetching ---- " + url);
		Random random = new Random();
    	Document docPage = Jsoup.connect(url).userAgent(USER_AGENT).cookies(cookies).get();
    	HtmlFileLog.htmlFileLog(docPage.html(), "page"+random.nextLong()+".html");  //得到页面
    	return docPage;
	}
	public String nextPageUrlSuffix(Document doc)  //获得下一页地址的后缀
	{
		Element pageAction = doc.select("form[action] > div > a[href]").first();
		if(pageAction.text() != "下一页")
			return null;
		return pageAction.attr("href");
	}
	public static void main(String[] args) throws IOException {
		Scanner in = new Scanner(System.in);
		System.out.println("Enter your username: ");
		String username = in.next();
		System.out.println("Enter your password: ");
		String password = in.next();
		CrawlerFromPhone crawler = new CrawlerFromPhone(username, password);
		crawler.getPages(crawler.getCookies());
	}
}
