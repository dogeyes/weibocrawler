package weibocrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import Log.HtmlFileLog;

public class TestHttpClient {
		static final public String USERNAME = "dexctor@gmail.com";
		static final public String PASSWORD = "dogeyes007";
    public static void main(String[] args) throws ClientProtocolException, IOException {
    	String initUrlString ="http://weibo.cn";
    	String userAgentString = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36";
    	Document docInit = Jsoup.connect(initUrlString).userAgent(userAgentString).get();
    	HtmlFileLog.htmlFileLog(docInit.html(), "init.html");
    	
    	String loginString = docInit.select("div.ut > a[href]").first().attr("href");
    	Document docLogin = Jsoup.connect(loginString).userAgent(userAgentString).get();
    	HtmlFileLog.htmlFileLog(docLogin.html(), "login.html");

    	Element loginForm = docLogin.select("form[action^=?rand]").first();
    	String nextAction = loginForm.attr("action");
    	String loginUrlString = "http://login.weibo.cn/login/";
//    	System.out.println(loginForm);
    	String passName = loginForm.getElementsByAttributeValueStarting("name", "password").attr("name");
    	String passValue = PASSWORD;
    	String userName = "mobile";
    	String userValue = USERNAME;
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
    	
    	List<NameValuePair> formparams = new ArrayList<NameValuePair>();
    	formparams.add(new BasicNameValuePair(passName, passValue));
    	formparams.add(new BasicNameValuePair(userName, userValue));
    	formparams.add(new BasicNameValuePair(backURLName, backURLValue));
    	formparams.add(new BasicNameValuePair(backTitleName, backTitleValue));
    	formparams.add(new BasicNameValuePair(tryCountName, tryCountValue));
    	formparams.add(new BasicNameValuePair(vkName, vkValue));
    	formparams.add(new BasicNameValuePair(rememberName, rememberValue));
    	formparams.add(new BasicNameValuePair(submitName, submitValue));


    	UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
    	

    	
    	HttpClient httpClient = HttpClientBuilder.create().build();
    	HttpPost post = new HttpPost(loginUrlString + nextAction);
    	post.addHeader("User-Agent", userAgentString);
    	
    	post.setEntity(entity);
    	
    	HttpResponse response = httpClient.execute(post);
    	Header[] headers = response.getHeaders("Set-Cookie");
    	Map<String, String> cookies = new HashMap<String, String>();
    	for(Header header: headers)
    	{
    		String[] keyValue = header.getValue().split(";")[0].split("=");
    		cookies.put(keyValue[0], keyValue[1]);
    	}
    	System.out.println("cookies " + cookies);
    	
    	String weiUrl = "http://weibo.cn/";
    	Document docPage = Jsoup.connect(weiUrl).userAgent(userAgentString).cookies(cookies).get();
    	HtmlFileLog.htmlFileLog(docPage.html(), "page1.html");
    	
    	for(int i = 0; i < 60; ++i)
    	{
    		Element pageAction = docPage.select("form[action] > div > a[href]").first();
    		
        	String nextPageString = initUrlString + pageAction.attr("href");
        	docPage = Jsoup.connect(nextPageString).userAgent(userAgentString).cookies(cookies).get();
        	HtmlFileLog.htmlFileLog(docPage.html(), "page" + i + ".html");
        	System.out.println(nextPageString);
        	try{
            	Thread.sleep(5000);
        	}catch(Exception e)
        	{
        		e.printStackTrace();
        	}
    	}
    }
}




