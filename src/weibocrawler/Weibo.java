package weibocrawler;

import java.util.Date;

public class Weibo {
	private String id;
	private String userName;
	private String screenName;
	private String content;
	private Date time;
	public Weibo(String id, String userName, String screenName, String content, Date time)
	{
		this.id = id;
		this.userName = userName;
		this.screenName = screenName;
		this.content = content;
		this.time = time;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getScreenName() {
		return screenName;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	} 
}
