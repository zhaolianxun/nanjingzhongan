package passion.module.client.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import passion.dao.entity.AppRenew;
import passion.dao.entity.WxMiniapp;
import passion.dao.entity.WxMiniappTemplate;
import passion.dao.mapper.AppRenewMapper;
import passion.dao.mapper.WxMiniappMapper;
import passion.entity.InteractRuntimeException;
import passion.entity.PagingPage;
import passion.module.client.business.GetLoginStatus;
import passion.module.client.dao.mapper.AppManageEntranceMapper;
import passion.module.client.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;
import redis.clients.jedis.Jedis;

@Controller("controller.client.AppManageEntrance")
@RequestMapping(value = "/c/app")
public class AppManageEntrance extends BaseController {

	public static Logger logger = Logger.getLogger(AppManageEntrance.class);

	@Autowired
	protected AppManageEntranceMapper appManageEntranceMapper;
	@Autowired
	protected WxMiniappMapper wxMiniappMapper;
	@Autowired
	protected AppRenewMapper appRenewMapper;

	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取并处理参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
			String username = StringUtils.trimToNull(request.getParameter("username"));
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfAdmin() != (byte) 1)
				throw new InteractRuntimeException("您不是管理员");

			Long totalItemCount = appManageEntranceMapper.appCount(phone, username);
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> item = appManageEntranceMapper.apps(phone, username, pagingPage);

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject appsJson = new JSONObject();
			appsJson.put("items", item);
			appsJson.put("pageNo", pagingPage.getPageNo());
			appsJson.put("pageSize", pagingPage.getPageSize());
			appsJson.put("totalItemCount", pagingPage.getTotalItemCount());
			appsJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("apps", appsJson);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/addusetime", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void addUseTime(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id不可空");

			String timeParam = StringUtils.trimToNull(request.getParameter("time"));
			if (timeParam == null)
				throw new InteractRuntimeException("time不可空");
			Long time = new Long(timeParam);

			String moneyParam = StringUtils.trimToNull(request.getParameter("money"));
			BigDecimal money = moneyParam == null ? null : new BigDecimal(moneyParam);

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			WxMiniapp miniapp = wxMiniappMapper.selectByPrimaryKey(appId);
			Long useEndTime = miniapp.getUseEndtime() == null ? 0l : miniapp.getUseEndtime();
			useEndTime = useEndTime + time;
			miniapp.setUseEndtime(useEndTime);
			wxMiniappMapper.updateByPrimaryKeySelective(miniapp);

			AppRenew appRenew = new AppRenew();
			appRenew.setMoney(money);
			appRenew.setTime(useEndTime);
			appRenew.setAppId(appId);
			appRenew.setBuyerUserid(miniapp.getUserId());
			appRenewMapper.insertSelective(appRenew);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
			throw e;
		} finally {
			// 释放资源
		}
	}
}
