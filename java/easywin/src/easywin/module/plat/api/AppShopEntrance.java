package easywin.module.plat.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("plat.api.AppShopEntrance")
@RequestMapping(value = "/p/e/appshop")
public class AppShopEntrance {

	public static Logger logger = Logger.getLogger(AppShopEntrance.class);

	@RequestMapping(value = "/apps")
	public void templates(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select id,price,icon,name,summary,intro_pic,remark,newest_version from t_app_seed order by create_time asc limit ?,?");
			pst.setObject(1, pageSize * (pageNo - 1));
			pst.setObject(2, pageSize);
			ResultSet rs = pst.executeQuery();
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
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("seeds", seeds);
			HttpRespondWithData.todo(request, response, 0, null, data);
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