package easywin.module.mallmanage.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayConfigScatterImpl;
import com.github.wxpay.sdk.WXPayUtil;

import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import easywin.module.mallmanage.business.OrderAction;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.AESUtil;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mallManage.api.OrderManageEntrance")
@RequestMapping(value = "/mm/{mallId}/e/order")
public class OrderManageEntrance {

	public static Logger logger = Logger.getLogger(OrderManageEntrance.class);

	@RequestMapping(value = "/list")
	public void list(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String statusParam = StringUtils.trimToNull(request.getParameter("status"));
			Integer status = statusParam == null ? null : Integer.parseInt(statusParam);
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
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

			} else if (status == 0 || status == 1 || status == 2 || status == 4) {
				orderStatus = status.toString();
			} else if (status == 3) {
				finished = 1;
			}
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			sqlParams.add(mallId);
			if (orderStatus != null)
				sqlParams.add(orderStatus);
			if (finished != null)
				sqlParams.add(finished);
			if (orderId != null)
				sqlParams.add(orderId);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select finished,refund_status,t.id,t.order_time,t.status,t.amount,t.buyer_note,u.phone,u.nickname from t_mall_order t left join t_mall_user u on t.user_id=u.id where t.mall_id=? "
							+ (orderStatus == null ? "" : " and t.status=? ")
							+ (finished == null ? "" : " and t.finished=? ") + (orderId == null ? "" : " and t.id=?")
							+ " order by t.order_time desc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("orderId", rs.getObject("id"));
				item.put("orderTime", rs.getObject("order_time"));
				item.put("status", rs.getObject("status"));
				item.put("finished", rs.getObject("finished"));
				item.put("refundStatus", rs.getObject("refund_status"));
				item.put("amount", rs.getObject("amount"));
				item.put("buyerNote", rs.getObject("buyer_note"));
				item.put("phone", rs.getObject("phone"));
				item.put("nickname", rs.getObject("nickname"));
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

	@RequestMapping(value = "/orderinfo/{orderId}")
	public void orderInfo(@PathVariable("mallId") String mallId, @PathVariable("orderId") String orderId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select t.finished,t.refund_status,t.good_count,t.cancel_time,t.receive_time,t.deliver_time,t.pay_type,t.pay_time,t.order_time,t.status,t.amount,t.receiver_name,t.receiver_phone,t.receiver_address,t.buyer_note,u.phone,u.nickname,u.realname from t_mall_order t left join t_mall_user u on t.user_id=u.id where t.id=? ");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();

			JSONObject order = new JSONObject();
			if (rs.next()) {
				order.put("orderId", orderId);
				order.put("goodCount", rs.getObject("good_count"));
				order.put("finished", rs.getObject("finished"));
				order.put("refundStatus", rs.getObject("refund_status"));
				order.put("cancelTime", rs.getObject("cancel_time"));
				order.put("receiveTime", rs.getObject("receive_time"));
				order.put("deliverTime", rs.getObject("deliver_time"));
				order.put("payType", rs.getObject("pay_type"));
				order.put("payTime", rs.getObject("pay_time"));
				order.put("orderTime", rs.getObject("order_time"));
				order.put("status", rs.getObject("status"));
				order.put("amount", rs.getObject("amount"));
				order.put("receiverName", rs.getObject("receiver_name"));
				order.put("receiverPhone", rs.getObject("receiver_phone"));
				order.put("receiverAddress", rs.getObject("receiver_address"));
				order.put("buyerNote", rs.getObject("buyer_note"));
				order.put("phone", rs.getObject("phone"));
				order.put("nickname", rs.getObject("nickname"));
				order.put("realname", rs.getObject("realname"));
				pst.close();

				pst = connection.prepareStatement(
						"select cover,price,count,name,attr_names,value_names from t_mall_order_detail where order_id=?");
				pst.setObject(1, orderId);
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

	@RequestMapping(value = "/deliver/{orderId}")
	public void deliver(@PathVariable("mallId") String mallId, @PathVariable("orderId") String orderId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

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
				throw new InteractRuntimeException("订单号有误");
			}
			pst.close();

			// 查詢订单列表
			pst = connection
					.prepareStatement("update t_mall_order set status='2',deliver_time=? where id=? and status='1'");
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, orderId);
			int n = pst.executeUpdate();
			pst.close();
			if (n == 0)
				throw new InteractRuntimeException("订单未支付");
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

	@RequestMapping(value = "/cancel/{orderId}")
	public void cancel(@PathVariable("mallId") String mallId, @PathVariable("orderId") String orderId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String reason = StringUtils.trimToNull(request.getParameter("reason"));
			if (reason == null)
				throw new InteractRuntimeException("reason 不可空");

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
				throw new InteractRuntimeException("订单号有误");
			}
			pst.close();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"update t_mall_order set finished=1,finish_time=?,status='4',cancel_time=?,cancel_reason=? where id=? and status='0'");
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, new Date().getTime());
			pst.setObject(3, reason);
			pst.setObject(4, orderId);
			int n = pst.executeUpdate();
			pst.close();
			if (n == 0)
				throw new InteractRuntimeException("订单已支付");
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

	@RequestMapping(value = "/refund/{orderId}")
	public void refund(@PathVariable("mallId") String mallId, @PathVariable("orderId") String orderId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String reason = StringUtils.trimToNull(request.getParameter("reason"));
			if (reason == null)
				throw new InteractRuntimeException("reason 不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					"select t.status,t.refund_status,t.amount,u.wx_appid,u.wx_mchid,u.wx_mchkey,u.wx_mchcertpath from t_mall_order t left join t_app u on t.mall_id=u.id where t.id=? and u.id=? for update");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			String status = null;
			String refundStatus = null;
			String wxMchid = null;
			String wxAppid = null;
			String wxMchkey = null;
			String wxMchcertpath = null;
			Integer amount = 0;
			if (rs.next()) {
				status = rs.getString("status");
				refundStatus = rs.getString("refundStatus");
				wxMchid = rs.getString("wx_mchid");
				wxAppid = rs.getString("wx_appid");
				wxMchkey = rs.getString("wx_mchkey");
				wxMchcertpath = rs.getString("wx_mchcertpath");
				amount = (Integer) rs.getObject("amount");

				if (wxMchid == null || wxMchcertpath == null || wxMchkey == null || wxMchid.isEmpty()
						|| wxMchcertpath.isEmpty() || wxMchkey.isEmpty())
					throw new InteractRuntimeException("商户支付信息未配置或不全");

				if (status.equals("0") || status.equals("4"))
					throw new InteractRuntimeException("订单未支付或已取消");
				if (refundStatus.equals("1"))
					throw new InteractRuntimeException("已经申请过退款，正在处理");
				if (refundStatus.equals("2"))
					throw new InteractRuntimeException("已退款成功");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			// 查詢订单列表
			pst = connection.prepareStatement(
					"update t_mall_order set refund_status='1',refund_reason=? where id=? and status in ('1','2','3') and refund_status='0'");
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, reason);
			pst.setObject(3, orderId);
			int n = pst.executeUpdate();
			pst.close();
			if (n == 0)
				throw new InteractRuntimeException("订单未支付或已取消");

			HashMap<String, String> payData = new HashMap<String, String>();
			payData.put("out_refund_no", orderId);
			payData.put("out_trade_no", orderId);
			payData.put("total_fee", amount.toString());
			payData.put("refund_fee", amount.toString());
			payData.put("refund_desc", reason);
			payData.put("notify_url",
					SysConstant.project_rooturl + "/mm/" + mallId + "/e/order/refund/wxminiappnotify");

			WXPayConfig wXPayConfig = new WXPayConfigScatterImpl(wxAppid, wxMchid, wxMchkey, wxMchcertpath);
			WXPay wxPay = new WXPay(wXPayConfig);
			Map<String, String> r = wxPay.refund(payData);
			System.out.println(r);

			if ("FAIL".equals(r.get("return_code")))
				throw new InteractRuntimeException(r.get("return_msg"));
			if ("FAIL".equals(r.get("result_code")))
				throw new InteractRuntimeException(
						new StringBuilder(r.get("err_code")).append(":").append(r.get("err_code_des")).toString());

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

	@RequestMapping(value = "/refund/wxminiappnotify")
	public void refundWxminiappNotify(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String reqData = IOUtils.toString(request.getInputStream());
			logger.debug("接收到微信退款通知：" + reqData);
			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select wx_appid,wx_mchid,wx_mchkey from t_app where t.id=?");
			pst.setObject(1, mallId);
			ResultSet rs = pst.executeQuery();
			String wxMchid = null;
			String wxAppid = null;
			String wxMchkey = null;
			if (rs.next()) {
				wxMchid = rs.getString("wx_mchid");
				wxAppid = rs.getString("wx_appid");
				wxMchkey = rs.getString("wx_mchkey");
			} else
				throw new InteractRuntimeException("商城不存在");
			pst.close();

			connection.close();

			// 处理微信返回数据
			WXPayConfig wXPayConfig = new WXPayConfigScatterImpl(wxAppid, wxMchid, wxMchkey);
			WXPay wxPay = new WXPay(wXPayConfig);
			Map<String, String> respData = WXPayUtil.xmlToMap(reqData);
			if (!"SUCCESS".equals(respData.get("return_code")))
				throw new InteractRuntimeException(respData.get("return_msg"));
			String appid = respData.get("appid");
			String mchId = respData.get("mch_id");
			String nonceStr = respData.get("nonce_str");
			String reqInfoStr = respData.get("req_info");
			Map<String, String> reqInfo = WXPayUtil.xmlToMap(AESUtil.decryptData(reqInfoStr));
			int settlementRefundFee = Integer.parseInt(respData.get("settlement_refund_fee"));
			String outTradeNo = respData.get("out_trade_no");
			String refundStatus = respData.get("refund_status");
			if ("SUCCESS".equals(refundStatus))
				OrderAction.refundComplete(outTradeNo, settlementRefundFee);
			else if ("CHANGE".equals(refundStatus))
				OrderAction.refundFail(outTradeNo, "退款异常");
			else if ("REFUNDCLOSE".equals(refundStatus))
				OrderAction.refundFail(outTradeNo, "退款关闭");

			// 返回结果
			response.getWriter().write(
					"<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			response.getWriter().write("<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA["
					+ e.getMessage() + "]]></return_msg></xml>");
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}
}
