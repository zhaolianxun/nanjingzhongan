package cszyylxm.module.plat.api.mobile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import redis.clients.jedis.Jedis;
import cszyylxm.constant.SysConstant;
import cszyylxm.constant.SysParam;
import cszyylxm.entity.InteractRuntimeException;
import cszyylxm.module.plat.business.Businesses;
import cszyylxm.module.plat.business.GetLoginStatus;
import cszyylxm.module.plat.entity.UserLoginStatus;
import cszyylxm.util.HttpRespondWithData;
import cszyylxm.util.CszyylxmDataSource;

@Controller("plat.api.mobile.MoneyManageEntrance")
@RequestMapping(value = "/p/m/moneymanageent")
public class MoneyManageEntrance {

	public static Logger logger = Logger.getLogger(MoneyManageEntrance.class);

	@RequestMapping(value = "/ent")
	public void ent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select (t.right_wallet_unoutable+t.right_wallet_outable) right_wallet,(t.withdrawable_money+t.unwithdraw_money+t.frozen_money) money,(select ifnull(sum(amount),0) from t_widthdraw where user_id=t.id and status=0) withdrawing_money,t.withdrawable_money from t_user t where t.id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal money;
			BigDecimal rightWallet;
			BigDecimal withdrawingMoney;
			BigDecimal withdrawableMoney;
			if (rs.next()) {
				withdrawableMoney = rs.getBigDecimal("withdrawable_money");
				money = rs.getBigDecimal("money");
				rightWallet = rs.getBigDecimal("right_wallet");
				withdrawingMoney = rs.getBigDecimal("withdrawing_money");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("withdrawableMoney", withdrawableMoney);
			data.put("money", money);
			data.put("withdrawingMoney", withdrawingMoney);
			data.put("rightWallet", rightWallet);
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

	@RequestMapping(value = "/topup/ent")
	public void topupEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数
			JSONObject data = new JSONObject();
			data.put("alipay_receipt_qrcode", SysParam.alipay_receipt_qrcode);
			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/topup/confirm")
	public void topupconfirm(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		Jedis jedis = null;
		try {
			// 获取请求参数
			String alipayTradeno = StringUtils.trimToNull(request.getParameter("alipay_tradeno"));
			if (alipayTradeno == null)
				throw new InteractRuntimeException("alipay_tradeno 不可空");
			String vcode = StringUtils.trimToNull(request.getParameter("vcode"));
			if (vcode == null)
				throw new InteractRuntimeException("vcode 不可空");

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus loginStatus = GetLoginStatus.todo(request, jedis);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getStatus() == 1)
				throw new InteractRuntimeException("您的账户已被冻结，请联系客服。");
			String vcodeValue = jedis.get(vcode.toUpperCase());
			if (vcodeValue == null || vcodeValue.isEmpty())
				throw new InteractRuntimeException("验证码错误");
			else
				jedis.del(vcode);
			jedis.close();
			jedis = null;

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_topup (id,user_id,status,topup_time,alipay_tradeno) values(?,?,1,?,?)").toString());
			pst.setObject(1, new Date().getTime() + RandomStringUtils.randomNumeric(2));
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, new Date().getTime());
			pst.setObject(4, alipayTradeno);
			int cnt = pst.executeUpdate();
			pst.close();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");

			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
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
			if (jedis != null)
				jedis.close();
		}
	}

	@RequestMapping(value = "/withdraw/ent")
	public void withdrawEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(
					new StringBuilder("select t.unwithdraw_money,t.withdrawable_money from t_user t where t.id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal withdrawableMoney;
			BigDecimal unwithdrawMoney;
			if (rs.next()) {
				withdrawableMoney = rs.getBigDecimal("withdrawable_money");
				unwithdrawMoney = rs.getBigDecimal("unwithdraw_money");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("withdrawableMoney", withdrawableMoney);
			data.put("unwithdrawMoney", unwithdrawMoney);
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

	@RequestMapping(value = "/withdraw/banks")
	public void withdrawBanks(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select fail_reason,id,INSERT(cardno,1,12,'************') cardno,belonger,status from t_user_bankcard where user_id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("bankId", rs.getInt("id"));
				item.put("cardno", rs.getString("cardno"));
				item.put("belonger", rs.getString("belonger"));
				item.put("failReason", rs.getString("fail_reason"));
				item.put("status", rs.getInt("status"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject banks = new JSONObject();
			banks.put("items", items);
			data.put("banks", banks);
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

	@RequestMapping(value = "/withdraw/applytobank")
	public void withdrawApply(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String amountParam = StringUtils.trimToNull(request.getParameter("amount"));
			if (amountParam == null)
				throw new InteractRuntimeException("amount 不可空");
			BigDecimal amount = new BigDecimal(amountParam);
			amount = amount.setScale(2, RoundingMode.DOWN);
			if (amount.compareTo(new BigDecimal(50)) == -1)
				throw new InteractRuntimeException("最低50才能提现");
			String bankIdParam = StringUtils.trimToNull(request.getParameter("bank_id"));
			if (bankIdParam == null)
				throw new InteractRuntimeException("bank_id 不可空");
			int bankId = Integer.parseInt(bankIdParam);
			String paypwd = StringUtils.trimToNull(request.getParameter("paypwd"));
			if (paypwd == null)
				throw new InteractRuntimeException("paypwd 不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getStatus() == 1)
				throw new InteractRuntimeException("您的账户已被冻结，请联系客服。");
			connection = CszyylxmDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.bankname,t.cardno,t.phone,t.belonger,t.status from t_user_bankcard t where t.id=? and user_id=?")
							.toString());
			pst.setObject(1, bankId);
			pst.setObject(2, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			String bankname = null;
			String bankcardno = null;
			String bankphone = null;
			String bankbelonger = null;
			if (rs.next()) {
				bankname = rs.getString("bankname");
				bankcardno = rs.getString("cardno");
				bankphone = rs.getString("phone");
				bankbelonger = rs.getString("belonger");
				int status = rs.getInt("status");
				if (status != 2)
					throw new InteractRuntimeException("银行卡还未审核通过");
			} else
				throw new InteractRuntimeException("卡号不存在");

			pst = connection.prepareStatement(
					new StringBuilder("select t.withdrawable_money,t.paypwd_md5 from t_user t where t.id=? for update")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			rs = pst.executeQuery();
			BigDecimal withdrawableMoney;
			if (rs.next()) {
				withdrawableMoney = rs.getBigDecimal("withdrawable_money");
				if (withdrawableMoney.compareTo(amount) == -1)
					throw new InteractRuntimeException("可提现金额不足");
				String paypwdMd5 = rs.getString("paypwd_md5");
				if (!paypwdMd5.equals(DigestUtils.md5Hex(paypwd)))
					throw new InteractRuntimeException("支付密码错误");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			String withdrawId = new Date().getTime() + RandomStringUtils.randomNumeric(2);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_widthdraw (id,user_id,amount,status,apply_time,`to`,to1_bankname,to1_phone,to1_belonger,to1_bankcardno) values(?,?,?,0,?,1,?,?,?,?)")
							.toString());
			pst.setObject(1, withdrawId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, amount);
			pst.setObject(4, new Date().getTime());
			pst.setObject(5, bankname);
			pst.setObject(6, bankphone);
			pst.setObject(7, bankbelonger);
			pst.setObject(8, bankcardno);
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"update t_user set withdrawable_money=withdrawable_money-? where id=? and (withdrawable_money-?)>=0")
							.toString());
			pst.setObject(1, amount);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, amount);
			cnt = pst.executeUpdate();
			pst.close();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");

			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,1)")
							.toString());
			pst.setObject(1, new Date().getTime() + RandomStringUtils.randomNumeric(2));
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, amount.negate());
			pst.setObject(4, "申请提现");
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, withdrawId);
			cnt = pst.executeUpdate();
			pst.close();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
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

	@RequestMapping(value = "/bill/ent")
	public void billEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select (select ifnull(sum(amount),0) from t_bill where user_id=? and amount>0) in_amount, (select ifnull(sum(amount),0) from t_bill where user_id=? and amount<0) out_amount")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal inAmount;
			BigDecimal outAmount;
			if (rs.next()) {
				inAmount = rs.getBigDecimal("in_amount");
				outAmount = rs.getBigDecimal("out_amount");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.amount,t.note,t.happen_time,t.id,t.type,t.link from t_bill t where t.user_id=?")
							.append(" order by t.happen_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("amount", rs.getObject("amount"));
				item.put("note", rs.getObject("note"));
				item.put("happenTime", rs.getObject("happen_time"));
				item.put("billId", rs.getObject("id"));
				item.put("type", rs.getObject("type"));
				item.put("link", rs.getObject("link"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("inAmount", inAmount);
			data.put("outAmount", outAmount);
			JSONObject bills = new JSONObject();
			bills.put("items", items);
			data.put("bills", bills);
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

	@RequestMapping(value = "/topuprecord/ent")
	public void topuprecordEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.alipay_tradeno,t.id,t.amount,t.status,t.topup_time,t.fail_reason from t_topup t where t.user_id=?")
							.append(" order by t.topup_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("topupId", rs.getObject("id"));
				item.put("amount", rs.getObject("amount"));
				item.put("status", rs.getObject("status"));
				item.put("topupTime", rs.getObject("topup_time"));
				item.put("alipayTradeno", rs.getObject("alipay_tradeno"));
				item.put("failReason", rs.getObject("fail_reason"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject topups = new JSONObject();
			topups.put("items", items);
			data.put("topups", topups);
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

	@RequestMapping(value = "/withdrawrecord/ent")
	public void withdrawrecordEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.amount,t.status,t.apply_time,t.process_time,t.fail_reason,t.to,t.to1_bankcardno from t_widthdraw t where t.user_id=?")
							.append(" order by t.apply_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("withdrawId", rs.getObject("id"));
				item.put("amount", rs.getObject("amount"));
				item.put("status", rs.getObject("status"));
				item.put("applyTime", rs.getObject("apply_time"));
				item.put("processTime", rs.getObject("process_time"));
				item.put("failReason", rs.getObject("fail_reason"));
				item.put("to", rs.getObject("to"));
				item.put("to1Bankcardno", rs.getObject("to1_bankcardno"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject withdraws = new JSONObject();
			withdraws.put("items", items);
			data.put("withdraws", withdraws);
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

	@RequestMapping(value = "/rightwallet/ent")
	public void rightwalletEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select (t.right_wallet_unoutable+t.right_wallet_outable) right_wallet from t_user t where t.id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal rightWallet;
			if (rs.next()) {
				rightWallet = rs.getBigDecimal("right_wallet");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("rightWallet", rightWallet);
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

	@RequestMapping(value = "/walletout/ent")
	public void walletoutEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			BigDecimal rightWalletOutable = Businesses.computeWalletOutable(loginStatus.getUserId());
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("rightWalletOutable", rightWalletOutable);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/walletout/out")
	public void walletoutOut(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String amountParam = StringUtils.trimToNull(request.getParameter("amount"));
			if (amountParam == null)
				throw new InteractRuntimeException("amount 不可空");
			BigDecimal amount = new BigDecimal(amountParam);
			amount = amount.setScale(2, RoundingMode.DOWN);
			if (amount.floatValue() <= 0)
				throw new InteractRuntimeException("金额有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					new StringBuilder("select right_wallet_outable from t_user where id=? for update").toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal rightWalletOutable = null;
			if (rs.next()) {
				rightWalletOutable = rs.getBigDecimal("right_wallet_outable");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			if (rightWalletOutable.compareTo(amount) == -1)
				throw new InteractRuntimeException("输入的金额超出范围");

			pst = connection.prepareStatement(new StringBuilder(
					"update t_user set withdrawable_money=withdrawable_money+?,right_wallet_outable=right_wallet_outable-? where id=? and right_wallet_outable-? >=0 ")
							.toString());
			pst.setObject(1, amount);
			pst.setObject(2, amount);
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, amount);
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			String walletBillId = new Date().getTime() + RandomStringUtils.randomNumeric(2);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_wallet_bill (id,user_id,amount,note,happen_time) values(?,?,?,?,?)").toString());
			pst.setObject(1, walletBillId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, amount.negate());
			pst.setObject(4, "转出到余额");
			pst.setObject(5, new Date().getTime());
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			String billId = new Date().getTime() + RandomStringUtils.randomNumeric(2);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,2)")
							.toString());
			pst.setObject(1, billId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, amount);
			pst.setObject(4, "钱包转入");
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, walletBillId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
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

	@RequestMapping(value = "/walletprofit/ent")
	public void walletprofitEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();
			pst = connection.prepareStatement(new StringBuilder(
					"select t.wallet_profit,(select ifnull(sum(amount),0) from t_wallet_profit where user_id=t.id and amount>0) history_wallet_profit from t_user t where id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal walletProfit = null;
			BigDecimal historyWalletProfit = null;
			if (rs.next()) {
				walletProfit = rs.getBigDecimal("wallet_profit");
				historyWalletProfit = rs.getBigDecimal("history_wallet_profit");

			} else
				throw new InteractRuntimeException("用户不存在");

			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"select t.happen_time,t.amount from t_wallet_profit t where t.user_id=? order by t.happen_time desc")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("happenTime", rs.getObject("happen_time"));
				item.put("amount", rs.getObject("amount"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			data.put("walletProfit", walletProfit);
			data.put("historyWalletProfit", historyWalletProfit);
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

	@RequestMapping(value = "/walletprofit/out")
	public void walletprofitOut(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String amountParam = StringUtils.trimToNull(request.getParameter("amount"));
			if (amountParam == null)
				throw new InteractRuntimeException("amount 不可空");
			BigDecimal amount = new BigDecimal(amountParam);
			if (amount.intValue() <= 0)
				throw new InteractRuntimeException("金额有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = CszyylxmDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection.prepareStatement(
					new StringBuilder("select wallet_profit from t_user where id=? for update").toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal walletProfit = null;
			if (rs.next()) {
				walletProfit = rs.getBigDecimal("wallet_profit");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			if (walletProfit.compareTo(amount) == -1)
				throw new InteractRuntimeException("输入的金额超出范围");

			pst = connection.prepareStatement(new StringBuilder(
					"update t_user set withdrawable_money=withdrawable_money+?,wallet_profit=wallet_profit-? where id=? and wallet_profit-? >=0 ")
							.toString());
			pst.setObject(1, amount);
			pst.setObject(2, amount);
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, amount);
			int cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			pst.close();

			pst = connection.prepareStatement(
					new StringBuilder("insert into t_wallet_profit (user_id,amount,happen_time) values(?,?,?)")
							.toString(),
					Statement.RETURN_GENERATED_KEYS);
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, amount.negate());
			pst.setObject(3, new Date().getTime());
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			rs = pst.getGeneratedKeys();
			rs.next();
			int profitId = rs.getInt(1);
			pst.close();

			String billId = new Date().getTime() + RandomStringUtils.randomNumeric(2);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_bill (id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,3)")
							.toString());
			pst.setObject(1, billId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, amount);
			pst.setObject(4, "钱包收益转入");
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, profitId);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
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
}