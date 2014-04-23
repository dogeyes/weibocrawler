package weibocrawler;

import java.util.Calendar;
import java.util.Date;

public class User {
	private String uid;
	private Integer id;
	private String screenname;
	private Date lastTime;
	private Date lastUserTime;
	
	public User(Integer id, String uid, String screenname, Date lastTime)
	{
		this.id = id;
		this.uid = uid;
		this.screenname = screenname;
		this.lastTime = lastTime;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, -8);
		if(this.lastTime == null)
		{
			this.lastTime = calendar.getTime();
		}
		if(this.lastUserTime == null)
			this.lastUserTime = calendar.getTime();
	}
	public User(Integer id, String uid, String screenname, Date lastTime, Date lastUserTime)
	{
		this.id = id;
		this.uid = uid;
		this.screenname = screenname;
		this.lastTime = lastTime;
		this.lastUserTime = lastUserTime;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, -8);
		if(this.lastTime == null)
		{
			this.lastTime = calendar.getTime();
		}
		if(this.lastUserTime == null)
			this.lastUserTime = calendar.getTime();
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getScreenname() {
		return screenname;
	}
	public void setScreenname(String screenname) {
		this.screenname = screenname;
	}
	public Date getLastTime() {
		return lastTime;
	}
	public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}
	@Override
	public boolean equals(Object obj)
	{
		User user;
		if (obj instanceof User) {
			user = (User) obj;
			return uid.equals(user.getUid());
		}
		return false;
	}
	public Date getLastUserTime() {
		return lastUserTime;
	}
	public void setLastUserTime(Date lastUserTime) {
		this.lastUserTime = lastUserTime;
	}
}
