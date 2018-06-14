package easywin.module.mall.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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

@Controller("mall.api.MyLitterCoffer")
@RequestMapping(value = "/m/e/mlc")
public class MyLitterCoffer {

	public static Logger logger = Logger.getLogger(MyLitterCoffer.class);

	@RequestMapping(value = "/ent")
	public void ent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 检查手机号
			pst = connection.prepareStatement(
					"select t.alipay_account,t.alipay_account_name,(select count(id) from t_mall_user_withdraw where user_id=t.id and status=1) withdrew_count,(select ifnull(sum(amount),0) from t_mall_user_withdraw where user_id=t.id and status=1) withdrewMoney,t.money canWithdrawMoney,(select count(id) from t_mall_user_bill where type=1 and user_id=t.id and mall_id=t.mall_id) reward_count,(select ifnull(sum(amount),0) from t_mall_user_bill where type=1 and user_id=t.id and mall_id=t.mall_id) reward_money,t.money,(select count(id) from t_mall_user where register_from_user_id=t.id and mall_id=t.mall_id) my_fans_count from t_mall_user t where t.id=? and t.mall_id=?");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, mallId);
			ResultSet rs = pst.executeQuery();
			int money = 0;
			int myFansCount = 0;
			int rewardMoney = 0;
			int withdrewMoney = 0;
			int rewardCount = 0;
			int withdrewCount = 0;
			String alipayAccount = null;
			String alipayAccountName = null;
			if (rs.next()) {
				money = rs.getInt("money");
				myFansCount = rs.getInt("my_fans_count");
				rewardMoney = rs.getInt("reward_money");
				withdrewMoney = rs.getInt("withdrewMoney");
				rewardCount = rs.getInt("reward_count");
				withdrewCount = rs.getInt("withdrew_count");
				alipayAccount = rs.getString("alipay_account");
				alipayAccountName = rs.getString("alipay_account_name");
			} else
				throw new InteractRuntimeException(20);
			pst.close();

			JSONObject data = new JSONObject();
			data.put("alipayAccount", alipayAccount);
			data.put("alipayAccountName", alipayAccountName);
			data.put("money", money);
			data.put("withdrewCount", withdrewCount);
			data.put("withdrewMoney", withdrewMoney);
			data.put("canWithdrawMoney", money);
			data.put("rewardMoney", rewardMoney);
			data.put("myFansCount", myFansCount);
			data.put("rewardCount", rewardCount);
			// 返回结果
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

	@RequestMapping(value = "/bindalipay")
	public void bindalipay(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
			String alipayAccount = StringUtils.trimToNull(request.getParameter("alipay_account"));
			if (alipayAccount == null)
				throw new InteractRuntimeException("请输入支付宝账号");
			String alipayAccountName = StringUtils.trimToNull(request.getParameter("alipay_account_name"));
			if (alipayAccountName == null)
				throw new InteractRuntimeException("请输入支付宝账号姓名");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 检查手机号
			pst = connection
					.prepareStatement("update t_mall_user set alipay_account=?,alipay_account_name=? where id=?");
			pst.setObject(1, alipayAccount);
			pst.setObject(2, alipayAccountName);
			pst.setObject(3, loginStatus.getUserId());
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
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
		}
	}

	@RequestMapping(value = "/rewardrecord/ent")
	public void rewardrecordEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
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

			connection = EasywinDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(mallId);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select u.headimg,t.amount,t.note,t.happen_time from t_mall_user_bill t left join t_mall_order_detail od on t.link=od.id left join t_mall_order o on od.order_id=o.id left join t_mall_user u on u.id=o.user_id where t.user_id=? and t.mall_id=? and t.type=1")
							.append(" order by t.happen_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("amount", rs.getObject("amount"));
				item.put("headimg", rs.getObject("headimg"));
				item.put("note", rs.getObject("note"));
				item.put("happenTime", rs.getObject("happen_time"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
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
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
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

			connection = EasywinDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(mallId);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.alipay_tradeno,t.note,t.alipay_account,t.alipay_account_name,t.status,t.amount,t.note,t.fail_reason,t.apply_time from t_mall_user_withdraw t where t.user_id=? and t.mall_id=?")
							.append(" order by t.apply_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("status", rs.getObject("status"));
				item.put("amount", rs.getObject("amount"));
				item.put("note", rs.getObject("note"));
				item.put("failReason", rs.getObject("fail_reason"));
				item.put("applyTime", rs.getObject("apply_time"));
				item.put("alipayAccount", rs.getObject("alipay_account"));
				item.put("alipayAccountName", rs.getObject("alipay_account_name"));
				item.put("note", rs.getObject("note"));
				item.put("alipayTradeno", rs.getObject("alipay_tradeno"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
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

	@RequestMapping(value = "/myfans/ent")
	public void myfansEnt(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
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

			connection = EasywinDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(mallId);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.headimg,t.register_time,insert(if(isnull(t.realname),if(isnull(t.phone),t.nickname,t.phone),t.realname),2,3,'***') user_logo from t_mall_user t  where t.register_from_user_id=? and t.mall_id=?")
							.append(" order by t.register_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("headimg", rs.getObject("headimg"));
				item.put("registerTime", rs.getObject("register_time"));
				item.put("userLogo", rs.getObject("user_logo"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
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

	@RequestMapping(value = "/withdraw/apply")
	public void withdrawApply(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
			String amountParam = StringUtils.trimToNull(request.getParameter("amount"));
			if (amountParam == null)
				throw new InteractRuntimeException("amount不可空");
			Integer amount = Integer.parseInt(amountParam);
			if (amount <= 0)
				throw new InteractRuntimeException("金额有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			sqlParams.add(mallId);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.money,t.alipay_account,t.alipay_account_name from t_mall_user t where t.id=? and t.mall_id=? for update")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));

			ResultSet rs = pst.executeQuery();
			int money = 0;
			String alipayAccount = null;
			String alipayAccountName = null;
			if (rs.next()) {
				money = rs.getInt("money");
				alipayAccount = rs.getString("alipay_account");
				alipayAccountName = rs.getString("alipay_account_name");
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			if (money < amount)
				throw new InteractRuntimeException(1000, "余额不足", null);
			if (StringUtils.isEmpty(alipayAccount) || StringUtils.isEmpty(alipayAccountName))
				throw new InteractRuntimeException(1001, "缺少支付宝账号信息", null);
			pst = connection.prepareStatement(
					new StringBuilder("update t_mall_user set money=money-? where id=? and mall_id=? and (money-?)>=0")
							.toString());
			pst.setObject(1, amount);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, mallId);
			pst.setObject(4, amount);
			int cnt = pst.executeUpdate();
			pst.close();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");

			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_mall_user_withdraw (mall_id,user_id,amount,apply_time,alipay_account,alipay_account_name) values(?,?,?,?,?,?)")
							.toString(),
					Statement.RETURN_GENERATED_KEYS);
			pst.setObject(1, mallId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, amount);
			pst.setObject(4, new Date().getTime());
			pst.setObject(5, alipayAccount);
			pst.setObject(6, alipayAccountName);
			cnt = pst.executeUpdate();
			if (cnt != 1)
				throw new InteractRuntimeException("操作失败");
			rs = pst.getGeneratedKeys();
			rs.next();
			int withdrawId = rs.getInt(1);
			pst.close();

			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_mall_user_bill (mall_id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,2)")
							.toString());
			pst.setObject(1, mallId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, -amount);
			pst.setObject(4, "提现");
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, withdrawId);
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
