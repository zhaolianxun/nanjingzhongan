package rrightway.module.mall.api;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import rrightway.constant.SysConstant;
import rrightway.entity.InteractRuntimeException;
import rrightway.module.mall.business.GetLoginStatus;
import rrightway.module.mall.entity.UserLoginStatus;
import rrightway.util.RrightwayDataSource;
import rrightway.util.HttpRespondWithData;

@Controller("mall.api.GoodDetailEntrance")
@RequestMapping(value = "/m/e/gooddetail")
public class GoodDetailEntrance {

	public static Logger logger = Logger.getLogger(GoodDetailEntrance.class);

	@RequestMapping(value = "")
	public void goodDetailEntrance(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String goodId = request.getParameter("good_id");
			if (goodId == null || goodId.trim() == "")
				throw new InteractRuntimeException("good_id不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);

			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢商品详情
			pst = connection.prepareStatement(
					"select onsale,tags,detail,id,name,(select min(price) from t_mall_good_sku where good_id=?) price,detail_pics,saled_count,buyer_count,(select sum(inventory) from t_mall_good_sku where good_id=?) inventory,(select min(original_price) from t_mall_good_sku where good_id=?) original_price,params,"
							+ (loginStatus == null ? "0 favor_is"
									: "(select if(count(id)>0,1,0) from t_mall_favor where user_id=? and good_id=?) favor_is")
							+ " from t_mall_good where id=?");

			pst.setObject(1, goodId);
			if (loginStatus == null) {
				pst.setObject(2, goodId);
				pst.setObject(3, goodId);
				pst.setObject(4, goodId);
			} else {
				pst.setObject(2, goodId);
				pst.setObject(3, goodId);
				pst.setObject(4, loginStatus.getUserId());
				pst.setObject(5, goodId);
				pst.setObject(6, goodId);
			}

			ResultSet rs = pst.executeQuery();
			JSONObject good = new JSONObject();
			if (rs.next()) {
				good.put("goodId", rs.getObject("id"));
				good.put("name", rs.getObject("name"));
				good.put("onsale", rs.getObject("onsale"));
				good.put("tags", rs.getObject("tags"));
				good.put("detail", rs.getObject("detail"));
				good.put("price", rs.getObject("price"));
				good.put("detailPics", rs.getObject("detail_pics"));
				good.put("saledCount", rs.getObject("saled_count"));
				good.put("buyerCount", rs.getObject("buyer_count"));
				good.put("inventory", rs.getObject("inventory"));
				good.put("originalPrice", rs.getObject("original_price"));
				good.put("params", rs.getObject("params"));
				good.put("favorIs", rs.getObject("favor_is"));
			} else {
				throw new InteractRuntimeException("商品不存在");
			}
			pst.close();

			// 查询該含有的规格
			pst = connection
					.prepareStatement("select id,name from t_mall_good_attr where good_id=? order by `level` asc");
			pst.setObject(1, goodId);
			rs = pst.executeQuery();
			JSONArray attrs = new JSONArray();
			while (rs.next()) {
				JSONObject attr = new JSONObject();
				attr.put("id", rs.getObject("id"));
				attr.put("name", rs.getObject("name"));
				attrs.add(attr);
			}
			pst.close();

			pst = connection
					.prepareStatement("select id,name from t_mall_good_attr_value where attr_id=?  order by name asc");
			for (int i = 0; i < attrs.size(); i++) {
				// 查詢所有規格下的所有子商品,也就是分支商品
				JSONObject attr = attrs.getJSONObject(i);
				pst.setObject(1, attr.get("id"));
				rs = pst.executeQuery();
				JSONArray values = new JSONArray();
				while (rs.next()) {
					JSONObject value = new JSONObject();
					value.put("id", rs.getObject("id"));
					value.put("name", rs.getObject("name"));
					values.add(value);
				}
				rs.close();
				attr.put("values", values);
			}

			// 查询該含有的sku
			pst = connection.prepareStatement(
					"select id,attr_ids,value_ids,inventory,name,price,original_price from t_mall_good_sku where good_id=?");
			pst.setObject(1, goodId);
			rs = pst.executeQuery();
			JSONArray skus = new JSONArray();
			while (rs.next()) {
				JSONObject sku = new JSONObject();
				sku.put("id", rs.getObject("id"));
				sku.put("attrIds", rs.getObject("attr_ids"));
				sku.put("valueIds", rs.getObject("value_ids"));
				sku.put("inventory", rs.getObject("inventory"));
				sku.put("name", rs.getObject("name"));
				sku.put("price", rs.getObject("price"));
				sku.put("originalPrice", rs.getObject("original_price"));
				skus.add(sku);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			good.put("attrs", attrs);
			good.put("skus", skus);
			data.putAll(good);
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

	@RequestMapping(value = "/detail")
	public void detail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		InputStream detail = null;
		try {
			// 获取请求参数
			String goodId = request.getParameter("good_id");
			if (goodId == null || goodId.trim() == "")
				throw new InteractRuntimeException("good_id不可空");

			// 业务处理
			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢商品详情
			pst = connection.prepareStatement("select detail from t_mall_good where id=?");
			pst.setObject(1, goodId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next())
				throw new InteractRuntimeException("商品不存在");
			detail = rs.getBinaryStream(1);

			pst.close();

			// 返回结果
			response.setCharacterEncoding(SysConstant.SYS_CHARSET);
			response.setStatus(200);
			response.setContentType("application/json;charset=" + SysConstant.SYS_CHARSET);
			IOUtils.copy(detail, response.getOutputStream());
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			response.setCharacterEncoding(SysConstant.SYS_CHARSET);
			response.setStatus(500);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
			if (detail != null)
				detail.close();
		}
	}

	@RequestMapping(value = "/good2cart")
	public void good2Cart(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			String name = request.getParameter("name");
			if (name == null || name == "")
				throw new InteractRuntimeException("name不可空");
			String countParam = request.getParameter("count");
			if (countParam == null)
				throw new InteractRuntimeException("count不可空");
			int count = Integer.parseInt(countParam);
			if (count < 1)
				throw new InteractRuntimeException("count不能小于1");
			String priceParam = request.getParameter("price");
			if (priceParam == null)
				throw new InteractRuntimeException("price不可空");
			int price = Integer.parseInt(priceParam);
			String skuIdParam = StringUtils.trimToNull(request.getParameter("sku_id"));
			if (skuIdParam == null)
				throw new InteractRuntimeException("sku_id 不可空");
			Integer skuId = Integer.parseInt(skuIdParam);
			String attrNames = StringUtils.trimToNull(request.getParameter("attr_names"));
			if (attrNames == null)
				throw new InteractRuntimeException("attr_names 不可空");
			String valueNames = StringUtils.trimToNull(request.getParameter("value_names"));
			if (valueNames == null)
				throw new InteractRuntimeException("value_names 不可空");
			String detailPic = request.getParameter("detail_pic");
			if (detailPic == null)
				throw new InteractRuntimeException("detail_pic不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			// 查询是否是叶子
			pst = connection.prepareStatement(
					"select count(t.id) from t_mall_good_sku u inner join t_mall_good t on t.id=u.good_id where u.good_id=? and u.id=?");
			pst.setObject(1, goodId);
			pst.setObject(2, skuId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next())
				throw new InteractRuntimeException("商品不存在");
			pst.close();

			// 检查购物车中是否已存在
			pst = connection.prepareStatement(
					"select id from t_mall_cart where user_id=? and good_id=? and mall_id=? and sku_id=?");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, goodId);
			pst.setObject(3, mallId);
			pst.setObject(4, skuId);
			rs = pst.executeQuery();
			if (rs.next()) {
				String id = rs.getString(1);
				// 更新数量
				pst = connection.prepareStatement(
						"update t_mall_cart set count=count+?,name=?,price=?,detail_pic=?,sku_id=?,attr_names=?,value_names=? where id=?");
				pst.setObject(1, count);
				pst.setObject(2, name);
				pst.setObject(3, price);
				pst.setObject(4, detailPic);
				pst.setObject(5, skuId);
				pst.setObject(6, attrNames);
				pst.setObject(7, valueNames);
				pst.setObject(8, id);
				int n = pst.executeUpdate();
				if (n == 0)
					throw new InteractRuntimeException("添加失败");
				pst.close();
			} else {
				// 插入购物车
				pst = connection.prepareStatement(
						"insert into t_mall_cart (mall_id,user_id,good_id,count,price,name,detail_pic,sku_id,attr_names,value_names,add_time) values(?,?,?,?,?,?,?,?,?,?,?)");
				pst.setObject(1, mallId);
				pst.setObject(2, loginStatus.getUserId());
				pst.setObject(3, goodId);
				pst.setObject(4, count);
				pst.setObject(5, price);
				pst.setObject(6, name);
				pst.setObject(7, detailPic);
				pst.setObject(8, skuId);
				pst.setObject(9, attrNames);
				pst.setObject(10, valueNames);
				pst.setObject(11, new Date().getTime());
				int n = pst.executeUpdate();
				if (n == 0)
					throw new InteractRuntimeException("添加失败");
				pst.close();
			}

			connection.commit();

			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (pst != null)
				pst.close();
			if (connection != null)
				connection.close();
		}
	}

	@RequestMapping(value = "/favor")
	public void favor(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			String name = request.getParameter("name");
			if (name == null || name == "")
				throw new InteractRuntimeException("name不可空");
			String cover = request.getParameter("cover");
			if (cover == null)
				throw new InteractRuntimeException("cover不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			pst = connection.prepareStatement(
					"select min(t.price) min_price from t_mall_good_sku t inner join t_mall_good u on t.good_id=u.id where t.good_id=?");
			pst.setObject(1, goodId);
			ResultSet rs = pst.executeQuery();
			if (!rs.next())
				throw new InteractRuntimeException("商品不存在");
			BigDecimal minPrice = rs.getBigDecimal(1);
			pst.close();

			// 检查收藏夹中是否已存在
			pst = connection.prepareStatement("delete from t_mall_favor where user_id=? and good_id=? and mall_id=?");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, goodId);
			pst.setObject(3, mallId);
			pst.executeUpdate();

			// 插入购物车
			pst = connection.prepareStatement(
					"insert into t_mall_favor (mall_id,user_id,good_id,price,name,cover) values(?,?,?,?,?,?)");
			pst.setObject(1, mallId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, goodId);
			pst.setObject(4, minPrice);
			pst.setObject(5, name);
			pst.setObject(6, cover);
			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("收藏失败");
			pst.close();

			connection.commit();

			// 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (connection != null)
				connection.rollback();
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
