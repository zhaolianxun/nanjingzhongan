package easywin.module.plat.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

@Controller("plat.api.AdminAppManageEntrance")
@RequestMapping(value = "/p/e/appmanage")
public class AdminAppManageEntrance {

	public static Logger logger = Logger.getLogger(AdminAppManageEntrance.class);

	@RequestMapping(value = "/apps")
	public void apps(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String phone = StringUtils.trimToNull(request.getParameter("phone"));
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
			List sqlParams = new ArrayList();
			if (phone != null)
				sqlParams.add(phone);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					"select t.head_img,t.seed_id,t.nick_name,t.id,t.current_template_version,t.authorized,t.audit_template_version,t.commit_template_version,t.bind_time,t.commit_status,t.audit_status,t.audit_fail_reason,u.newest_version newest_template_version,t.use_endtime,u.template_code from t_app t left join t_app_seed u on t.seed_id=u.id left join t_user v on v.id=t.user_id where 1=1 "
							+ (phone == null ? "" : " and phone=?") + " order by t.bind_time desc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));

			ResultSet rs = pst.executeQuery();
			JSONArray apps = new JSONArray();
			while (rs.next()) {
				JSONObject app = new JSONObject();
				app.put("appId", rs.getObject("id"));
				app.put("headImg", rs.getObject("head_img"));
				app.put("seedId", rs.getObject("seed_id"));
				app.put("nickName", rs.getObject("nick_name"));
				app.put("currentTemplateVersion", rs.getObject("current_template_version"));
				app.put("authorized", rs.getObject("authorized"));
				app.put("auditTemplateVersion", rs.getObject("audit_template_version"));
				app.put("commitTemplateVersion", rs.getObject("commit_template_version"));
				app.put("bindTime", rs.getObject("bind_time"));
				app.put("commitStatus", rs.getObject("commit_status"));
				app.put("auditStatus", rs.getObject("audit_status"));
				app.put("auditFailReason", rs.getObject("audit_fail_reason"));
				app.put("newestTemplateVersion", rs.getObject("newest_template_version"));
				app.put("useEndtime", rs.getObject("use_endtime"));
				app.put("templateCode", rs.getObject("template_code"));
				apps.add(app);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("apps", apps);
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