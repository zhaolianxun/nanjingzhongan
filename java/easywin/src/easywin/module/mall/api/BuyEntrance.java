package easywin.module.mall.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayConfigScatterImpl;

import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import easywin.module.mall.business.GetLoginStatus;
import easywin.module.mall.business.OrderAction;
import easywin.module.mall.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mall.api.BuyEntrance")
@RequestMapping(value = "/m/e/buy")
public class BuyEntrance {

	public static Logger logger = Logger.getLogger(BuyEntrance.class);

	@RequestMapping(value = "/orderconfirmpage")
	public void orderConfirmPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
			String goodsParam = StringUtils.trimToNull(request.getParameter("goods"));
			if (goodsParam == null)
				throw new InteractRuntimeException("goods不可空");
			JSONArray goods = JSON.parseArray(goodsParam);
			if (goods.size() < 1)
				throw new InteractRuntimeException("请选择商品");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查询默认地址
			pst = connection.prepareStatement(
					"select id,receiver_name,phone,full_address,province_name,city_name,district_name from t_mall_address where user_id=? and mall_id=? order by default_is desc,id asc limit 0,1");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, mallId);
			ResultSet rs = pst.executeQuery();
			JSONObject address = null;
			if (rs.next()) {
				address = new JSONObject();
				address.put("addressId", rs.getObject("id"));
				address.put("receiverName", rs.getObject("receiver_name"));
				address.put("phone", rs.getObject("phone"));
				address.put("fullAddress", rs.getObject("full_address"));
				address.put("provinceName", rs.getObject("province_name"));
				address.put("cityName", rs.getObject("city_name"));
				address.put("districtName", rs.getObject("district_name"));
			}
			pst.close();

			// 查询商品信息
			int totalPrice = 0;
			JSONArray gotGoods = new JSONArray();
			for (int i = 0; i < goods.size(); i++) {
				String id = goods.getJSONObject(i).getString("id");
				int cnt = goods.getJSONObject(i).getInteger("cnt");
				int skuId = goods.getJSONObject(i).getInteger("skuId");
				String attrNames = goods.getJSONObject(i).getString("attrNames");
				String valueNames = goods.getJSONObject(i).getString("valueNames");

				pst = connection.prepareStatement(
						"select u.price,t.name,t.cover from t_mall_good_sku u inner join t_mall_good t on t.id=u.good_id where u.good_id=? and u.id=?");
				pst.setObject(1, id);
				pst.setObject(2, skuId);
				rs = pst.executeQuery();
				if (rs.next()) {
					JSONObject gotGood = new JSONObject();
					gotGood.put("id", id);
					gotGood.put("count", cnt);
					gotGood.put("skuId", skuId);
					gotGood.put("attrNames", attrNames);
					gotGood.put("valueNames", valueNames);
					int price = rs.getInt("price");
					gotGood.put("price", price);
					gotGood.put("name", rs.getObject("name"));
					gotGood.put("cover", rs.getObject("cover"));
					gotGoods.add(gotGood);

					totalPrice = totalPrice + price;
				}
				pst.close();
			}

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("address", address);
			data.put("goods", gotGoods);
			data.put("totalPrice", totalPrice);
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

	@RequestMapping(value = "/order")
	public void order(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String buyerNote = StringUtils.trimToNull(request.getParameter("buyer_note"));
			String addressId = StringUtils.trimToNull(request.getParameter("address_id"));
			if (addressId == null)
				throw new InteractRuntimeException("address_id不可空");
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
			String goodsParam = StringUtils.trimToNull(request.getParameter("goods"));
			if (goodsParam == null)
				throw new InteractRuntimeException("goods不可空");
			JSONArray goods = JSON.parseArray(goodsParam);
			if (goods.size() < 1)
				throw new InteractRuntimeException("请选择商品");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查询默认地址
			pst = connection.prepareStatement(
					"select receiver_name,phone,province_name||' '||city_name||' '||district_name||' '||full_address full_address from t_mall_address where id=?");
			pst.setObject(1, addressId);
			ResultSet rs = pst.executeQuery();
			String receiverName = null;
			String receiverPhone = null;
			String receiverFullAddress = null;
			if (rs.next()) {
				receiverName = rs.getString("receiver_name");
				receiverPhone = rs.getString("phone");
				receiverFullAddress = rs.getString("full_address");
			} else
				throw new InteractRuntimeException("地址不存在");
			pst.close();

			String orderId = SysConstant.sdf1.format(new Date()) + RandomStringUtils.randomNumeric(4);

			// 查询商品信息
			int totalPrice = 0;
			int totalCount = 0;
			for (int i = 0; i < goods.size(); i++) {
				String goodId = goods.getJSONObject(i).getString("id");
				int cnt = goods.getJSONObject(i).getInteger("cnt");
				int skuId = goods.getJSONObject(i).getInteger("skuId");
				String attrNames = goods.getJSONObject(i).getString("attrNames");
				String valueNames = goods.getJSONObject(i).getString("valueNames");
				int price = 0;
				String name = null;
				String cover = null;
				pst = connection.prepareStatement(
						"select u.price,t.name,t.cover from t_mall_good_sku u inner join t_mall_good t on t.id=u.good_id where u.good_id=? and u.id=?");
				pst.setObject(1, goodId);
				pst.setObject(2, skuId);
				rs = pst.executeQuery();
				if (rs.next()) {
					price = rs.getInt("price");
					name = rs.getString("name");
					cover = rs.getString("cover");
				} else {
					throw new InteractRuntimeException("商品不存在");
				}
				pst.close();

				totalPrice = totalPrice + price;
				totalCount = totalCount + cnt;

				// 插入订单明细
				pst = connection.prepareStatement(
						"insert into t_mall_order_detail (order_id,good_id,price,count,name,cover,sku_id,attr_names,value_names) values(?,?,?,?,?,?,?,?,?)");
				pst.setObject(1, orderId);
				pst.setObject(2, goodId);
				pst.setObject(3, price);
				pst.setObject(4, cnt);
				pst.setObject(5, name);
				pst.setObject(6, cover);
				pst.setObject(7, skuId);
				pst.setObject(8, attrNames);
				pst.setObject(9, valueNames);
				int n = pst.executeUpdate();
				if (n == 0)
					throw new InteractRuntimeException("生成订单明细失败");
				pst.close();
			}

			// 插入订单
			pst = connection.prepareStatement(
					"insert into t_mall_order (id,mall_id,user_id,amount,good_count,status,order_time,receiver_name,receiver_phone,receiver_address,buyer_note) values(?,?,?,?,?,?,?,?,?,?,?)");
			pst.setObject(1, orderId);
			pst.setObject(2, mallId);
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, totalPrice);
			pst.setObject(5, totalCount);
			pst.setObject(6, 0);
			pst.setObject(7, new Date().getTime());
			pst.setObject(8, receiverName);
			pst.setObject(9, receiverPhone);
			pst.setObject(10, receiverFullAddress);
			if (buyerNote != null)
				pst.setObject(11, buyerNote);
			else
				pst.setNull(11, Types.VARCHAR);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("生成订单失败");
			pst.close();

			connection.commit();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("orderId", orderId);
			HttpRespondWithData.todo(request, response, 0, null, data);
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

	@RequestMapping(value = "/pay/wxminiapp")
	public void payWxminiapp(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String orderId = StringUtils.trimToNull(request.getParameter("order_id"));
			if (orderId == null)
				throw new InteractRuntimeException("order_id不可空");
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id 不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getWxOpenid() == null)
				throw new InteractRuntimeException(27);

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查询默认地址
			pst = connection.prepareStatement(
					"select u.wx_appid,u.wx_mchid,u.wx_mchkey,u.wx_mchcertpath,t.amount,t.status,(select group_concat(name) from t_mall_order_detail where order_id=t.id) detail from t_mall_order t inner join t_app u on t.mall_id=u.id where t.id=? for update");
			pst.setObject(1, orderId);
			ResultSet rs = pst.executeQuery();
			Integer amount = 0;
			String status = null;
			String wxMchid = null;
			String wxAppid = null;
			String wxMchkey = null;
			String wxMchcertpath = null;
			String detail = null;
			if (rs.next()) {
				amount = rs.getInt("amount");
				status = rs.getString("status");
				wxMchid = rs.getString("wx_mchid");
				wxAppid = rs.getString("wx_appid");
				wxMchkey = rs.getString("wx_mchkey");
				detail = rs.getString("detail");
				wxMchcertpath = rs.getString("wx_mchcertpath");
			} else
				throw new InteractRuntimeException("订单不存在");
			pst.close();

			if (wxMchid == null || wxMchcertpath == null || wxMchkey == null || wxMchid.isEmpty()
					|| wxMchcertpath.isEmpty() || wxMchkey.isEmpty())
				throw new InteractRuntimeException("商户支付信息未配置或不全");
			if (amount <= 0)
				throw new InteractRuntimeException("支付金额有误");
			if (!status.equals("0"))
				throw new InteractRuntimeException("订单状态有误");

			// 微信统一下单
			HashMap<String, String> payData = new HashMap<String, String>();
			payData.put("body", "商品购买");
			payData.put("detail", detail);
			payData.put("out_trade_no", orderId);
			payData.put("total_fee", amount.toString());
			payData.put("spbill_create_ip", request.getRemoteAddr());
			payData.put("notify_url", SysConstant.project_rooturl + "/m/e/buy/pay/wxminiappnotify/" + mallId);
			payData.put("trade_type", "JSAPI");
			payData.put("openid", loginStatus.getWxOpenid());

			WXPayConfig wXPayConfig = new WXPayConfigScatterImpl(wxAppid, wxMchid, wxMchkey);
			WXPay wxPay = new WXPay(wXPayConfig);
			Map<String, String> r = wxPay.unifiedOrder(payData);
			System.out.println(r);

			if ("FAIL".equals(r.get("return_code")))
				throw new InteractRuntimeException(r.get("return_msg"));
			if ("FAIL".equals(r.get("result_code")))
				throw new InteractRuntimeException(
						new StringBuilder(r.get("err_code")).append(":").append(r.get("err_code_des")).toString());
			// 生成前端支付信息
			Map frontPayInfo = wxPay.genFrontPayInfo(r.get("prepay_id"));

			connection.commit();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(frontPayInfo);
			HttpRespondWithData.todo(request, response, 0, null, data);
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

	@RequestMapping(value = "/pay/wxminiappnotify/{mallId}")
	public void payWxminiappNotify(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String reqData = IOUtils.toString(request.getInputStream());

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

			WXPayConfig wXPayConfig = new WXPayConfigScatterImpl(wxAppid, wxMchid, wxMchkey);
			WXPay wxPay = new WXPay(wXPayConfig);

			Map<String, String> parsedReqData = wxPay.processResponseXml(reqData);
			if (!"SUCCESS".equals(parsedReqData.get("return_code")))
				throw new InteractRuntimeException(parsedReqData.get("return_msg"));
			if (!"SUCCESS".equals(parsedReqData.get("result_code")))
				throw new InteractRuntimeException(parsedReqData.get("err_code_des"));

			int totalFee = Integer.parseInt(parsedReqData.get("total_fee"));
			String outTradeNo = parsedReqData.get("out_trade_no");

			connection.close();
			OrderAction.payComplete(outTradeNo, null, totalFee, "1");

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
