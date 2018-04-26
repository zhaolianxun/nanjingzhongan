package passion.module.client.api;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.bind.annotation.ExceptionHandler;

import passion.dao.mapper.ClientUserMapper;
import passion.dao.mapper.OssMapper;
import passion.dao.mapper.SysparamMapper;
import passion.dao.mapper.WxMiniappMapper;
import passion.dao.mapper.WxMiniappTemplateMapper;
import passion.module.client.dao.mapper.AppShopEntranceMapper;
import passion.module.client.dao.mapper.MiniappTemplateManageMapper;
import passion.module.client.dao.mapper.MyMiniappEntranceMapper;
import passion.module.client.dao.mapper.UserManageMapper;

public class BaseController {

	private static Logger logger = Logger.getLogger(BaseController.class);

	@Autowired
	protected DataSourceTransactionManager txManager;
	@Autowired
	protected ClientUserMapper clientUserMapper;
	@Autowired
	protected SysparamMapper sysparamMapper;
	@Autowired
	protected WxMiniappTemplateMapper wxMiniappTemplateMapper;
	@Autowired
	protected WxMiniappMapper wxMiniappMapper;

	@Autowired
	protected OssMapper ossMapper;

	@ExceptionHandler
	public void exceptionHandler(HttpServletRequest request, Exception ex) {

	}
}
