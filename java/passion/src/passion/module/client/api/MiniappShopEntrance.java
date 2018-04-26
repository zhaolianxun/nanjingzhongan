package passion.module.client.api;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import passion.constant.SysConstant;
import passion.dao.mapper.ShopappMapper;
import passion.entity.InteractRuntimeException;
import passion.entity.PagingPage;
import passion.module.client.business.GetLoginStatus;
import passion.module.client.dao.mapper.AppShopEntranceMapper;
import passion.module.client.dao.mapper.MiniappTemplateManageMapper;
import passion.module.client.dao.mapper.MyMiniappEntranceMapper;
import passion.module.client.dao.mapper.UserManageMapper;
import passion.module.client.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;

@Controller("controller.client.MiniappShop")
@RequestMapping(value = "/c/miniappshop")
public class MiniappShopEntrance extends BaseController {

	public static Logger logger = Logger.getLogger(MiniappShopEntrance.class);
	@Autowired
	protected MiniappTemplateManageMapper miniappTemplateManagementMapper;
	@Autowired
	protected AppShopEntranceMapper appShopModuleMapper;
	@Autowired
	protected MyMiniappEntranceMapper myMiniappMapper;
	@Autowired
	protected UserManageMapper userManageMapper;
	@Autowired
	protected ShopappMapper shopappMapper;

	@RequestMapping(value = "/apps", method = RequestMethod.POST)
	public void apps(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取并处理参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 15;

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			Long totalItemCount = appShopModuleMapper.appsCount();
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> apps = appShopModuleMapper.apps(loginStatus.getUserId(), pagingPage);

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject appsJson = new JSONObject();
			appsJson.put("items", apps);
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

	@RequestMapping(value = "/previewapp", method = RequestMethod.GET)
	public void previewApp(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// 获取请求参数
			String miniappId = StringUtils.trimToNull(request.getParameter("miniapp_id"));
			if (miniappId == null)
				throw new InteractRuntimeException("miniappId 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			String accessToken = myMiniappMapper.getAccessTokenById(miniappId);

			String url = new StringBuilder("https://api.weixin.qq.com/wxa/getwxacode?").append("access_token=")
					.append(accessToken).toString();

			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();

			// 返回结果
			OutputStream out = response.getOutputStream();
			IOUtils.copy(okHttpResponse.body().byteStream(), out);
			out.flush();
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}

	}

}
