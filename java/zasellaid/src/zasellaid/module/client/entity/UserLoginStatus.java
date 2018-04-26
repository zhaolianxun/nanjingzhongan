package zasellaid.module.client.entity;

public class UserLoginStatus {
	private String token;
	private String userId;
	private String realname;
	private String phone;
	private long loginTime;
	private int ifSuperadmin;
	private int ifAdmin;
	private int maintainIs;

	public String getUserId() {
		return userId;
	}

	public String getPhone() {
		return phone;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public int getIfSuperadmin() {
		return ifSuperadmin;
	}

	public int getIfAdmin() {
		return ifAdmin;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public void setIfSuperadmin(int ifSuperadmin) {
		this.ifSuperadmin = ifSuperadmin;
	}

	public void setIfAdmin(int ifAdmin) {
		this.ifAdmin = ifAdmin;
	}

	public int getMaintainIs() {
		return maintainIs;
	}

	public void setMaintainIs(int maintainIs) {
		this.maintainIs = maintainIs;
	}

}
