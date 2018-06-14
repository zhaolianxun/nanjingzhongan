package easywin.module.mallmanage.api;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mallManage.api.UserWithdrawManageEntrance")
@RequestMapping(value = "/mm/{mallId}/e/userwithdraw")
public class UserWithdrawManageEntrance {

	public static Logger logger = Logger.getLogger(UserWithdrawManageEntrance.class);

	@RequestMapping(value = "/ent")
	public void ent(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
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

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			sqlParams.add(mallId);
			if (status != null)
				sqlParams.add(status);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select t.alipay_tradeno,t.note,t.id,u.nickname,t.amount,t.status,t.fail_reason,t.apply_time,t.alipay_account,t.alipay_account_name from t_mall_user_withdraw t left join t_mall_user u on t.user_id=u.id where t.mall_id=? "
							+ (status == null ? "" : " and t.status=? ") + " order by t.apply_time desc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("amount", rs.getObject("amount"));
				item.put("note", rs.getObject("note"));
				item.put("status", rs.getObject("status"));
				item.put("failReason", rs.getObject("fail_reason"));
				item.put("applyTime", rs.getObject("apply_time"));
				item.put("alipayAccount", rs.getObject("alipay_account"));
				item.put("alipayAccountName", rs.getObject("alipay_account_name"));
				item.put("alipayTradeno", rs.getObject("alipay_tradeno"));
				item.put("nickname", rs.getObject("nickname"));
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

	@RequestMapping(value = "/detail")
	public void detail(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("id 不能空");
			int id = Integer.parseInt(idParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			List sqlParams = new ArrayList();
			sqlParams.add(mallId);
			sqlParams.add(id);
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select t.alipay_tradeno,t.note,t.id,u.nickname,t.amount,t.status,t.fail_reason,t.apply_time,t.alipay_account,t.alipay_account_name from t_mall_user_withdraw t left join t_mall_user u on t.user_id=u.id where t.mall_id=? and t.id=? ");
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("id", rs.getObject("id"));
				item.put("amount", rs.getObject("amount"));
				item.put("note", rs.getObject("note"));
				item.put("status", rs.getObject("status"));
				item.put("failReason", rs.getObject("fail_reason"));
				item.put("applyTime", rs.getObject("apply_time"));
				item.put("alipayAccount", rs.getObject("alipay_account"));
				item.put("alipayAccountName", rs.getObject("alipay_account_name"));
				item.put("alipayTradeno", rs.getObject("alipay_tradeno"));
				item.put("nickname", rs.getObject("nickname"));
			} else
				throw new InteractRuntimeException(4004, "目标不存在", null);

			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(item);
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

	@RequestMapping(value = "/alternote")
	public void alterNote(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("请选择提现记录");
			int id = Integer.parseInt(idParam);
			String note = StringUtils.trimToNull(request.getParameter("note"));
			if (note == null)
				throw new InteractRuntimeException("备注不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();

			pst = connection.prepareStatement("update t_mall_user_withdraw set status=1,note=? where id=?");
			pst.setObject(1, note);
			pst.setObject(2, id);
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

	@RequestMapping(value = "/success")
	public void success(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("请选择提现记录");
			int id = Integer.parseInt(idParam);
			String alipayTradeno = StringUtils.trimToNull(request.getParameter("alipay_tradeno"));
			String note = StringUtils.trimToNull(request.getParameter("note"));
			if (note == null || alipayTradeno == null)
				throw new InteractRuntimeException("支付宝交易号和备注至少填一项");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			pst = connection.prepareStatement("select status from t_mall_user_withdraw where id=? for update");
			pst.setObject(1, id);
			ResultSet rs = pst.executeQuery();
			int status = 0;
			if (rs.next()) {
				status = rs.getInt("status");
			} else
				throw new InteractRuntimeException(4004, "目标不存在", null);
			pst.close();
			if (status != 0)
				throw new InteractRuntimeException("已处理过");

			pst = connection
					.prepareStatement("update t_mall_user_withdraw set status=1,alipay_tradeno=?,note=? where id=?");
			pst.setObject(1, alipayTradeno);
			pst.setObject(2, note);
			pst.setObject(3, id);
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

	@RequestMapping(value = "/fail")
	public void fail(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String idParam = StringUtils.trimToNull(request.getParameter("id"));
			if (idParam == null)
				throw new InteractRuntimeException("请选择提现记录");
			int id = Integer.parseInt(idParam);
			String reason = StringUtils.trimToNull(request.getParameter("reason"));
			if (reason == null)
				throw new InteractRuntimeException("请输入原因");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			pst = connection
					.prepareStatement("select user_id,amount,status from t_mall_user_withdraw where id=? for update");
			pst.setObject(1, id);
			ResultSet rs = pst.executeQuery();
			String userId = null;
			int amount = 0;
			int status = 0;
			if (rs.next()) {
				userId = rs.getString("user_id");
				amount = rs.getInt("amount");
				status = rs.getInt("status");
			} else
				throw new InteractRuntimeException(4004, "目标不存在", null);
			pst.close();
			if (status != 0)
				throw new InteractRuntimeException("已处理过");

			pst = connection.prepareStatement("select id from t_mall_user where id=? for update");
			pst.setObject(1, userId);
			rs = pst.executeQuery();
			if (rs.next()) {
			} else
				throw new InteractRuntimeException("用户不存在");
			pst.close();

			pst = connection.prepareStatement("update t_mall_user set money=money+? where id=?");
			pst.setObject(1, amount);
			pst.setObject(2, userId);
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			pst = connection.prepareStatement("update t_mall_user_withdraw set status=2,fail_reason=? where id=?");
			pst.setObject(1, reason);
			pst.setObject(2, id);
			n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			pst = connection.prepareStatement(new StringBuilder(
					"insert into t_mall_user_bill (mall_id,user_id,amount,note,happen_time,link,type) values(?,?,?,?,?,?,3)")
							.toString());
			pst.setObject(1, mallId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, amount);
			pst.setObject(4, "提现失败返还");
			pst.setObject(5, new Date().getTime());
			pst.setObject(6, id);
			n = pst.executeUpdate();
			pst.close();
			if (n != 1)
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
}
