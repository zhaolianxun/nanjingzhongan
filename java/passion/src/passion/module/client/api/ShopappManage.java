package passion.module.client.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import passion.dao.entity.Shopapp;
import passion.dao.mapper.ShopappMapper;
import passion.entity.InteractRuntimeException;
import passion.module.client.business.GetLoginStatus;
import passion.module.client.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;

@Controller("controller.client.ShopappManage")
@RequestMapping(value = "/c/shopappmanage")
public class ShopappManage extends BaseController {

	public static Logger logger = Logger.getLogger(ShopappManage.class);
	@Autowired
	protected ShopappMapper shopappMapper;
	
	@RequestMapping(value = "/alterapp", method = RequestMethod.POST)
	public void alterApp(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String appId = StringUtils.trimToNull(request.getParameter("app_id"));
			if (appId == null)
				throw new InteractRuntimeException("app_id不可空");

			String priceParam = StringUtils.trimToNull(request.getParameter("price"));
			BigDecimal price = priceParam == null ? null : new BigDecimal(priceParam);

			String icon = StringUtils.trim(request.getParameter("icon"));
			if (icon == "")
				throw new InteractRuntimeException("icon不可空");
			
			String remark = StringUtils.trim(request.getParameter("remark"));
			if (remark == "")
				throw new InteractRuntimeException("remark不可空");
			
			String name = StringUtils.trim(request.getParameter("name"));
			if (name == "")
				throw new InteractRuntimeException("name不可空");

			String summary = StringUtils.trim(request.getParameter("summary"));
			if (summary == "")
				throw new InteractRuntimeException("summary不可空");

			String introPic = StringUtils.trim(request.getParameter("intro_pic"));
			if (introPic == "")
				throw new InteractRuntimeException("introPic不可空");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			Shopapp shopapp = shopappMapper.selectByPrimaryKey(appId);
			String srcIcon = shopapp.getIcon();
			String srcIntroPic = shopapp.getIntroPic();

			shopapp.setIcon(icon);
			shopapp.setIntroPic(introPic);
			shopapp.setName(name);
			shopapp.setPrice(price);
			shopapp.setSummary(summary);
			shopapp.setRemark(remark);
			shopappMapper.updateByPrimaryKeySelective(shopapp);

			List<String> delOsses = new ArrayList<String>();
			if (StringUtils.isNotEmpty(icon) && StringUtils.isNotEmpty(srcIcon)) {
				delOsses.add(srcIcon);
			}
			if (StringUtils.isNotEmpty(introPic) && StringUtils.isNotEmpty(srcIntroPic)) {
				delOsses.add(srcIntroPic);
			}
			if (!delOsses.isEmpty())
				ossMapper.delOss(delOsses);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
		}
	}
}
