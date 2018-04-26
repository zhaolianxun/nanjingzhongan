package zasellaid.api;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;

import zasellaid.constant.SysConstant;
import zasellaid.dao.mapper.AdminareaMapper;
import zasellaid.entity.InteractRuntimeException;
import zasellaid.util.HttpRespondWithData;

@Controller("zasellaid.AdminareaManage")
@RequestMapping(value = "/adminarea")
public class AdminareaManage {

	public static Logger logger = Logger.getLogger(AdminareaManage.class);

	@RequestMapping(value = "/provinces", method = RequestMethod.POST)
	public void provinces(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数

			// TODO 业务处理
			session = SysConstant.sqlSessionFactory.openSession(true);
			AdminareaMapper adminareaMapper = session.getMapper(AdminareaMapper.class);

			List<Map> items = adminareaMapper.getProvinces();
			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/cities", method = RequestMethod.POST)
	public void cities(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String provinceIdParam = StringUtils.trimToNull(request.getParameter("province_id"));
			if (provinceIdParam == null)
				throw new InteractRuntimeException("province_id can`t be empty.");
			int provinceId = Integer.parseInt(provinceIdParam);

			// TODO 业务处理
			session = SysConstant.sqlSessionFactory.openSession(true);
			AdminareaMapper adminareaMapper = session.getMapper(AdminareaMapper.class);

			List<Map> items = adminareaMapper.getCities(provinceId);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/distincts", method = RequestMethod.POST)
	public void distincts(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String cityIdParam = StringUtils.trimToNull(request.getParameter("city_id"));
			if (cityIdParam == null)
				throw new InteractRuntimeException("city_id can`t be empty.");
			int cityId = Integer.parseInt(cityIdParam);

			// TODO 业务处理
			session = SysConstant.sqlSessionFactory.openSession(true);
			AdminareaMapper adminareaMapper = session.getMapper(AdminareaMapper.class);
			List<Map> items = adminareaMapper.getDistincts(cityId);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("items", items);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}
}
