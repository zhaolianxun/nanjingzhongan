package rrightway.module.plat.api.pc.merchant;

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

import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.pc.merchant.InActivityCowryEntrance")
@RequestMapping(value = "/p/p/inactcowriesent")
public class InActivityCowryEntrance {

	public static Logger logger = Logger.getLogger(InActivityCowryEntrance.class);

	@RequestMapping(value = "/cowries")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
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

			connection = RrightwayDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
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
					"select id,gift_name,pay_price,return_money,title,await_participant_num,buyer_num,(start_time+keep_days*24*60*60*1000-rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')) remain_time from t_activity where in_activity=1")
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
				item.put("awaitParticipantNum", rs.getObject("await_participant_num"));
				item.put("buyerNum", rs.getObject("buyer_num"));
				item.put("remainTime", rs.getObject("remain_time"));
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