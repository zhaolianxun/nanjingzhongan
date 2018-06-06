package easywin.module.mallmanage.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mallManage.api.GoodManageEntrance")
@RequestMapping(value = "/mm/{mallId}/e/goodmanage")
public class GoodManageEntrance {

	public static Logger logger = Logger.getLogger(GoodManageEntrance.class);

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
				sqlParams.add(new StringBuilder("%").append(name).append("%").toString());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			pst = connection.prepareStatement(
					"select (select sum(inventory) from t_mall_good_sku where good_id=t.id) inventory,t.id,t.name,(select min(price) min_price from t_mall_good_sku where good_id=t.id) price,t.onsale,t.cover,t.saled_count,t.buyer_count from t_mall_good t where  t.mall_id=?"
							+ (name == null ? "" : " and t.name like ?") + " order by t.add_time desc limit ?,?");
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
				item.put("saledCount", rs.getObject("saled_count"));
				item.put("buyerCount", rs.getObject("buyer_count"));
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

	@RequestMapping(value = "/goodinfo")
	public void goodInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String goodId = request.getParameter("good_id");
			if (goodId == null || goodId.trim() == "")
				throw new InteractRuntimeException("good_id不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢商品详情
			pst = connection.prepareStatement(
					"select share_reward,detail,id,name,detail_pics,saled_count,buyer_count,params,onsale,type1_name,type2_name,type3_name,cover from t_mall_good where id=?");

			pst.setObject(1, goodId);

			ResultSet rs = pst.executeQuery();
			JSONObject good = new JSONObject();
			if (rs.next()) {
				good.put("goodId", rs.getObject("id"));
				good.put("name", rs.getObject("name"));
				good.put("detailPics", rs.getObject("detail_pics"));
				good.put("saledCount", rs.getObject("saled_count"));
				good.put("buyerCount", rs.getObject("buyer_count"));
				good.put("params", rs.getObject("params"));
				good.put("onsale", rs.getObject("onsale"));
				good.put("type1Name", rs.getObject("type1_name"));
				good.put("type2Name", rs.getObject("type2_name"));
				good.put("type3Name", rs.getObject("type3_name"));
				good.put("cover", rs.getObject("cover"));
				good.put("detail", rs.getObject("detail"));
				good.put("shareReward", rs.getObject("share_reward"));
			} else {
				throw new InteractRuntimeException("商品不存在");
			}
			pst.close();

			// 查询該含有的sku
			pst = connection.prepareStatement(
					"select u.name sku_name,t.id,t.inventory,t.price,t.original_price from t_mall_good_sku t inner join t_mall_good_attr_value u on t.value_ids=u.id where t.good_id=?");
			pst.setObject(1, goodId);
			rs = pst.executeQuery();
			JSONArray skus = new JSONArray();
			while (rs.next()) {
				JSONObject sku = new JSONObject();
				sku.put("id", rs.getObject("id"));
				sku.put("inventory", rs.getObject("inventory"));
				sku.put("price", rs.getObject("price"));
				sku.put("originalPrice", rs.getObject("original_price"));
				sku.put("skuName", rs.getObject("sku_name"));
				skus.add(sku);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
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

	@RequestMapping(value = "/addgood")
	public void addGood(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("name 不可空");
			String tags = StringUtils.trimToNull(request.getParameter("tags"));
			String type1Param = StringUtils.trimToNull(request.getParameter("type1"));
			if (type1Param == null)
				throw new InteractRuntimeException("type1 不可空");
			Integer type1 = Integer.parseInt(type1Param);
			String type2Param = StringUtils.trimToNull(request.getParameter("type2"));
			Integer type2 = type2Param == null ? null : Integer.parseInt(type2Param);

			String type1Name = StringUtils.trimToNull(request.getParameter("type1_name"));
			if (type1Name == null)
				throw new InteractRuntimeException("type1_name 不可空");
			String type2Name = StringUtils.trimToNull(request.getParameter("type2_name"));
			if (type2 != null && type2Name == null)
				throw new InteractRuntimeException("type2_name 不可空");
			String onsale = StringUtils.trimToNull(request.getParameter("onsale"));
			onsale = onsale == null ? "0" : "1";
			String detail = StringUtils.trimToNull(request.getParameter("detail"));
			String params = StringUtils.trimToNull(request.getParameter("params"));
			String detailPics = StringUtils.trimToNull(request.getParameter("detail_pics"));
			String cover = StringUtils.trimToNull(request.getParameter("cover"));
			String shareRewardParam = StringUtils.trimToNull(request.getParameter("share_reward"));
			int shareReward = shareRewardParam == null ? 0 : Integer.parseInt(shareRewardParam);
			String skusParam = StringUtils.trimToNull(request.getParameter("skus"));
			if (skusParam == null)
				throw new InteractRuntimeException("skus 不可空");
			JSONArray skus = null;
			try {
				skus = JSON.parseArray(skusParam);
			} catch (Exception e) {
				throw new InteractRuntimeException("skus 格式有误");
			}

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查詢商品详情
			pst = connection.prepareStatement(
					"INSERT INTO `t_mall_good` (`id`, `mall_id`, `name`, `tags`, `type1`, `type1_name`, `type2`, `type2_name`,  `onsale`, `detail`, `params`, `detail_pics`, `cover`, `add_time`,`base_alter_time`,`share_reward`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			String goodId = RandomStringUtils.randomNumeric(12);
			pst.setObject(1, goodId);
			pst.setObject(2, mallId);
			pst.setObject(3, name);
			pst.setObject(4, tags);
			pst.setObject(5, type1);
			pst.setObject(6, type1Name);
			pst.setObject(7, type2);
			pst.setObject(8, type2Name);
			pst.setObject(9, onsale);
			pst.setObject(10, detail);
			pst.setObject(11, params);
			pst.setObject(12, detailPics);
			pst.setObject(13, cover);
			long addTime = new Date().getTime();
			pst.setObject(14, addTime);
			pst.setObject(15, addTime);
			pst.setObject(16, shareReward);

			int n = pst.executeUpdate();
			pst.close();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");

			// 插入属性
			pst = connection.prepareStatement(
					"INSERT INTO `t_mall_good_attr` ( `good_id`, `name`, `level`) VALUES ( ?, ?, ?)",
					PreparedStatement.RETURN_GENERATED_KEYS);
			pst.setObject(1, goodId);
			pst.setObject(2, "规格");
			pst.setObject(3, 1);
			n = pst.executeUpdate();
			if (n == 0) {
				pst.close();
				throw new InteractRuntimeException("操作失败");
			}
			ResultSet rs = pst.getGeneratedKeys();
			rs.next();
			int attrId = rs.getInt(1);
			pst.close();

			for (int i = 0; i < skus.size(); i++) {
				JSONObject sku = skus.getJSONObject(i);
				String skuName = sku.getString("skuName");
				Integer skuInventory = sku.getInteger("inventory");
				Integer skuPrice = sku.getInteger("price");
				Integer skuOriginalPrice = sku.getInteger("originalPrice");

				if (skuName == null)
					throw new InteractRuntimeException("skus[x].skuName 不能空");
				if (skuInventory == null)
					throw new InteractRuntimeException("skus[x].inventory 不能空");
				if (skuPrice == null)
					throw new InteractRuntimeException("skus[x].price 不能空");
				if (skuOriginalPrice == null)
					throw new InteractRuntimeException("skus[x].originalPrice 不能空");

				// 插入属性值
				pst = connection.prepareStatement(
						"INSERT INTO `t_mall_good_attr_value` ( `attr_id`, `name`) VALUES ( ?, ?)",
						PreparedStatement.RETURN_GENERATED_KEYS);
				pst.setObject(1, attrId);
				pst.setObject(2, skuName);
				n = pst.executeUpdate();
				if (n == 0) {
					pst.close();
					throw new InteractRuntimeException("操作失败");
				}
				rs = pst.getGeneratedKeys();
				rs.next();
				int valueId = rs.getInt(1);
				pst.close();

				// 插入sku
				pst = connection.prepareStatement(
						"INSERT INTO `t_mall_good_sku` ( `good_id`, `attr_ids`, `value_ids`, `inventory`, `price`, `original_price`) VALUES (?,?,?,?,?,?)");
				pst.setObject(1, goodId);
				pst.setObject(2, attrId);
				pst.setObject(3, valueId);
				pst.setObject(4, skuInventory);
				pst.setObject(5, skuPrice);
				pst.setObject(6, skuOriginalPrice);
				n = pst.executeUpdate();
				if (n == 0) {
					pst.close();
					throw new InteractRuntimeException("操作失败");
				}
				pst.close();
			}
			connection.commit();
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("goodId", goodId);
			HttpRespondWithData.todo(request, response, 0, null, data);
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

	@RequestMapping(value = "/altergood")
	public void alterGood(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String goodId = StringUtils.trimToNull(request.getParameter("good_id"));
			if (goodId == null)
				throw new InteractRuntimeException("good_id 不可空");
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String tags = StringUtils.trimToNull(request.getParameter("tags"));
			String type1Param = StringUtils.trimToNull(request.getParameter("type1"));
			Integer type1 = type1Param == null ? null : Integer.parseInt(type1Param);
			String type2Param = StringUtils.trimToNull(request.getParameter("type2"));
			Integer type2 = type2Param == null ? null : Integer.parseInt(type2Param);

			String type1Name = StringUtils.trimToNull(request.getParameter("type1_name"));
			if (type1 != null && type1Name == null)
				throw new InteractRuntimeException("type1_name 不可空");
			String type2Name = StringUtils.trimToNull(request.getParameter("type2_name"));
			if (type2 != null && type2Name == null)
				throw new InteractRuntimeException("type2_name 不可空");
			String onsale = StringUtils.trimToNull(request.getParameter("onsale"));
			String detail = StringUtils.trimToNull(request.getParameter("detail"));
			String params = StringUtils.trimToNull(request.getParameter("params"));
			String detailPics = StringUtils.trimToNull(request.getParameter("detail_pics"));
			String cover = StringUtils.trimToNull(request.getParameter("cover"));
			String shareRewardParam = StringUtils.trimToNull(request.getParameter("share_reward"));
			Integer shareReward = shareRewardParam == null ? null : Integer.parseInt(shareRewardParam);
			String skusParam = StringUtils.trimToNull(request.getParameter("skus"));
			JSONArray skus = null;
			if (skusParam != null)
				try {
					skus = JSON.parseArray(skusParam);
				} catch (Exception e) {
					throw new InteractRuntimeException("skus 格式有误");
				}

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查詢商品详情
			boolean baseAlter = false;
			boolean sqlParamsEmpty = false;
			List sqlParams = new ArrayList();
			if (shareReward != null)
				sqlParams.add(shareReward);
			if (name != null)
				sqlParams.add(name);
			if (tags != null)
				sqlParams.add(tags);
			if (type1 != null)
				sqlParams.add(type1);
			if (type1Name != null)
				sqlParams.add(type1Name);
			if (type2 != null)
				sqlParams.add(type2);
			if (type2Name != null)
				sqlParams.add(type2Name);
			if (onsale != null)
				sqlParams.add(onsale);
			if (detail != null)
				sqlParams.add(detail);
			if (params != null)
				sqlParams.add(params);
			if (detailPics != null)
				sqlParams.add(detailPics);
			if (cover != null)
				sqlParams.add(cover);
			if (!sqlParams.isEmpty()) {
				baseAlter = true;
				sqlParams.add(new Date().getTime());
			}
			if (!sqlParams.isEmpty()) {
				sqlParams.add(goodId);
				String s = (shareReward == null ? "" : " ,`share_reward`=?") + (name == null ? "" : " ,`name`=?")
						+ (tags == null ? "" : " ,`tags`=?") + (type1 == null ? "" : " ,`type1`=?")
						+ (type1Name == null ? "" : " ,`type1_name`=?") + (type2 == null ? "" : " ,`type2`=?")
						+ (type2Name == null ? "" : " ,`type2_name`=?") + (onsale == null ? "" : " ,`onsale`=?")
						+ (detail == null ? "" : " ,`detail`=?") + (params == null ? "" : " ,`params`=?")
						+ (detailPics == null ? "" : " ,`detail_pics`=?") + (cover == null ? "" : " ,`cover`=?")
						+ (!baseAlter ? "" : " ,`base_alter_time`=?");
				pst = connection.prepareStatement("update `t_mall_good` set " + s.substring(2) + " where id=?");
				for (int i = 0; i < sqlParams.size(); i++) {
					pst.setObject(i + 1, sqlParams.get(i));
				}
				int n = pst.executeUpdate();
				pst.close();
				if (n == 0)
					throw new InteractRuntimeException("商品不存在");
				else if (n > 1)
					throw new InteractRuntimeException("操作失败");
			}

			for (int i = 0; i < skus.size(); i++) {
				JSONObject sku = skus.getJSONObject(i);
				Integer skuId = sku.getInteger("id");
				String skuName = sku.getString("skuName");
				Integer skuInventory = sku.getInteger("inventory");
				Integer skuPrice = sku.getInteger("price");
				Integer skuOriginalPrice = sku.getInteger("originalPrice");
				if (skuId != null) {
					if (skuName != null) {
						pst = connection.prepareStatement(
								"update `t_mall_good_attr_value` t inner join t_mall_good_sku u on t.id=u.value_ids set t.name=?  where u.id=? and u.good_id=?");
						pst.setObject(1, skuName);
						pst.setObject(2, skuId);
						pst.setObject(3, goodId);
						int n = pst.executeUpdate();
						pst.close();
						if (n == 0)
							throw new InteractRuntimeException("sku不存在");
						else if (n > 1)
							throw new InteractRuntimeException("操作失败");
					}

					// 插入sku
					List sqlParams1 = new ArrayList();
					if (skuInventory != null)
						sqlParams1.add(skuInventory);
					if (skuPrice != null)
						sqlParams1.add(skuPrice);
					if (skuOriginalPrice != null)
						sqlParams1.add(skuOriginalPrice);
					if (!sqlParams1.isEmpty()) {
						sqlParams1.add(skuId);
						pst = connection.prepareStatement("update `t_mall_good_sku` set id=id"
								+ (skuInventory == null ? "" : " ,inventory=?") + (skuPrice == null ? "" : " ,price=?")
								+ (skuOriginalPrice == null ? "" : " ,original_price=?") + " where id=?");
						for (int j = 0; j < sqlParams1.size(); j++) {
							pst.setObject(j + 1, sqlParams1.get(j));
						}
						int n = pst.executeUpdate();
						pst.close();
						if (n == 0)
							throw new InteractRuntimeException("sku不存在");
						else if (n > 1)
							throw new InteractRuntimeException("操作失败");
					}
				} else {
					if (skuName == null)
						throw new InteractRuntimeException("skus[x].skuName 不能空");
					if (skuInventory == null)
						throw new InteractRuntimeException("skus[x].inventory 不能空");
					if (skuPrice == null)
						throw new InteractRuntimeException("skus[x].price 不能空");
					if (skuOriginalPrice == null)
						throw new InteractRuntimeException("skus[x].originalPrice 不能空");

					pst = connection.prepareStatement("select id from t_mall_good_attr where good_id=?");
					pst.setObject(1, goodId);
					ResultSet rs = pst.executeQuery();
					rs.next();
					int attrId = rs.getInt(1);
					pst.close();

					// 插入属性值
					pst = connection.prepareStatement(
							"INSERT INTO `t_mall_good_attr_value` ( `attr_id`, `name`) VALUES ( ?, ?)",
							PreparedStatement.RETURN_GENERATED_KEYS);
					pst.setObject(1, attrId);
					pst.setObject(2, skuName);
					int n = pst.executeUpdate();
					if (n == 0) {
						pst.close();
						throw new InteractRuntimeException("操作失败");
					}
					rs = pst.getGeneratedKeys();
					rs.next();
					int valueId = rs.getInt(1);
					pst.close();

					// 插入sku
					pst = connection.prepareStatement(
							"INSERT INTO `t_mall_good_sku` ( `good_id`, `attr_ids`, `value_ids`, `inventory`, `price`, `original_price`) VALUES (?,?,?,?,?,?)");
					pst.setObject(1, goodId);
					pst.setObject(2, attrId);
					pst.setObject(3, valueId);
					pst.setObject(4, skuInventory);
					pst.setObject(5, skuPrice);
					pst.setObject(6, skuOriginalPrice);
					n = pst.executeUpdate();
					if (n == 0) {
						pst.close();
						throw new InteractRuntimeException("操作失败");
					}
					pst.close();
				}
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

	@RequestMapping(value = "/batchoffsale")
	public void batchOffsale(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String goodIdsParam = StringUtils.trimToNull(request.getParameter("good_ids"));
			String[] goodIds = null;
			if (goodIdsParam != null) {
				goodIds = goodIdsParam.split(",");
				if (goodIds.length > 100)
					throw new InteractRuntimeException("最多选择100个商品");
			}
			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();

			if (goodIds != null && goodIds.length > 0) {
				StringBuilder sb = new StringBuilder("update t_mall_good set onsale='0' where id in (");
				for (int i = 0; i < goodIds.length; i++) {
					if (i == goodIds.length - 1)
						sb.append("?");
					else
						sb.append("?,");
				}
				sb.append(")");
				pst = connection.prepareStatement(sb.toString());
				for (int j = 0; j < goodIds.length; j++) {
					pst.setObject(j + 1, goodIds[j]);
				}
				pst.executeUpdate();
			}
			// 返回结果
			JSONObject data = new JSONObject();
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

	@RequestMapping(value = "/batchonsale")
	public void batchOnsale(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String goodIdsParam = StringUtils.trimToNull(request.getParameter("good_ids"));
			String[] goodIds = null;
			if (goodIdsParam != null) {
				goodIds = goodIdsParam.split(",");
				if (goodIds.length > 100)
					throw new InteractRuntimeException("最多选择100个商品");
			}
			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();

			if (goodIds != null && goodIds.length > 0) {
				StringBuilder sb = new StringBuilder("update t_mall_good set onsale='1' where id in (");
				for (int i = 0; i < goodIds.length; i++) {
					if (i == goodIds.length - 1)
						sb.append("?");
					else
						sb.append("?,");
				}
				sb.append(")");
				pst = connection.prepareStatement(sb.toString());
				for (int j = 0; j < goodIds.length; j++) {
					pst.setObject(j + 1, goodIds[j]);
				}
				pst.executeUpdate();
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

	@RequestMapping(value = "/delgood")
	public void delGood(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String goodId = StringUtils.trimToNull(request.getParameter("good_id"));
			if (goodId == null)
				throw new InteractRuntimeException("good_id 不能空");

			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			pst = connection.prepareStatement("delete from t_mall_good where id=?");
			pst.setObject(1, goodId);
			int n = pst.executeUpdate();
			pst.close();
			if (n == 0)
				throw new InteractRuntimeException("商品不存在");
			else if (n > 1)
				throw new InteractRuntimeException("操作失败");

			pst = connection.prepareStatement("delete from t_mall_good_attr where good_id=?");
			pst.setObject(1, goodId);
			pst.executeUpdate();
			pst.close();

			pst = connection.prepareStatement("delete from t_mall_good_sku where good_id=?");
			pst.setObject(1, goodId);
			pst.executeUpdate();
			pst.close();

			pst = connection.prepareStatement(
					"delete t from t_mall_good_attr_value t left join t_mall_good_attr u on t.attr_id=u.id where isnull(u.id) or length(u.id)=0");
			pst.executeUpdate();
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

	@RequestMapping(value = "/delsku")
	public void delSku(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String goodId = StringUtils.trimToNull(request.getParameter("good_id"));
			if (goodId == null)
				throw new InteractRuntimeException("good_id 不能空");
			String skuIdParam = StringUtils.trimToNull(request.getParameter("sku_id"));
			if (skuIdParam == null)
				throw new InteractRuntimeException("sku_id 不能空");
			int skuId = Integer.parseInt(skuIdParam);

			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);

			pst = connection.prepareStatement("select count(id) from t_mall_good_sku where good_id=? for update");
			pst.setObject(1, goodId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int cnt = rs.getInt(1);
				if (cnt == 1)
					throw new InteractRuntimeException("至少保留一个规格");
			}
			pst = connection.prepareStatement(
					"delete t from t_mall_good_attr_value t inner join t_mall_good_sku u on u.value_ids=t.id where u.id=?");
			pst.setObject(1, skuId);
			int n = pst.executeUpdate();
			pst.close();
			if (n == 0)
				throw new InteractRuntimeException("sku不存在");
			else if (n > 1)
				throw new InteractRuntimeException("操作失败");
			pst = connection.prepareStatement("delete from t_mall_good_sku where id=?");
			pst.setObject(1, skuId);
			n = pst.executeUpdate();
			pst.close();
			if (n == 0)
				throw new InteractRuntimeException("sku不存在");
			else if (n > 1)
				throw new InteractRuntimeException("操作失败");

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

	@RequestMapping(value = "/altersku")
	public void alterSku(@PathVariable("mallId") String mallId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String goodId = StringUtils.trimToNull(request.getParameter("good_id"));
			if (goodId == null)
				throw new InteractRuntimeException("good_id 不能空");
			String skuIdParam = StringUtils.trimToNull(request.getParameter("sku_id"));
			if (skuIdParam == null)
				throw new InteractRuntimeException("sku_id 不能空");
			int skuId = Integer.parseInt(skuIdParam);
			String inventoryParam = StringUtils.trimToNull(request.getParameter("inventory"));
			Integer inventory = inventoryParam == null ? null : Integer.parseInt(inventoryParam);
			if (inventory != null && inventory < 0)
				throw new InteractRuntimeException("库存值有误");
			String priceParam = StringUtils.trimToNull(request.getParameter("price"));
			Integer price = priceParam == null ? null : Integer.parseInt(priceParam);
			if (price != null && price <= 0)
				throw new InteractRuntimeException("价格有误");
			String originalPriceParam = StringUtils.trimToNull(request.getParameter("original_price"));
			Integer originalPrice = originalPriceParam == null ? null : Integer.parseInt(originalPriceParam);
			if (originalPrice != null && originalPrice <= 0)
				throw new InteractRuntimeException("原价有误");
			String name = StringUtils.trimToNull(request.getParameter("name"));

			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			List sqlParams = new ArrayList();
			if (name != null)
				sqlParams.add(name);
			if (price != null)
				sqlParams.add(price);
			if (originalPrice != null)
				sqlParams.add(originalPrice);
			if (inventory != null)
				sqlParams.add(inventory);
			sqlParams.add(skuId);
			sqlParams.add(goodId);
			pst = connection.prepareStatement(new StringBuilder(
					"update `t_mall_good_attr_value` t inner join t_mall_good_sku u on t.id=u.value_ids set  ")
							.append(name == null ? "" : ",t.name=?").append(price == null ? "" : ",u.price=?")
							.append(originalPrice == null ? "" : ",u.original_price=?")
							.append(inventory == null ? "" : ",u.inventory=?").append(" where u.id=? and u.good_id=? ")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			int n = pst.executeUpdate();
			pst.close();
			if (n != 1)
				throw new InteractRuntimeException("操作失败");

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

//	@RequestMapping(value = "/addsku")
//	public void addSku(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
//			throws Exception {
//		Connection connection = null;
//		PreparedStatement pst = null;
//		try {
//			// 获取请求参数
//			String goodId = StringUtils.trimToNull(request.getParameter("good_id"));
//			if (goodId == null)
//				throw new InteractRuntimeException("good_id 不能空");
//			String inventoryParam = StringUtils.trimToNull(request.getParameter("inventory"));
//			if (inventoryParam == null)
//				throw new InteractRuntimeException("inventory 不能空");
//			Integer inventory = Integer.parseInt(inventoryParam);
//			if (inventory < 0)
//				throw new InteractRuntimeException("库存值有误");
//			String priceParam = StringUtils.trimToNull(request.getParameter("price"));
//			if (priceParam == null)
//				throw new InteractRuntimeException("price 不能空");
//			Integer price = Integer.parseInt(priceParam);
//			if (price <= 0)
//				throw new InteractRuntimeException("价格有误");
//			String originalPriceParam = StringUtils.trimToNull(request.getParameter("original_price"));
//			if (originalPriceParam == null)
//				throw new InteractRuntimeException("original_price 不能空");
//			Integer originalPrice = Integer.parseInt(originalPriceParam);
//			if (originalPrice <= 0)
//				throw new InteractRuntimeException("原价有误");
//			String name = StringUtils.trimToNull(request.getParameter("name"));
//			if (name == null)
//				throw new InteractRuntimeException("name 不能空");
//			// 业务处理
//			connection = EasywinDataSource.dataSource.getConnection();
//			connection.setAutoCommit(false);
//
//			pst = connection.prepareStatement("select id from t_mall_good_attr where good_id=? and name='规格'");
//			pst.setObject(1, goodId);
//			ResultSet rs = pst.executeQuery();
//			int attrId = 0;
//			if (rs.next()) {
//				attrId = rs.getInt(1);
//			} else
//				throw new InteractRuntimeException("规格不存在，请删除后重新添加整个商品");
//			pst.close();
//
//			pst = connection.prepareStatement(
//					new StringBuilder("insert into t_mall_good_attr_value (attr_id,name) values(?,?)").toString(),
//					Statement.RETURN_GENERATED_KEYS);
//			pst.setObject(1, attrId);
//			pst.setObject(2, name);
//			int n = pst.executeUpdate();
//			if (n != 1)
//				throw new InteractRuntimeException("操作失败");
//			rs = pst.getGeneratedKeys();
//			rs.next();
//			int attrValueId = rs.getInt(1);
//
//			List sqlParams = new ArrayList();
//			sqlParams.add(attrId);
//			sqlParams.add(price);
//			sqlParams.add(originalPrice);
//			sqlParams.add(inventory);
//			sqlParams.add(goodId);
//			pst = connection.prepareStatement(new StringBuilder(
//					"insert into t_mall_good_sku (good_id,attr_ids,value_ids,inventory,price,original_price) values()")
//							.append(name == null ? "" : ",t.name=?").append(price == null ? "" : ",u.price=?")
//							.append(originalPrice == null ? "" : ",u.original_price=?")
//							.append(inventory == null ? "" : ",u.inventory=?").append(" where u.id=? and u.good_id=? ")
//							.toString());
//			for (int i = 0; i < sqlParams.size(); i++) {
//				pst.setObject(i + 1, sqlParams.get(i));
//			}
//			int n = pst.executeUpdate();
//			pst.close();
//			if (n != 1)
//				throw new InteractRuntimeException("操作失败");
//
//			connection.commit();
//			// 返回结果
//			HttpRespondWithData.todo(request, response, 0, null, null);
//		} catch (Exception e) {
//			// 处理异常
//			logger.info(ExceptionUtils.getStackTrace(e));
//			if (connection != null)
//				connection.rollback();
//			HttpRespondWithData.exception(request, response, e);
//		} finally {
//			// 释放资源
//			if (pst != null)
//				pst.close();
//			if (connection != null)
//				connection.close();
//		}
//	}
}
