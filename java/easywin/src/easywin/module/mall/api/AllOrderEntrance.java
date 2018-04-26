package easywin.module.mall.api;

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

import easywin.entity.InteractRuntimeException;
import easywin.module.mall.business.GetLoginStatus;
import easywin.module.mall.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mall.api.AllOrderEntrance")
@RequestMapping(value = "/m/e/allorder")
public class AllOrderEntrance {

	public static Logger logger = Logger.getLogger(AllOrderEntrance.class);

	@RequestMapping(value = "/orderspage")
	public void orders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
				throw new InteractRuntimeException("mall_id不可空");
			String statusParam = StringUtils.trimToNull(request.getParameter("status"));
			Integer status = statusParam == null ? null : Integer.parseInt(statusParam);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			String orderStatus = null;
			Integer finished = null;
			if (status == null) {

			} else if (status == 0 || status == 1 || status == 2) {
				orderStatus = status.toString();
			} else if (status == 3) {
				finished = 1;
			}

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(mallId);
			if (orderStatus != null)
				sqlParams.add(orderStatus);
			if (finished != null)
				sqlParams.add(finished);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select id,order_time,status,amount,finished,refund_status from t_mall_order where user_id=? and mall_id=?"
							+ (orderStatus == null ? "" : " and status=? ")
							+ (finished == null ? "" : " and finished=? ") + " order by order_time desc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray orders = new JSONArray();
			while (rs.next()) {
				JSONObject order = new JSONObject();
				order.put("orderId", rs.getObject("id"));
				order.put("orderTime", rs.getObject("order_time"));
				order.put("status", rs.getObject("status"));
				order.put("finished", rs.getObject("finished"));
				order.put("refundStatus", rs.getObject("refund_status"));
				order.put("amount", rs.getObject("amount"));
				orders.add(order);
			}
			pst.close();

			// 查询订单明细列表
			pst = connection.prepareStatement(
					"select cover,price,count,name,attr_names,value_names from t_mall_order_detail where order_id=?");
			for (int i = 0; i < orders.size(); i++) {
				JSONObject order = orders.getJSONObject(i);
				String orderId = order.getString("orderId");
				pst.setObject(1, orderId);
				rs = pst.executeQuery();
				JSONArray orderDetails = new JSONArray();
				while (rs.next()) {
					JSONObject orderDetail = new JSONObject();
					orderDetail.put("cover", rs.getObject("cover"));
					orderDetail.put("price", rs.getObject("price"));
					orderDetail.put("count", rs.getObject("count"));
					orderDetail.put("name", rs.getObject("name"));
					orderDetail.put("attrNames", rs.getObject("attr_names"));
					orderDetail.put("valueNames", rs.getObject("value_names"));
					orderDetails.add(orderDetail);
				}
				rs.close();
				order.put("details", orderDetails);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
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

	@RequestMapping(value = "/cancelorder")
	public void cancelOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("orderId不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("select id from t_mall_order where id=? for update");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("订单号");
			}
			pst.close();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"update t_mall_order set finish_time=?,finished=1,status=4,cancel_time=? where id=? and user_id=? and status='0'");
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, new Date().getTime());
			pst.setObject(3, orderId);
			pst.setObject(4, loginStatus.getUserId());
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("订单已支付，如需取消，请联系卖家退款");
			pst.close();

			connection.commit();
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/sign")
	public void sign(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("orderId不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement("select id from t_mall_order where id=? for update");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next()) {
				pst.close();
				throw new InteractRuntimeException("订单号不存在");
			}
			pst.close();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"update t_mall_order set finish_time=?,finished=1,status=3,receive_time=? where id=? and user_id=? and status='2'");
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, new Date().getTime());
			pst.setObject(3, orderId);
			pst.setObject(4, loginStatus.getUserId());
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("卖家还没发货或已签收");
			pst.close();

			connection.commit();
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/orderinfopage")
	public void orderInfoPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id 不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select order_time,status,amount,receiver_name,receiver_phone,receiver_address,buyer_note from t_mall_order where id=? ");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			JSONObject order = new JSONObject();
			if (rs.next()) {
				order.put("orderId", orderId);
				order.put("orderTime", rs.getObject("order_time"));
				order.put("status", rs.getObject("status"));
				order.put("amount", rs.getObject("amount"));
				order.put("receiverName", rs.getObject("receiver_name"));
				order.put("receiverPhone", rs.getObject("receiver_phone"));
				order.put("receiverAddress", rs.getObject("receiver_address"));
				order.put("buyerNote", rs.getObject("buyer_note"));
				pst.close();

				pst = connection.prepareStatement(
						"select cover,price,count,name,attr_names,value_names from t_mall_order_detail where order_id=?");
				pst.setObject(1, orderId);
				rs = pst.executeQuery();
				JSONArray orderDetails = new JSONArray();
				while (rs.next()) {
					JSONObject orderDetail = new JSONObject();
					orderDetail.put("cover", rs.getObject("cover"));
					orderDetail.put("price", rs.getObject("price"));
					orderDetail.put("count", rs.getObject("count"));
					orderDetail.put("name", rs.getObject("name"));
					orderDetail.put("attrNames", rs.getObject("attr_names"));
					orderDetail.put("valueNames", rs.getObject("value_names"));
					orderDetails.add(orderDetail);
				}
				pst.close();
				order.put("details", orderDetails);
			} else
				throw new InteractRuntimeException("订单不存在");

			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, order);
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

	public static void main(String[] args) {
		List list = new ArrayList();
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("3");
		list.remove("3");
		list.remove("3");
		System.out.println(list.size());
		System.out.println(list.size());

	}
}
