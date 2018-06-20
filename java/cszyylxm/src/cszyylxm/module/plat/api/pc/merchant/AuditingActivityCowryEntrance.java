package cszyylxm.module.plat.api.pc.merchant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cszyylxm.entity.InteractRuntimeException;
import cszyylxm.module.plat.business.GetLoginStatus;
import cszyylxm.module.plat.entity.UserLoginStatus;
import cszyylxm.util.HttpRespondWithData;
import cszyylxm.util.CszyylxmDataSource;

@Controller("plat.api.pc.merchant.AuditingActivityCowryEntrance")
@RequestMapping(value = "/p/p/auditcowriesent")
public class AuditingActivityCowryEntrance {

	public static Logger logger = Logger.getLogger(AuditingActivityCowryEntrance.class);

	@RequestMapping(value = "/cowries")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String auditParam = StringUtils.trimToNull(request.getParameter("audit"));
			Integer audit = auditParam == null ? null : Integer.parseInt(auditParam);
			String title = StringUtils.trimToNull(request.getParameter("title"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String buyWayParam = StringUtils.trimToNull(request.getParameter("buy_way"));
			Integer buyWay = buyWayParam == null ? null : Integer.parseInt(buyWayParam);
			String couponIfParam = StringUtils.trimToNull(request.getParameter("coupon_if"));
			Integer couponIf = couponIfParam == null ? null : Integer.parseInt(couponIfParam);
			String publishTimeStartParam = StringUtils.trimToNull(request.getParameter("publish_time_start"));
			Long publishTimeStart = publishTimeStartParam == null ? null : Long.parseLong(publishTimeStartParam);
			String publishTimeEndParam = StringUtils.trimToNull(request.getParameter("publish_time_end"));
			Long publishTimeEnd = publishTimeEndParam == null ? null : Long.parseLong(publishTimeEndParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			if (audit != null)
				sqlParams.add(audit);
			if (title != null)
				sqlParams.add(new StringBuilder("%").append(title).append("%").toString());
			if (giftName != null)
				sqlParams.add(new StringBuilder("%").append(giftName).append("%").toString());
			if (buyWay != null)
				sqlParams.add(buyWay);
			if (couponIf != null)
				sqlParams.add(couponIf);
			if (publishTimeStart != null)
				sqlParams.add(publishTimeStart);
			if (publishTimeEnd != null)
				sqlParams.add(publishTimeEnd);

			pst = connection.prepareStatement(new StringBuilder(
					"select id,gift_name,pay_price,return_money,title,stock,publish_time,audit from t_activity where 1=1 ")
							.append(audit == null ? "" : " and audit=? ")
							.append(title == null ? "" : " and title like ? ")
							.append(giftName == null ? "" : " and gift_name like ? ")
							.append(couponIf == null ? ""
									: couponIf == 1 ? " and (!ISNULL(coupon_url) and LENGTH(trim(coupon_url))>1) "
											: " and (ISNULL(coupon_url) or LENGTH(trim(coupon_url))=0) ")
							.append(publishTimeStart == null ? "" : " and publish_time >= ? ")
							.append(publishTimeEnd == null ? "" : " and publish_time <= ? ").toString());
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("activityId", rs.getObject("id"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("title", rs.getObject("title"));
				item.put("stock", rs.getObject("stock"));
				item.put("publishTime", rs.getObject("publish_time"));
				item.put("audit", rs.getObject("audit"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject activies = new JSONObject();
			activies.put("items", items);
			data.put("activies", activies);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

}