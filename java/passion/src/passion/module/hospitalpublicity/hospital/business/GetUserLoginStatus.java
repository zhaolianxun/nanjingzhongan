package passion.module.hospitalpublicity.hospital.business;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import passion.module.hospitalpublicity.hospital.entity.UserLoginStatus;

public class GetUserLoginStatus {

	public static UserLoginStatus todo(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if (session == null)
			return null;
		UserLoginStatus status = (UserLoginStatus) session.getAttribute("HospitalPublicityHospitalUserLoginStatus");
		return status;
	}
}
