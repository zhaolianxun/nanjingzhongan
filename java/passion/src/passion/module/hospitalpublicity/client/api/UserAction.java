package passion.module.hospitalpublicity.client.api;

import java.util.Date;

import javax.annotation.Resource;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import okhttp3.Request;
import okhttp3.Response;
import passion.constant.SysConstant;
import passion.dao.entity.HpClientUser;
import passion.dao.entity.WxMiniapp;
import passion.dao.mapper.HpClientUserMapper;
import passion.entity.InteractRuntimeException;
import passion.module.client.api.BaseController;
import passion.module.hospitalpublicity.client.dao.mapper.UserModuleMapper;
import passion.module.hospitalpublicity.client.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;
import redis.clients.jedis.Jedis;

@Controller("passion.module.hospitalpublicity.client.UserAction")
@RequestMapping(value = "/hp/c/user")
public class UserAction extends BaseController {

	public static Logger logger = Logger.getLogger(UserAction.class);

	@Resource(name = "passion.module.hospitalpublicity.client.dao.mapper.UserModuleMapper")
	protected UserModuleMapper userModuleMapper;
	@Autowired
	protected HpClientUserMapper hpClientUserMapper;

	@RequestMapping(value = "/loginbywxminiapp")
	@Transactional(rollbackFor = Exception.class)
	public void loginByWxMiniApp(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// TODO 获取请求参数
			String jscode = StringUtils.trimToNull(request.getParameter("jscode"));
			if (jscode == null)
				throw new InteractRuntimeException("jscode不可空");
			String miniappId = StringUtils.trimToNull(request.getParameter("miniapp_id"));
			if (miniappId == null)
				throw new InteractRuntimeException("miniappId不可空");

			WxMiniapp wxMiniapp = wxMiniappMapper.selectByPrimaryKey(miniappId);
			if (wxMiniapp == null)
				throw new InteractRuntimeException("小程序不存在");
			String hospitalId = userModuleMapper.getHospitalIdByWxMiniappId(miniappId);
			if (hospitalId == null)
				throw new InteractRuntimeException("医院未创建");

			jedis = SysConstant.jedisPool.getResource();
			String componentAccessToken = jedis.get(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken");

			String url = new StringBuilder("https://api.weixin.qq.com/sns/component/jscode2session?").append("appid=")
					.append(wxMiniapp.getAppId()).append("&js_code=").append(jscode).append("&grant_type=")
					.append(wxMiniapp.getAccessToken()).append("&component_appid=")
					.append(SysConstant.wechat_open_thirdparty_AppId).append("&component_access_token=")
					.append(componentAccessToken).toString();
			logger.debug("url " + url);

			Request okHttpRequest = new Request.Builder().url(url).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();
			String responseBody = okHttpResponse.body().string();
			logger.debug("responseBody " + responseBody);
			JSONObject resultVo = JSON.parseObject(responseBody);

			Integer errcode = resultVo.getInteger("errcode");
			if (errcode != null && errcode != 0)
				throw new InteractRuntimeException(resultVo.getString("errmsg"));
			String openid = resultVo.getString("openid");
			String sessionKey = resultVo.getString("session_key");

			String userId = userModuleMapper.lockAndGetIdByWxOpenIdAndHospitalId(openid, hospitalId);
			if (userId == null) {
				HpClientUser clientUser = new HpClientUser();
				clientUser.setId(RandomStringUtils.randomNumeric(12));
				clientUser.setWxOpenId(openid);
				clientUser.setWxSessionKey(sessionKey);
				clientUser.setIfAdmin((byte) 0);
				clientUser.setIfSuperadmin((byte) 0);
				clientUser.setIfLoginable((byte) 1);
				clientUser.setHospitalId(hospitalId);
				clientUser.setRegisterTime(new Date().getTime());
				hpClientUserMapper.insertSelective(clientUser);
			} else {
				HpClientUser clientUser = new HpClientUser();
				clientUser.setId(userId);
				clientUser.setWxSessionKey(sessionKey);
				clientUser.setWxOpenId(openid);
				hpClientUserMapper.updateByPrimaryKeySelective(clientUser);
			}
			String token = RandomStringUtils.randomNumeric(12);

			UserLoginStatus loginStatus = new UserLoginStatus();
			loginStatus.setUserId(userId);
			loginStatus.setLoginTime(new Date().getTime());
			loginStatus.setHospitalId(hospitalId);
			String loginRedisKey = new StringBuilder("passion.hospitalpublicity.client.login-").append(token)
					.toString();
			String oldToken = jedis.get(userId);
			if (oldToken != null)
				jedis.del(new StringBuilder("passion.hospitalpublicity.client.login-").append(oldToken).toString());
			jedis.del(userId);
			jedis.set(loginRedisKey, JSON.toJSONString(loginStatus));
			jedis.set(userId, token);
			jedis.expire(loginRedisKey, 30 * 24 * 60 * 60);
			jedis.expire(userId, 30 * 24 * 60 * 60);

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("token", token);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
		}
	}
}
