package easywin.module.plat.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import easywin.util.EasywinDataSource;
import okhttp3.Request;
import okhttp3.Response;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

@Component
public class PullWxMiniappTemplates {

	private static Logger logger = Logger.getLogger(PullWxMiniappTemplates.class);

	public PullWxMiniappTemplates() {
		logger.info("启动定时任务 DailyReportReminder");
	}

	@Scheduled(cron = "0 0/5 * * * ?")
	public void run() {
		logger.debug("执行 task.PullWxMiniappTemplates 定时任务");
		Jedis jedis = null;
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			jedis = SysConstant.jedisPool.getResource();
			// 获取componentAccessToken，并判断如果为空则退出
			String componentAccessToken = jedis.get(SysConstant.wechat_open_thirdparty_AppId + "-componentAccessToken");
			jedis.close();
			jedis = null;
			logger.debug("componentAccessToken " + componentAccessToken);

			if (StringUtils.isEmpty(componentAccessToken))
				return;

			// 从微信拉取模板列表
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
				throw new InteractRuntimeException(resultVo.getString("errmsg"));

			// 把模板信息保存到数据库
			JSONArray templateList = resultVo.getJSONArray("template_list");
			connection = EasywinDataSource.dataSource.getConnection();
			for (int i = 0; i < templateList.size(); i++) {
				JSONObject template = templateList.getJSONObject(i);
				int wxTemplateId = template.getIntValue("template_id");
				String wxUserDesc = template.getString("user_desc");
				String wxUserVersion = template.getString("user_version");
				String wxSourceMiniprogramAppid = template.getString("source_miniprogram_appid");
				String wxSourceMiniprogram = template.getString("source_miniprogram");
				String wxSourceMiniprogramDeveloper = template.getString("developer");
				String tplCode = wxUserDesc;
				String tplVersion = wxUserVersion;
				Long wxCreateTime = template.getLong("create_time");

				pst = connection.prepareStatement(
						"select (select t.id from t_seed_template t  where  t.tpl_version=? and t.tpl_code=?) id,(select id from t_app_seed where template_code=?) seed_id");
				pst.setObject(1, tplVersion);
				pst.setObject(2, tplCode);
				pst.setObject(3, tplCode);
				ResultSet rs = pst.executeQuery();
				Integer templateIdOld = null;
				String seedId = null;

				if (rs.next()) {
					templateIdOld = (Integer) rs.getObject("id");
					seedId = rs.getString("seed_id");
				}
				pst.close();
				
				if(seedId == null)
					continue;
				if (templateIdOld == null) {
					// 插入新的模板
					pst = connection.prepareStatement(
							"insert into t_seed_template (wx_templateid,tpl_version,tpl_code,wx_create_time,seed_id,wx_user_version,wx_user_desc,wx_source_miniprogram_appid,wx_source_miniprogram,wx_source_miniprogram_developer) values(?,?,?,?,?,?,?,?,?,?)");
					pst.setObject(1, wxTemplateId);
					pst.setObject(2, tplVersion);
					pst.setObject(3, tplCode);
					pst.setObject(4, wxCreateTime);
					pst.setObject(5, seedId);
					pst.setObject(6, wxUserVersion);
					pst.setObject(7, wxUserDesc);
					pst.setObject(8, wxSourceMiniprogramAppid);
					pst.setObject(9, wxSourceMiniprogram);
					pst.setObject(10, wxSourceMiniprogramDeveloper);
					int n = pst.executeUpdate();
					pst.close();
					if (n != 1)
						throw new InteractRuntimeException(resultVo.getString("操作失败"));
				} else {
					// 更新已有的模板
					pst = connection.prepareStatement(
							"update t_seed_template set wx_create_time=?,tpl_version=?,tpl_code=?,seed_id=?,wx_user_version=?,wx_user_desc=?,wx_source_miniprogram_appid=?,wx_source_miniprogram=?,wx_source_miniprogram_developer=? where id=?");
					pst.setObject(1, wxCreateTime);
					pst.setObject(2, tplVersion);
					pst.setObject(3, tplCode);
					pst.setObject(4, seedId);
					pst.setObject(5, wxUserVersion);
					pst.setObject(6, wxUserDesc);
					pst.setObject(7, wxSourceMiniprogramAppid);
					pst.setObject(8, wxSourceMiniprogram);
					pst.setObject(9, wxSourceMiniprogramDeveloper);
					pst.setObject(10, templateIdOld);
					int n = pst.executeUpdate();
					pst.close();
					if (n != 1)
						throw new InteractRuntimeException(resultVo.getString("操作失败"));
				}
			}

			pst = connection.prepareStatement(
					"update t_app_seed t set newest_version=(select max(tpl_version) from t_seed_template where tpl_code=t.template_code)");
			pst.executeUpdate();
			pst.close();
		} catch (Exception e) {
			logger.info(ExceptionUtils.getStackTrace(e));
		} finally {
			if (jedis != null)
				try {
					jedis.close();
				} catch (JedisException e) {
					logger.info(ExceptionUtils.getStackTrace(e));
				}
			if (pst != null)
				try {
					pst.close();
				} catch (SQLException e) {
					logger.info(ExceptionUtils.getStackTrace(e));
				}
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger.info(ExceptionUtils.getStackTrace(e));
				}
			logger.debug("执行结束 task.PullWxMiniappTemplates 定时任务");
		}
	}

}
