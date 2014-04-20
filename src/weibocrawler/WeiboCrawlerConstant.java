package weibocrawler;

public class WeiboCrawlerConstant {
	
	public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36";
	public static final String INDEX_PAGE = "http://weibo.cn"; // 手机微博首页
	public static final String USER_TABLE = "user";
	public static final String WEIBO_TABLE = "weibo_1";
	public static final String NEWUSER_TABLE = "newuser";
	public static final String OLDUSER_TABLE = "olduser";
	public static final String USER_RELATIONSHIP_TABLE = "user_relationship";
	public static final String MYSQLURL = "jdbc:mysql://127.0.0.1:3306/weibo?useUnicode=true&characterEncoding=utf8";
	public static final String MYSQL_USERNAME = "root";
	public static final String MYSQL_PASSWORD = "";
	public static final Integer WAIT_TIME = 5000; // ms

}
