package weibocrawler;

import java.util.Calendar;
import java.util.Date;

public class User {
	private String uid;
	private Integer id;
	private String screenname;
	private Date lastTime;
	
	public User(Integer id, String uid, String screenname, Date lastTime)
	{
		this.id = id;
		this.uid = uid;
		this.screenname = screenname;
		this.lastTime = lastTime;
		if(this.lastTime == null)
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_YEAR, -8);
			this.lastTime = calendar.getTime();
		}
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
}
