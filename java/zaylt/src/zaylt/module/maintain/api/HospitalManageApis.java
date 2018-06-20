package zaylt.module.maintain.api;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;
import zaylt.business.LoginStatus;
import zaylt.constant.SysConstant;
import zaylt.entity.InteractRuntimeException;
import zaylt.util.HttpRespondWithData;
import zaylt.util.ZayltDataSource;

@Controller("maintain.api.HospitalManage")
@RequestMapping(value = "/mm/hm")
public class HospitalManageApis {

	public static Logger logger = Logger.getLogger(HospitalManageApis.class);

	@RequestMapping(value = "/hospitallist")
	public void hospitallist(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String tel = StringUtils.trimToNull(request.getParameter("tel"));
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			LoginStatus loginStatus = LoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (!loginStatus.getType().equals("3"))
				throw new InteractRuntimeException("您不是开发者");
			connection = ZayltDataSource.dataSource.getConnection();

			List sqlParams = new ArrayList();
			sqlParams.add(loginStatus.getUserId());
			if (name != null)
				sqlParams.add(new StringBuilder("%").append(name).append("%").toString());
			if (tel != null)
				sqlParams.add(new StringBuilder("%").append(tel).append("%").toString());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.name,t.tel,u.phone user_phone,t.id,t.headman_name from t_hospital t where 1=1 ")
							.append(name == null ? "" : " and t.name like ?")
							.append(tel == null ? "" : " and t.tel like ?")
							.append(" order by t.add_time desc limit ?,? ").toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("name", rs.getObject("name"));
				item.put("tel", rs.getObject("tel"));
				item.put("userPhone", rs.getObject("user_phone"));
				item.put("headmanName", rs.getObject("headman_name"));
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

}