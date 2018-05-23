package rrightway.module.plat.entity;

public class UserLoginStatus {
	private String token;
	private String userId;
	private String phone;
	private String username;
	private long loginTime;
	private int adminIf;

	public int getAdminIf() {
		return adminIf;
	}

	public void setAdminIf(int adminIf) {
		this.adminIf = adminIf;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

}
