package rrightway.module.plat.entity;

public class UserLoginStatus {
	private String token;
	private String userId;
	private String phone;
	private String nickname;
	private String realname;
	private long loginTime;
	private int sellerIs;
	private int buyerIs;

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

	public int getSellerIs() {
		return sellerIs;
	}

	public void setSellerIs(int sellerIs) {
		this.sellerIs = sellerIs;
	}

	public int getBuyerIs() {
		return buyerIs;
	}

	public void setBuyerIs(int buyerIs) {
		this.buyerIs = buyerIs;
	}

}
