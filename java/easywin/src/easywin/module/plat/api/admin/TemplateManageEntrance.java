package easywin.module.plat.api.admin;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
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

@Controller("plat.api.admin.TemplateManageEntrance")
@RequestMapping(value = "/p/e/admin/tmplmanage")
public class TemplateManageEntrance {

	public static Logger logger = Logger.getLogger(TemplateManageEntrance.class);

	@RequestMapping(value = "/tmpls")
	public void templates(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String name = StringUtils.trimToNull(request.getParameter("name"));
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

			if (loginStatus.getAdminIs() != 1)
				throw new InteractRuntimeException("您不是管理员");
			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			List sqlParams = new ArrayList();
			if (name != null)
				sqlParams.add(new StringBuilder("%").append(name).append("%").toString());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					"select id,price,agent1_price,agent2_price,agent3_price,icon,name,summary,intro_pic,remark from t_app_seed where 1=1 "
							+ (name == null ? "" : " and name like ? ") + " order by create_time asc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));
			ResultSet rs = pst.executeQuery();
			JSONArray seeds = new JSONArray();
			while (rs.next()) {
				JSONObject seed = new JSONObject();
				seed.put("seedId", rs.getObject("id"));
				seed.put("price", rs.getObject("price"));
				seed.put("icon", rs.getObject("icon"));
				seed.put("name", rs.getObject("name"));
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

	@RequestMapping(value = "/tmpl")
	public void tmpl(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String seedIdParam = StringUtils.trimToNull(request.getParameter("seed_id"));
			if (seedIdParam == null)
				throw new InteractRuntimeException("seed_id 不能空");
			int seedId = Integer.parseInt(seedIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getAdminIs() != 1)
				throw new InteractRuntimeException("您不是管理员");
			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			pst = connection.prepareStatement(
					"select id,price,agent1_price,agent2_price,agent3_price,icon,name,summary,intro_pic,remark from t_app_seed where id=? ");
			pst.setObject(1, seedId);
			ResultSet rs = pst.executeQuery();
			JSONObject seed = new JSONObject();
			while (rs.next()) {
				seed.put("seedId", rs.getObject("id"));
				seed.put("price", rs.getObject("price"));
				seed.put("agent1Price", rs.getObject("agent1_price"));
				seed.put("agent2Price", rs.getObject("agent2_price"));
				seed.put("agent3Price", rs.getObject("agent3_price"));
				seed.put("icon", rs.getObject("icon"));
				seed.put("name", rs.getObject("name"));
				seed.put("summary", rs.getObject("summary"));
				seed.put("introPic", rs.getObject("intro_pic"));
				seed.put("remark", rs.getObject("remark"));
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(seed);
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

	@RequestMapping(value = "/alter")
	public void alter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String seedId = StringUtils.trimToNull(request.getParameter("seed_id"));
			if (seedId == null)
				throw new InteractRuntimeException("seed_id 不能为空");
			String priceParam = StringUtils.trimToNull(request.getParameter("price"));
			BigDecimal price = priceParam == null ? null : new BigDecimal(priceParam);
			String agent1PriceParam = StringUtils.trimToNull(request.getParameter("agent1_price"));
			BigDecimal agent1Price = agent1PriceParam == null ? null : new BigDecimal(agent1PriceParam);
			String agent2PriceParam = StringUtils.trimToNull(request.getParameter("agent2_price"));
			BigDecimal agent2Price = agent2PriceParam == null ? null : new BigDecimal(agent2PriceParam);
			String agent3PriceParam = StringUtils.trimToNull(request.getParameter("agent3_price"));
			BigDecimal agent3Price = agent3PriceParam == null ? null : new BigDecimal(agent3PriceParam);
			String icon = StringUtils.trimToNull(request.getParameter("icon"));
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String summary = StringUtils.trimToNull(request.getParameter("summary"));
			String introPic = StringUtils.trimToNull(request.getParameter("intro_pic"));
			String remark = StringUtils.trimToNull(request.getParameter("remark"));

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getAdminIs() != 1)
				throw new InteractRuntimeException("您不是管理员");
			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢订单列表
			List sqlParams = new ArrayList();
			if (price != null)
				sqlParams.add(price);
			if (agent1Price != null)
				sqlParams.add(agent1Price);
			if (agent2Price != null)
				sqlParams.add(agent2Price);
			if (agent3Price != null)
				sqlParams.add(agent3Price);
			if (icon != null)
				sqlParams.add(icon);
			if (name != null)
				sqlParams.add(name);
			if (summary != null)
				sqlParams.add(summary);
			if (introPic != null)
				sqlParams.add(introPic);
			if (remark != null)
				sqlParams.add(remark);
			sqlParams.add(seedId);
			pst = connection.prepareStatement("update t_app_seed set id=id " + (price == null ? "" : " ,price=? ")
					+ (agent1Price == null ? "" : " ,agent1_price=? ")
					+ (agent2Price == null ? "" : " ,agent2_price=? ")
					+ (agent3Price == null ? "" : " ,agent3_price=? ") + (icon == null ? "" : " ,icon=? ")
					+ (name == null ? "" : " ,name=? ") + (summary == null ? "" : " ,summary=? ")
					+ (introPic == null ? "" : " ,intro_pic=? ") + (remark == null ? "" : " ,remark=? ")
					+ " where id=?");
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));
			int n = pst.executeUpdate();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

			pst.close();

			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
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