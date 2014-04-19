package weibocrawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import static weibocrawler.WeiboCrawlerConstant.*;

public class Loginer {

	public static Map<String, String> getCookies(String username, String password) throws IOException
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
}
