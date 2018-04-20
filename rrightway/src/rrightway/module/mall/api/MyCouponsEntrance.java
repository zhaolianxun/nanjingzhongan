package rrightway.module.mall.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
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
import rrightway.module.mall.business.GetLoginStatus;
import rrightway.module.mall.entity.UserLoginStatus;
import rrightway.util.RrightwayDataSource;
import rrightway.util.HttpRespondWithData;

@Controller("mall.api.MyCouponsEntrance")
@RequestMapping(value = "/m/e/mc")
public class MyCouponsEntrance {

	public static Logger logger = Logger.getLogger(MyCouponsEntrance.class);

	@RequestMapping(value = "/mycoupons")
	public void goodDetailEntrance(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
			String usedParam = StringUtils.trimToNull(request.getParameter("used"));
			Integer used = usedParam == null ? null : Integer.parseInt(usedParam);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			// 业务处理
			connection = RrightwayDataSource.dataSource.getConnection();
			// 查询优惠券
			List sqlParams = new ArrayList();
			sqlParams.add(mallId);
			sqlParams.add(loginStatus.getUserId());
			if (used != null)
				sqlParams.add(used);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					"select tt.id,t.title,t.desc,t.type,t.type1_uptomoney,t.type1_submoney,t.type1_starttime,t.type1_endtime from t_mall_coupon t right join t_mall_usercoupon tt on t.id=tt.coupon_id where tt.mall_id=? and tt.user_id=? "
							+ (used == null ? "" : " and tt.used=? ") + " order by tt.get_time desc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));
			ResultSet rs = pst.executeQuery();
			JSONArray coupons = new JSONArray();
			while (rs.next()) {
				JSONObject coupon = new JSONObject();
				coupon.put("title", rs.getObject("title"));
				coupon.put("userCouponId", rs.getObject("id"));
				coupon.put("desc", rs.getObject("desc"));
				coupon.put("type", rs.getObject("type"));
				coupon.put("type1Starttime", rs.getObject("type1_starttime"));
				coupon.put("type1Endtime", rs.getObject("type1_endtime"));
				coupon.put("type1Uptomoney", rs.getObject("type1_uptomoney"));
				coupon.put("type1Submoney", rs.getObject("type1_submoney"));
				coupons.add(coupon);
			}
			pst.close();
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("coupons", coupons);
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
