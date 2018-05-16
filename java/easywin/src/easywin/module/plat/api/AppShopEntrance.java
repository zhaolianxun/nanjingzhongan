package easywin.module.plat.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import easywin.constant.OutApis;
import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import redis.clients.jedis.Jedis;

@Controller("plat.api.AppShopEntrance")
@RequestMapping(value = "/p/e/appshop")
public class AppShopEntrance {

	public static Logger logger = Logger.getLogger(AppShopEntrance.class);

	@RequestMapping(value = "/apps")
	public void templates(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			logger.debug("1");
			// 获取请求参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");
			logger.debug("2");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			logger.debug("3");
			connection = EasywinDataSource.dataSource.getConnection();
			logger.debug("4");
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select id,price,icon,name,summary,intro_pic,remark,newest_version from t_app_seed order by create_time asc limit ?,?");
			logger.debug("5");
			pst.setObject(1, pageSize * (pageNo - 1));
			pst.setObject(2, pageSize);
			ResultSet rs = pst.executeQuery();
			logger.debug("6");
			JSONArray seeds = new JSONArray();
			while (rs.next()) {
				JSONObject seed = new JSONObject();
				seed.put("seedId", rs.getObject("id"));
				seed.put("price", rs.getObject("price"));
				seed.put("icon", rs.getObject("icon"));
				seed.put("name", rs.getObject("name"));
				seed.put("summary", rs.getObject("summary"));
				seed.put("introPic", rs.getObject("intro_pic"));
				seed.put("remark", rs.getObject("remark"));
				seed.put("newestVersion", rs.getObject("newest_version"));
				seeds.add(seed);
			}
			logger.debug("7");
			pst.close();
			logger.debug("8");
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("seeds", seeds);
			logger.debug("9");
			HttpRespondWithData.todo(request, response, 0, null, data);
			logger.debug("10");
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/saomatiyan")
	public void saomatiyan(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String seedId = StringUtils.trimToNull(request.getParameter("seed_id"));
			if (seedId == null)
				throw new InteractRuntimeException("seed_id 不能为空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select u.access_token from t_seed_template t inner join t_app u on t.wx_source_miniprogram_appid=u.wx_appid where t.seed_id=? and t.tpl_version=(select max(tpl_version) from t_seed_template where seed_id=t.seed_id)");
			pst.setObject(1, seedId);
			ResultSet rs = pst.executeQuery();
			String accessToken = null;
			if (rs.next()) {
				accessToken = rs.getString("access_token");
			} else
				throw new InteractRuntimeException("模板不存在");
			pst.close();

			String url = new StringBuilder("https://api.weixin.qq.com/wxa/getwxacodeunlimit").append("?")
					.append("access_token=").append(accessToken).toString();
			JSONObject od = new JSONObject();
			od.put("scene", 1);
			Request okHttpRequest = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("application/json"), od.toJSONString())).build();
			Response okHttpResponse = SysConstant.okHttpClient.newCall(okHttpRequest).execute();

			// 返回结果
			response.setCharacterEncoding(SysConstant.SYS_CHARSET);
			response.setStatus(200);
			IOUtils.copy(okHttpResponse.body().byteStream(), response.getOutputStream());
			okHttpResponse.close();
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

}