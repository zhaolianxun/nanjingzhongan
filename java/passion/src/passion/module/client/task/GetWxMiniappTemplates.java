package passion.module.client.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import passion.constant.SysConstant;
import passion.dao.entity.Shopapp;
import passion.dao.entity.WxMiniappTemplate;
import passion.dao.mapper.ShopappMapper;
import passion.dao.mapper.WxMiniappTemplateMapper;
import passion.entity.InteractException;
import passion.module.client.dao.mapper.AppShopEntranceMapper;
import passion.module.client.dao.mapper.MiniappTemplateManageMapper;
import redis.clients.jedis.Jedis;

@Component
public class GetWxMiniappTemplates {

	private static Logger logger = Logger.getLogger(GetWxMiniappTemplates.class);
	@Autowired
	protected WxMiniappTemplateMapper wxMiniappTemplateMapper;

	@Autowired
	protected AppShopEntranceMapper appShopMapper;
	@Autowired
	protected ShopappMapper shopappMapper;
	@Autowired
	protected MiniappTemplateManageMapper miniappTemplateManageMapper;

	@Scheduled(cron = "0 0/5 * * * ?")
	public void run() {
		logger.debug("执行 task.GetWxMiniappTemplates 定时任务");
		Jedis jedis = null;
		try {
			jedis = SysConstant.jedisPool.getResource();
			String componentAccessToken = jedis.get(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken");
			jedis.close();
			jedis = null;

			logger.debug("componentAccessToken " + componentAccessToken);
			if (StringUtils.isEmpty(componentAccessToken))
				return;

			String url = new StringBuilder("https://api.weixin.qq.com/wxa/gettemplatelist?").append("access_token=")
					.append(componentAccessToken).toString();
			logger.debug("url  " + url);

			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody  " + responseBody);

			JSONObject resultVo = JSON.parseObject(responseBody);

			int errcode = resultVo.getIntValue("errcode");
			if (errcode != 0)
				throw new InteractException(resultVo.getString("errmsg"));

			JSONArray templateList = resultVo.getJSONArray("template_list");
			ArrayList<String> shopappIds = new ArrayList<String>();
			for (int i = 0; i < templateList.size(); i++) {
				JSONObject template = templateList.getJSONObject(i);
				int templateId = template.getIntValue("template_id");
				String userDesc = template.getString("user_desc");
				String userVersion = template.getString("user_version");
				Long createTime = template.getLong("create_time") * 1000l;

				String wxAppId = userDesc.split(",")[0];
				String shopAppId = userDesc.split(",")[1];
				if (!shopappIds.contains(shopAppId))
					shopappIds.add(shopAppId);
				WxMiniappTemplate wxMiniappTemplate = miniappTemplateManageMapper
						.getByWxTemplateIdAndWxAppid(templateId, wxAppId);
				if (wxMiniappTemplate == null) {
					wxMiniappTemplate = new WxMiniappTemplate();
					wxMiniappTemplate.setId(RandomStringUtils.randomNumeric(12));
					wxMiniappTemplate.setCreateTime(createTime);
					wxMiniappTemplate.setUserDesc(userDesc);
					wxMiniappTemplate.setUserVersion(userVersion);
					wxMiniappTemplate.setWxTemplateId(templateId);
					wxMiniappTemplate.setUserDesc(userDesc);
					wxMiniappTemplate.setWxAppid(wxAppId);
					wxMiniappTemplate.setShopappId(shopAppId);
					wxMiniappTemplateMapper.insertSelective(wxMiniappTemplate);
				} else {
					boolean ifUpdate = false;
					if (!wxMiniappTemplate.getCreateTime().equals(createTime)) {
						wxMiniappTemplate.setCreateTime(createTime);
						ifUpdate = true;
					}
					if (!wxMiniappTemplate.getUserDesc().equals(userDesc)) {
						wxMiniappTemplate.setUserDesc(userDesc);
						ifUpdate = true;
					}
					if (!wxMiniappTemplate.getUserVersion().equals(userVersion)) {
						wxMiniappTemplate.setUserVersion(userVersion);
						ifUpdate = true;
					}
					if (ifUpdate)
						wxMiniappTemplateMapper.updateByPrimaryKeySelective(wxMiniappTemplate);
				}
			}
			for (String tmp : shopappIds) {
				String lastestTemplateId = miniappTemplateManageMapper.getLastestTemplateId(tmp);
				Shopapp shopapp = new Shopapp();
				shopapp.setId(tmp);
				shopapp.setLastestTemplateId(lastestTemplateId);
				shopappMapper.updateByPrimaryKeySelective(shopapp);
			}

		} catch (Exception e) {
			logger.info(ExceptionUtils.getStackTrace(e));
		} finally {
			if (jedis != null)
				jedis.close();
			logger.debug("执行结束 task.GetWxMiniappTemplates 定时任务");
		}
	}

}
