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

@Controller("mall.api.CartEntrance")
@RequestMapping(value = "/m/e/cart")
public class CartEntrance {

	public static Logger logger = Logger.getLogger(CartEntrance.class);

	@RequestMapping(value = "")
	public void goodDetailEntrance(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
				throw new InteractRuntimeException("mall_id不可空");
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");

			logger.debug("mallId=" + mallId + " pageNoParam=" + pageNoParam + " pageSizeParam=" + pageSizeParam);
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select good_id,count,price,name,sku_id,attr_names,value_names,detail_pic,(select onsale from t_mall_good where id=t.good_id) onsale,(select inventory from t_mall_good_sku where id=t.sku_id) sku_inventory from t_mall_cart t where user_id=? and mall_id=? order by id desc limit ?,?");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, mallId);
			pst.setObject(3, pageSize * (pageNo - 1));
			pst.setObject(4, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray goods = new JSONArray();
			while (rs.next()) {
				JSONObject good = new JSONObject();
				good.put("goodId", rs.getObject("good_id"));
				good.put("count", rs.getObject("count"));
				good.put("onsale", rs.getObject("onsale"));
				good.put("price", rs.getObject("price"));
				good.put("name", rs.getObject("name"));
				good.put("skuId", rs.getObject("sku_id"));
				good.put("attrNames", rs.getObject("attr_names"));
				good.put("valueNames", rs.getObject("value_names"));
				good.put("detailPic", rs.getObject("detail_pic"));
				good.put("skuInventory", rs.getObject("sku_inventory"));
				goods.add(good);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("goods", goods);
			HttpRespondWithData.todo(request, response, 0, null, goods);
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

	@RequestMapping(value = "/changecount")
	public void changeCount(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
				throw new InteractRuntimeException("mall_id不可空");
			String goodId = request.getParameter("good_id");
			if (goodId == null || goodId.trim() == "")
				throw new InteractRuntimeException("good_id不可空");
			String countParam = request.getParameter("count");
			if (countParam == null || countParam.trim() == "")
				throw new InteractRuntimeException("count不可空");
			int count = Integer.parseInt(countParam);
			if (count <= 0)
				throw new InteractRuntimeException("count有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection
					.prepareStatement("update t_mall_cart set count=? where mall_id=? and user_id=? and good_id=?");
			pst.setObject(1, count);
			pst.setObject(2, mallId);
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, goodId);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/addcount")
	public void addCount(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
				throw new InteractRuntimeException("mall_id不可空");
			String goodId = request.getParameter("good_id");
			if (goodId == null || goodId.trim() == "")
				throw new InteractRuntimeException("good_id不可空");
			String countParam = request.getParameter("count");
			if (countParam == null || countParam.trim() == "")
				throw new InteractRuntimeException("count不可空");
			int count = Integer.parseInt(countParam);
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"update t_mall_cart set count=count+? where mall_id=? and user_id=? and good_id=?");
			pst.setObject(1, count);
			pst.setObject(2, mallId);
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, goodId);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/subcount")
	public void subCount(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
				throw new InteractRuntimeException("mall_id不可空");
			String goodId = request.getParameter("good_id");
			if (goodId == null || goodId.trim() == "")
				throw new InteractRuntimeException("good_id不可空");
			String countParam = request.getParameter("count");
			if (countParam == null || countParam.trim() == "")
				throw new InteractRuntimeException("count不可空");
			int count = Integer.parseInt(countParam);

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"update t_mall_cart set count=count-? where mall_id=? and user_id=? and good_id=? and count-? > 0");
			pst.setObject(1, count);
			pst.setObject(2, mallId);
			pst.setObject(3, loginStatus.getUserId());
			pst.setObject(4, goodId);
			pst.setObject(5, count);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/remove")
	public void remove(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
				throw new InteractRuntimeException("mall_id不可空");
			String goodId = request.getParameter("good_id");
			if (goodId == null || goodId.trim() == "")
				throw new InteractRuntimeException("good_id不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement("delete from t_mall_cart  where mall_id=? and user_id=? and good_id=? ");
			pst.setObject(1, mallId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, goodId);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");

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
