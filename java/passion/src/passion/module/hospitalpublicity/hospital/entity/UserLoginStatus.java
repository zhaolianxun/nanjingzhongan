package passion.module.hospitalpublicity.hospital.entity;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class UserLoginStatus {
	private String userId;
	private String username;
	private String phone;
	private String nickname;
	private long loginTime;
	private byte ifSuperadmin;
	private byte ifAdmin;
	private String hospitalId;

	public UserLoginStatus(String userId, String username, String phone, String nickname, byte ifSuperadmin,
			byte ifAdmin, String hospitalId, Date loginTime) {
		super();
		if (StringUtils.isEmpty(userId))
			throw new RuntimeException("managerId can`t be empty.");
		if (loginTime == null)
			throw new RuntimeException("loginTime can`t be empty.");
		if (hospitalId == null)
			throw new RuntimeException("hospitalId can`t be empty.");
		this.ifSuperadmin = ifSuperadmin;
		this.ifAdmin = ifAdmin;
		this.hospitalId = hospitalId;
		this.userId = userId;
		this.username = username;
		this.phone = phone;
		this.nickname = nickname;
		this.loginTime = loginTime.getTime();
	}

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

	public String getHospitalId() {
		return hospitalId;
	}

}
