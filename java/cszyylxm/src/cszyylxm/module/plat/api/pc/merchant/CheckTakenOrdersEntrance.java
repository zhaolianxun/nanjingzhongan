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

@Controller("plat.api.pc.merchant.CheckTakenOrdersEntrance")
@RequestMapping(value = "/p/p/chktakenent")
public class CheckTakenOrdersEntrance {

	public static Logger logger = Logger.getLogger(CheckTakenOrdersEntrance.class);

	@RequestMapping(value = "/orders")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String buyerNickname = StringUtils.trimToNull(request.getParameter("buyer_nickname"));
			String sellerNickname = StringUtils.trimToNull(request.getParameter("seller_nickname"));
			String title = StringUtils.trimToNull(request.getParameter("title"));
			String giftName = StringUtils.trimToNull(request.getParameter("gift_name"));
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			String orderTimeStartParam = StringUtils.trimToNull(request.getParameter("order_time_start"));
			Long orderTimeStart = orderTimeStartParam == null ? null : Long.parseLong(orderTimeStartParam);
			String orderTimeEndParam = StringUtils.trimToNull(request.getParameter("order_time_end"));
			Long orderTimeEnd = orderTimeEndParam == null ? null : Long.parseLong(orderTimeEndParam);
			String statusParam = StringUtils.trimToNull(request.getParameter("status"));
			Integer status = statusParam == null ? null : Integer.parseInt(statusParam);
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			if (status != null)
				sqlParams.add(status);
			if (buyerNickname != null)
				sqlParams.add(new StringBuilder("%").append(buyerNickname).append("%").toString());
			if (sellerNickname != null)
				sqlParams.add(new StringBuilder("%").append(sellerNickname).append("%").toString());
			if (title != null)
				sqlParams.add(new StringBuilder("%").append(title).append("%").toString());
			if (giftName != null)
				sqlParams.add(new StringBuilder("%").append(giftName).append("%").toString());
			if (orderId != null)
				sqlParams.add(new StringBuilder("%").append(orderId).append("%").toString());
			if (orderTimeStart != null)
				sqlParams.add(orderTimeStart);
			if (orderTimeEnd != null)
				sqlParams.add(orderTimeEnd);

			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.gift_name,t.pay_price,t.return_money,t.activity_title,t.status,bt.nickname buyer_nickname,st.nickname seller_nickname from t_order t left join t_buyer_taobaoaccount bt on t.buyer_taobaoaccount_id=bt.id left join t_seller_taobaoaccount st on t.seller_taobaoaccount_id=st.id where t.status=0 ")
							.append(status == null ? "" : " and status=? ")
							.append(buyerNickname == null ? "" : " and bt.nickname like ? ")
							.append(sellerNickname == null ? "" : " and st.nickname like ? ")
							.append(title == null ? "" : " and t.title like ? ")
							.append(giftName == null ? "" : " and t.gift_name like ? ")
							.append(orderId == null ? "" : " and t.id like ? ")
							.append(orderTimeStart == null ? "" : " and t.order_time >= ? ")
							.append(orderTimeEnd == null ? "" : " and t.order_time <= ? ").toString());
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("orderId", rs.getObject("id"));
				item.put("giftName", rs.getObject("gift_name"));
				item.put("payPrice", rs.getObject("pay_price"));
				item.put("returnMoney", rs.getObject("return_money"));
				item.put("activityTitle", rs.getObject("activity_title"));
				item.put("status", rs.getObject("status"));
				item.put("buyerNickname", rs.getObject("buyer_nickname"));
				item.put("sellerNickname", rs.getObject("seller_nickname"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject orders = new JSONObject();
			orders.put("items", items);
			data.put("orders", orders);
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