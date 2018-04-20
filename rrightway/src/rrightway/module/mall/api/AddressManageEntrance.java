package rrightway.module.mall.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

@Controller("mall.api.AddressManageEntrance")
@RequestMapping(value = "/m/e/addrmanage")
public class AddressManageEntrance {

	public static Logger logger = Logger.getLogger(AddressManageEntrance.class);

	@RequestMapping(value = "")
	public void goodDetailEntrance(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
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

			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select id,receiver_name,phone,full_address,default_is,province_name,city_name,district_name from t_mall_address where user_id=? and mall_id=? order by default_is desc,id asc limit ?,?");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, mallId);
			pst.setObject(3, pageSize * (pageNo - 1));
			pst.setObject(4, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray addresses = new JSONArray();
			while (rs.next()) {
				JSONObject address = new JSONObject();
				address.put("addressId", rs.getObject("id"));
				address.put("receiverName", rs.getObject("receiver_name"));
				address.put("phone", rs.getObject("phone"));
				address.put("fullAddress", rs.getObject("full_address"));
				address.put("defaultIs", rs.getObject("default_is"));
				address.put("provinceName", rs.getObject("province_name"));
				address.put("cityName", rs.getObject("city_name"));
				address.put("districtName", rs.getObject("district_name"));
				addresses.add(address);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("addresses", addresses);
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

	@RequestMapping(value = "/removeaddress")
	public void removeAddress(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String addressId = request.getParameter("address_id");
			if (addressId == null || addressId.trim() == "")
				throw new InteractRuntimeException("address_id不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			// 从连接池获取数据库链接
			connection = RrightwayDataSource.dataSource.getConnection();
			// 删除数据库记录
			pst = connection.prepareStatement("delete from t_mall_address  where id=?");
			pst.setObject(1, addressId);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("地址不存在");

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

	@RequestMapping(value = "/addaddress")
	public void addaddress(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
				throw new InteractRuntimeException("mall_id不可空");
			String receiverName = StringUtils.trimToNull(request.getParameter("receiver_name"));
			if (receiverName == null)
				throw new InteractRuntimeException("receiver_name不可空");
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");
			String fullAddress = StringUtils.trimToNull(request.getParameter("full_address"));
			if (fullAddress == null)
				throw new InteractRuntimeException("full_address不可空");
			String defaultIsParam = StringUtils.trimToNull(request.getParameter("default_is"));
			int defaultIs = defaultIsParam == null ? 0 : Integer.parseInt(defaultIsParam);
			String provinceName = StringUtils.trimToNull(request.getParameter("province_name"));
			if (provinceName == null)
				throw new InteractRuntimeException("province_name不可空");
			String cityName = StringUtils.trimToNull(request.getParameter("city_name"));
			if (cityName == null)
				throw new InteractRuntimeException("city_name不可空");
			String districtName = StringUtils.trimToNull(request.getParameter("district_name"));
			if (districtName == null)
				throw new InteractRuntimeException("district_name不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 消除已默认的
			if (defaultIs == 1) {
				pst = connection.prepareStatement(
						"update t_mall_address set default_is=0 where mall_id=? and user_id=? and default_is=1");
				pst.setObject(1, mallId);
				pst.setObject(2, loginStatus.getUserId());
				pst.executeUpdate();
				pst.close();
			}
			// 插入地址
			pst = connection.prepareStatement(
					"insert t_mall_address (mall_id,user_id,receiver_name,phone,full_address,default_is,province_name,city_name,district_name) values(?,?,?,?,?,?,?,?,?)");
			pst.setObject(1, mallId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, receiverName);
			pst.setObject(4, phone);
			pst.setObject(5, fullAddress);
			pst.setObject(6, defaultIs);
			pst.setObject(7, provinceName);
			pst.setObject(8, cityName);
			pst.setObject(9, districtName);
			int n = pst.executeUpdate();
			if (n == 0)
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

	@RequestMapping(value = "/alteraddress")
	public void alterAddress(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
			String addressId = StringUtils.trimToNull(request.getParameter("address_id"));
			if (addressId == null)
				throw new InteractRuntimeException("address_id不可空");
			String receiverName = StringUtils.trimToNull(request.getParameter("receiver_name"));
			if (receiverName == null)
				throw new InteractRuntimeException("receiver_name不可空");
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			if (phone == null)
				throw new InteractRuntimeException("phone不可空");
			String fullAddress = StringUtils.trimToNull(request.getParameter("full_address"));
			if (fullAddress == null)
				throw new InteractRuntimeException("full_address不可空");
			String defaultIsParam = StringUtils.trimToNull(request.getParameter("default_is"));
			if (defaultIsParam == null)
				throw new InteractRuntimeException("default_is不可空");
			int defaultIs = Integer.parseInt(defaultIsParam);
			String provinceName = StringUtils.trimToNull(request.getParameter("province_name"));
			if (provinceName == null)
				throw new InteractRuntimeException("province_name不可空");
			String cityName = StringUtils.trimToNull(request.getParameter("city_name"));
			if (cityName == null)
				throw new InteractRuntimeException("city_name不可空");
			String districtName = StringUtils.trimToNull(request.getParameter("district_name"));
			if (districtName == null)
				throw new InteractRuntimeException("district_name不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 消除已默认的
			if (defaultIs == 1) {
				pst = connection.prepareStatement(
						"update t_mall_address set default_is=0 where mall_id=? and user_id=? and default_is=1");
				pst.setObject(1, mallId);
				pst.setObject(2, loginStatus.getUserId());
				pst.executeUpdate();
				pst.close();
			}

			// 修改地址
			pst = connection.prepareStatement(
					"update t_mall_address set receiver_name=?,phone=?,full_address=?,default_is=?,province_name=?,city_name=?,district_name=? where id=?");
			pst.setObject(1, receiverName);
			pst.setObject(2, phone);
			pst.setObject(3, fullAddress);
			pst.setObject(4, defaultIs);
			pst.setObject(5, provinceName);
			pst.setObject(6, cityName);
			pst.setObject(7, districtName);
			pst.setObject(8, addressId);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("地址不存在");
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
