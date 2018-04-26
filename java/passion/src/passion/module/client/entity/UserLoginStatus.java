package passion.module.client.entity;

public class UserLoginStatus {
	private String token;
	private String userId;
	private String username;
	private String phone;
	private String nickname;
	private long loginTime;
	private byte ifSuperadmin;
	private byte ifAdmin;

	public String getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public String getPhone() {
		return phone;
	}

	public String getNickname() {
		return nickname;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public byte getIfSuperadmin() {
		return ifSuperadmin;
	}

	public byte getIfAdmin() {
		return ifAdmin;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public void setIfSuperadmin(byte ifSuperadmin) {
		this.ifSuperadmin = ifSuperadmin;
	}

	public void setIfAdmin(byte ifAdmin) {
		this.ifAdmin = ifAdmin;
	}

}
