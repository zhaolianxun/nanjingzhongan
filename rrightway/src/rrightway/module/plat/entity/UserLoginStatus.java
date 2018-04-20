package rrightway.module.plat.entity;

public class UserLoginStatus {
	private String token;
	private String userId;
	private String phone;
	private String nickname;
	private String realname;
	private long loginTime;
	private int superadminIs;
	private int adminIs;
	private int agentIs;

	public int getAgentIs() {
		return agentIs;
	}

	public void setAgentIs(int agentIs) {
		this.agentIs = agentIs;
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

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public int getSuperadminIs() {
		return superadminIs;
	}

	public void setSuperadminIs(int superadminIs) {
		this.superadminIs = superadminIs;
	}

	public int getAdminIs() {
		return adminIs;
	}

	public void setAdminIs(int adminIs) {
		this.adminIs = adminIs;
	}

}
