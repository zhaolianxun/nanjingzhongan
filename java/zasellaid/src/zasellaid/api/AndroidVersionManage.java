package zasellaid.api;

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
import zasellaid.dao.mapper.OssMapper;
import zasellaid.dao.mapper.SysparamMapper;
import zasellaid.entity.InteractRuntimeException;
import zasellaid.util.HttpRespondWithData;

@Controller("controller.AndroidVersionManage")
@RequestMapping(value = "/avm")
public class AndroidVersionManage {

	public static Logger logger = Logger.getLogger(AndroidVersionManage.class);

	@RequestMapping(value = "/newestinfo", method = RequestMethod.POST)
	public void newestInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// 获取并处理参数

			// 业务处理
			session = SysConstant.sqlSessionFactory.openSession(true);
			SysparamMapper sysparamMapper = session.getMapper(SysparamMapper.class);

			String androidNewestAppUrl = sysparamMapper.getStringValue("android_newest_app_url");
			String androidNewestAppVersion = sysparamMapper.getStringValue("android_newest_app_version");

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("androidNewestAppUrl", androidNewestAppUrl);
			data.put("androidNewestAppVersion", androidNewestAppVersion);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/alertnewestinfo", method = RequestMethod.POST)
	public void alertNewestInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String androidNewestAppUrl = StringUtils.trimToNull(request.getParameter("android_newest_app_url"));
			if (androidNewestAppUrl == null)
				throw new InteractRuntimeException("android_newest_app_url不可空");

			String androidNewestAppVersion = StringUtils.trim(request.getParameter("android_newest_app_version"));
			if (androidNewestAppVersion == "")
				throw new InteractRuntimeException("android_newest_app_version不可空");

			// TODO 业务处理
			String referer = request.getHeader("Referer");
			if (StringUtils.isEmpty(referer)
					|| !referer.contains("zasellaid.njshangka.com/androidversionmanage.html")) {
				throw new InteractRuntimeException("您没有权限");
			}

			session = SysConstant.sqlSessionFactory.openSession(false);
			SysparamMapper sysparamMapper = session.getMapper(SysparamMapper.class);
			OssMapper ossMapper = session.getMapper(OssMapper.class);

			String oldUrl = sysparamMapper.getStringValue("android_newest_app_url");
			if (oldUrl != null && oldUrl.trim().length() > 0)
				ossMapper.delOssOfArray(oldUrl);

			sysparamMapper.updateValue("android_newest_app_url", androidNewestAppUrl);
			sysparamMapper.updateValue("android_newest_app_version", androidNewestAppVersion);

			session.commit();
			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (session != null)
				session.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

}
