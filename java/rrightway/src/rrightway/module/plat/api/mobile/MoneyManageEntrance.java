package rrightway.module.plat.api.mobile;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
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
import rrightway.constant.SysConstant;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.mobile.MoneyManageEntrance")
@RequestMapping(value = "/p/m/moneymanageent")
public class MoneyManageEntrance {

	public static Logger logger = Logger.getLogger(MoneyManageEntrance.class);

	@RequestMapping(value = "/ent")
	public void needCheckOrders(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select t.right_wallet,t.money,(select ifnull(sum(money),0) from t_widthdraw where user_id=t.id and status=0) withdrawing_money,t.withdrawable_money from t_user t where t.id=?")
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
			if (alipayTradeno.length() != 28)
				throw new InteractRuntimeException("交易号异常");

			String vcode = StringUtils.trimToNull(request.getParameter("vcode"));
			if (vcode == null)
				throw new InteractRuntimeException("vcode 不可空");

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			UserLoginStatus loginStatus = GetLoginStatus.todo(request, jedis);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String vcodeValue = jedis.get(vcode);
			if (vcodeValue == null || vcodeValue.isEmpty())
				throw new InteractRuntimeException("验证码错误");
			else
				jedis.del(vcode);
			jedis.close();
			connection = RrightwayDataSource.dataSource.getConnection();

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

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(
					new StringBuilder("select t.money,t.withdrawable_money from t_user t where t.id=?").toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal withdrawableMoney;
			BigDecimal unwithdrawWoney;
			if (rs.next()) {
				withdrawableMoney = rs.getBigDecimal("withdrawable_money");
				BigDecimal money = rs.getBigDecimal("money");
				unwithdrawWoney = money.subtract(withdrawableMoney);
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("withdrawableMoney", withdrawableMoney);
			data.put("unwithdrawWoney", unwithdrawWoney);
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

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select id,INSERT(cardno,1,12,'************') cardno,belonger,status from t_user_bankcard where user_id=?")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("bankId", rs.getInt("id"));
				item.put("cardno", rs.getString("cardno"));
				item.put("belonger", rs.getString("belonger"));
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
			String moneyParam = StringUtils.trimToNull(request.getParameter("money"));
			if (moneyParam == null)
				throw new InteractRuntimeException("money 不可空");
			BigDecimal money = new BigDecimal(moneyParam);
			if (money.compareTo(new BigDecimal(50)) == -1)
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

			connection = RrightwayDataSource.dataSource.getConnection();
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
				if (withdrawableMoney.compareTo(money) == -1)
					throw new InteractRuntimeException("可提现金额不足");
				String paypwdMd5 = rs.getString("paypwd_md5");
				if (!paypwdMd5.equals(DigestUtils.md5Hex(paypwd)))
					throw new InteractRuntimeException("支付密码错误");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			String withdrawId = new Date().getTime() + RandomStringUtils.randomNumeric(2);
			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_widthdraw (id,user_id,money,status,apply_time,`to`,to1_bankname,to1_phone,to1_belonger,to1_bankcardno) values(?,?,?,0,?,1,?,?,?,?)")
							.toString());
			pst.setObject(1, withdrawId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, money);
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
					"update t_user set money=money-?,withdrawable_money=withdrawable_money-? where id=? and (money-?)>=0 and (withdrawable_money-?)>=0")
							.toString());
			pst.setObject(1, money);
			pst.setObject(2, money);
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, money);
			pst.setObject(5, money);
			cnt = pst.executeUpdate();
			pst.close();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");

			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_bill (id,user_id,money,note,happen_time,link,type) values(?,?,?,?,?,?,1)")
							.toString());
			pst.setObject(1, new Date().getTime() + RandomStringUtils.randomNumeric(2));
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, money.negate());
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

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select (select ifnull(sum(money),0) from t_bill where user_id=? and money>0) in_money, (select ifnull(sum(money),0) from t_bill where user_id=? and money<0) out_money")
							.toString());
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, loginStatus.getUserId());
			ResultSet rs = pst.executeQuery();
			BigDecimal inMoney;
			BigDecimal outMoney;
			if (rs.next()) {
				inMoney = rs.getBigDecimal("in_money");
				outMoney = rs.getBigDecimal("out_money");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.money,t.note,t.happen_time,t.id,t.type,t.link from t_bill t where t.user_id=?")
							.append(" order by t.happen_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("money", rs.getObject("money"));
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
			data.put("inMoney", inMoney);
			data.put("outMoney", outMoney);
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

			connection = RrightwayDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.alipay_tradeno,t.id,t.money,t.status,t.topup_time,t.fail_reason from t_topup t where t.user_id=?")
							.append(" order by t.topup_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("topupId", rs.getObject("id"));
				item.put("money", rs.getObject("money"));
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

			connection = RrightwayDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.money,t.status,t.apply_time,t.process_time,t.fail_reason,t.to,t.to1_bankcardno from t_widthdraw t where t.user_id=?")
							.append(" order by t.apply_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("withdrawId", rs.getObject("id"));
				item.put("money", rs.getObject("money"));
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

			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection
					.prepareStatement(new StringBuilder("select t.right_wallet from t_user t where t.id=?").toString());
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
}