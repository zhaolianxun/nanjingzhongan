package easywin.module.mall.api;

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
import easywin.module.mall.business.GetLoginStatus;
import easywin.module.mall.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mall.api.MyFavoriteEntrance")
@RequestMapping(value = "/m/e/myfavorite")
public class MyFavoriteEntrance {

	public static Logger logger = Logger.getLogger(MyFavoriteEntrance.class);

	@RequestMapping(value = "")
	public void myFavorites(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
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
			pst = connection.prepareStatement(
					"select good_id goodId,price,name,cover from t_mall_favor where user_id=? and mall_id=? order by id desc limit ?,?");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, mallId);
			pst.setObject(3, pageSize * (pageNo - 1));
			pst.setObject(4, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray favorites = new JSONArray();
			while (rs.next()) {
				JSONObject favorite = new JSONObject();
				favorite.put("goodId", rs.getObject(1));
				favorite.put("price", rs.getObject(2));
				favorite.put("name", rs.getObject(3));
				favorite.put("cover", rs.getObject(4));
				favorites.add(favorite);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("favorites", favorites);
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

	@RequestMapping(value = "/remove")
	public void remove(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
			String goodId = StringUtils.trimToNull(request.getParameter("good_id"));
			if (goodId == null)
				throw new InteractRuntimeException("good_id不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement("delete from t_mall_favor where mall_id=? and user_id=? and good_id=?");
			pst.setObject(1, mallId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, goodId);
			pst.executeUpdate();
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
