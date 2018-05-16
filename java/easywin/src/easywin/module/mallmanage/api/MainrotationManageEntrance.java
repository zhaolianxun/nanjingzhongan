package easywin.module.mallmanage.api;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mallManage.api.MainrotationManageEntrance")
@RequestMapping(value = "/mm/{mallId}/e/mainrotation")
public class MainrotationManageEntrance {

	public static Logger logger = Logger.getLogger(MainrotationManageEntrance.class);

	@RequestMapping(value = "/list")
	public void type1s(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection
					.prepareStatement("select id,pic,type,link from t_mall_mainrotation where mall_id=? limit 0,50");
			pst.setObject(1, mallId);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("pic", rs.getObject("pic"));
				item.put("type", rs.getObject("type"));
				item.put("link", rs.getObject("link"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject mainrotations = new JSONObject();
			mainrotations.put("items", items);
			data.put("mainrotations", mainrotations);
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

	
	@RequestMapping(value = "goods")
	public void goods(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
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

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			List sqlParams = new ArrayList();
			sqlParams.add(mallId);
			if (name != null)
				sqlParams.add(name);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					"select (select sum(inventory) from t_mall_good_sku where good_id=t.id) inventory,t.id,t.name,(select min(price) min_price from t_mall_good_sku where good_id=t.id) price,t.onsale,t.cover from t_mall_good t where  t.mall_id=?"
							+ (name == null ? "" : " and t.name=?") + " order by t.add_time desc limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("goodId", rs.getObject("id"));
				item.put("name", rs.getObject("name"));
				item.put("price", rs.getObject("price"));
				item.put("onsale", rs.getObject("onsale"));
				item.put("inventory", rs.getObject("inventory"));
				item.put("cover", rs.getObject("cover"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("goods", items);
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
	
	
	@RequestMapping(value = "/add")
	public void addType1(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String pic = StringUtils.trimToNull(request.getParameter("pic"));
			if (pic == null)
				throw new InteractRuntimeException("pic 不可空");
			String type = StringUtils.trimToNull(request.getParameter("type"));
			if (type == null)
				throw new InteractRuntimeException("type 不可空");
			String link = StringUtils.trimToNull(request.getParameter("link"));
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢商品详情
			pst = connection.prepareStatement("insert into t_mall_mainrotation (mall_id,pic,type,link) values(?,?,?,?)",
					PreparedStatement.RETURN_GENERATED_KEYS);

			pst.setObject(1, mallId);
			pst.setObject(2, pic);
			pst.setObject(3, type);
			pst.setObject(4, link);

			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");
			ResultSet rs = pst.getGeneratedKeys();
			rs.next();
			int id = rs.getInt(1);
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("mainrotationId", id);
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

	@RequestMapping(value = "/del")
	public void delType(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mainrotationIdParam = StringUtils.trimToNull(request.getParameter("mainrotation_id"));
			if (mainrotationIdParam == null)
				throw new InteractRuntimeException("mainrotation_id 不可空");
			Integer mainrotationId = Integer.parseInt(mainrotationIdParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢商品详情
			pst = connection.prepareStatement("delete from t_mall_mainrotation where id=?");
			pst.setObject(1, mainrotationId);

			int n = pst.executeUpdate();
			pst.close();
			if (n == 0) {
				throw new InteractRuntimeException("轮播图不存在");
			}

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
