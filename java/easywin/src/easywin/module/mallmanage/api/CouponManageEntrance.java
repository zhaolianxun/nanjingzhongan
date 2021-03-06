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

@Controller("mallManage.api.CouponManageEntrance")
@RequestMapping(value = "/mm/{mallId}/e/couponm")
public class CouponManageEntrance {

	public static Logger logger = Logger.getLogger(CouponManageEntrance.class);

	@RequestMapping(value = "/coupons")
	public void coupons(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select t.id,t.title,t.desc,t.type1_uptomoney,t.type1_submoney,t.type1_starttime,t.type1_endtime from t_mall_coupon t where t.mall_id=?");
			pst.setObject(1, mallId);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("couponId", rs.getObject("id"));
				item.put("title", rs.getObject("title"));
				item.put("desc", rs.getObject("desc"));
				item.put("type1Uptomoney", rs.getObject("type1_uptomoney"));
				item.put("type1Submoney", rs.getObject("type1_submoney"));
				item.put("type1Starttime", rs.getObject("type1_starttime"));
				item.put("type1Endtime", rs.getObject("type1_endtime"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject coupons = new JSONObject();
			coupons.put("items", items);
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

	@RequestMapping(value = "/del")
	public void del(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String couponIdParam = StringUtils.trimToNull(request.getParameter("coupon_id"));
			if (couponIdParam == null)
				throw new InteractRuntimeException("coupon_id 不能为空");
			int couponId = Integer.parseInt(couponIdParam);
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement("delete from t_mall_coupon where id=?");
			pst.setObject(1, couponId);
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

	@RequestMapping(value = "/alterfulloff")
	public void alterFulloff(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String couponIdParam = StringUtils.trimToNull(request.getParameter("coupon_id"));
			if (couponIdParam == null)
				throw new InteractRuntimeException("coupon_id 不能为空");
			int couponId = Integer.parseInt(couponIdParam);
			String title = StringUtils.trimToNull(request.getParameter("title"));
			String desc = StringUtils.trimToNull(request.getParameter("desc"));
			String uptomoneyParam = StringUtils.trimToNull(request.getParameter("uptomoney"));
			Integer uptomoney = uptomoneyParam == null ? null : Integer.parseInt(uptomoneyParam);
			String submoneyParam = StringUtils.trimToNull(request.getParameter("submoney"));
			Integer submoney = submoneyParam == null ? null : Integer.parseInt(submoneyParam);
			String starttimeParam = StringUtils.trimToNull(request.getParameter("starttime"));
			if (starttimeParam == null)
				throw new InteractRuntimeException("请选择开始时间");
			long starttime = Long.parseLong(starttimeParam);
			String endtimeParam = StringUtils.trimToNull(request.getParameter("endtime"));
			if (endtimeParam == null)
				throw new InteractRuntimeException("请选择结束时间");
			long endtime = Long.parseLong(endtimeParam);

			if (endtime < starttime)
				throw new InteractRuntimeException("结束时间不可小于开始时间");
			if (starttime <= new Date().getTime())
				throw new InteractRuntimeException("开始时间不可小于现在");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			List sqlParams = new ArrayList();
			if (desc != null)
				sqlParams.add(desc);
			if (uptomoney != null)
				sqlParams.add(uptomoney);
			if (submoney != null)
				sqlParams.add(submoney);
			sqlParams.add(starttime);
			sqlParams.add(endtime);
			if (title != null)
				sqlParams.add(title);
			sqlParams.add(couponId);
			String sql = new StringBuilder("update t_mall_coupon set id=id").append(desc == null ? "" : ",`desc`=?")
					.append(uptomoney == null ? "" : ",type1_uptomoney=?")
					.append(submoney == null ? "" : ",type1_submoney=?").append(",type1_starttime=?")
					.append(",type1_endtime=?").append(title == null ? "" : ",title=?").append(" where id=?")
					.toString();
			logger.debug(sql);
			logger.debug(sqlParams);
			pst = connection.prepareStatement(sql);
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			int n = pst.executeUpdate();
			logger.debug(new StringBuilder("effect : ").append(n).toString());
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

	@RequestMapping(value = "/addfulloff")
	public void addfulloff(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String title = StringUtils.trimToNull(request.getParameter("title"));
			if (title == null)
				throw new InteractRuntimeException("title 不能为空");
			String desc = StringUtils.trimToNull(request.getParameter("desc"));
			if (desc == null)
				throw new InteractRuntimeException("desc 不能为空");
			String uptomoneyParam = StringUtils.trimToNull(request.getParameter("uptomoney"));
			if (uptomoneyParam == null)
				throw new InteractRuntimeException("uptomoney 不能为空");
			int uptomoney = Integer.parseInt(uptomoneyParam);
			String submoneyParam = StringUtils.trimToNull(request.getParameter("submoney"));
			if (submoneyParam == null)
				throw new InteractRuntimeException("submoney 不能为空");
			int submoney = Integer.parseInt(submoneyParam);
			if (submoney >= uptomoney)
				throw new InteractRuntimeException("优惠金额只能小于满足金额");
			String starttimeParam = StringUtils.trimToNull(request.getParameter("starttime"));
			if (starttimeParam == null)
				throw new InteractRuntimeException("starttime 不能为空");
			long starttime = Long.parseLong(starttimeParam);
			String endtimeParam = StringUtils.trimToNull(request.getParameter("endtime"));
			if (endtimeParam == null)
				throw new InteractRuntimeException("endtime 不能为空");
			long endtime = Long.parseLong(endtimeParam);
			if (endtime < starttime)
				throw new InteractRuntimeException("结束时间不可小于开始时间");
			if (starttime <= new Date().getTime())
				throw new InteractRuntimeException("开始时间不可小于现在");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"INSERT INTO `t_mall_coupon` (`mall_id`, `add_time`, `title`, `desc`, `type`, `type1_uptomoney`, `type1_submoney`, `type1_starttime`, `type1_endtime`) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?);");
			pst.setObject(1, mallId);
			pst.setObject(2, new Date().getTime());
			pst.setObject(3, title);
			pst.setObject(4, desc);
			pst.setObject(5, 1);
			pst.setObject(6, uptomoney);
			pst.setObject(7, submoney);
			pst.setObject(8, starttime);
			pst.setObject(9, endtime);
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

}
