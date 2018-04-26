package passion.module.client.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import passion.dao.entity.Shopapp;
import passion.dao.entity.WxMiniappTemplate;
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
import redis.clients.jedis.Jedis;

@Controller("controller.client.MiniappTemplateManage")
@RequestMapping(value = "/c/minitplmanage")
public class MiniappTemplateManage extends BaseController {

	public static Logger logger = Logger.getLogger(MiniappTemplateManage.class);

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

	@RequestMapping(value = "/tpls", method = RequestMethod.POST)
	public void templates(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

			Long totalItemCount = miniappTemplateManagementMapper.templateCount();
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> apps = miniappTemplateManagementMapper.templates(pagingPage);

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject appsJson = new JSONObject();
			appsJson.put("items", apps);
			appsJson.put("pageNo", pagingPage.getPageNo());
			appsJson.put("pageSize", pagingPage.getPageSize());
			appsJson.put("totalItemCount", pagingPage.getTotalItemCount());
			appsJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("templates", appsJson);

			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}

	@RequestMapping(value = "/altertpl", method = RequestMethod.POST)
	public void alterTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// TODO 获取请求参数
			String templateId = StringUtils.trimToNull(request.getParameter("template_id"));
			if (templateId == null)
				throw new InteractRuntimeException("template_id不可空");

			String priceParam = StringUtils.trimToNull(request.getParameter("price"));
			BigDecimal price = priceParam == null ? null : new BigDecimal(priceParam);

			String icon = StringUtils.trim(request.getParameter("icon"));
			if (icon == "")
				throw new InteractRuntimeException("icon不可空");

			String name = StringUtils.trim(request.getParameter("name"));
			if (name == "")
				throw new InteractRuntimeException("name不可空");

			String summary = StringUtils.trim(request.getParameter("summary"));
			if (summary == "")
				throw new InteractRuntimeException("summary不可空");

			String introPic = StringUtils.trim(request.getParameter("intro_pic"));
			if (introPic == "")
				throw new InteractRuntimeException("introPic不可空");
			String remark = StringUtils.trim(request.getParameter("remark"));
			
			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			Shopapp template = shopappMapper.selectByPrimaryKey(templateId);
			template.setSummary(summary);
			template.setIntroPic(introPic);
			template.setPrice(price);
			template.setIcon(icon);
			template.setName(name);
			template.setRemark(remark);
			shopappMapper.updateByPrimaryKeySelective(template);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
		}
	}

	public static void main(String[] args) {
		UserLoginStatus loginStatus = new UserLoginStatus();
		loginStatus.setUserId("12323");
		// TODO 返回结果
		JSONObject data = new JSONObject();
		System.out.println(JSON.toJSON(loginStatus).getClass());
		data.putAll((JSONObject) JSON.toJSON(loginStatus));
		System.out.println(data);
	}
}
