package rrightway.module.mallmanage.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.RrightwayDataSource;
import rrightway.util.HttpRespondWithData;

@Controller("mallManage.api.MallBaseInfoEntrance")
@RequestMapping(value = "/mm/{mallId}/e/baseinfo")
public class MallBaseInfoEntrance {

	public static Logger logger = Logger.getLogger(MallBaseInfoEntrance.class);

	@RequestMapping(value = "/get")
	public void get(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select good_show_style,name,headimg,service_type,enter_time from t_mall where id=?");
			pst.setObject(1, mallId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("goodShowStyle", rs.getObject("good_show_style"));
				item.put("name", rs.getObject("name"));
				item.put("headimg", rs.getObject("headimg"));
				item.put("serviceType", rs.getObject("service_type"));
				item.put("enterTime", rs.getObject("enter_time"));
			} else {
				throw new InteractRuntimeException("商城不存在");
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.putAll(item);
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
	public void alter(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String goodShowStyleParam = StringUtils.trimToNull(request.getParameter("good_show_style"));
			Integer goodShowStyle = goodShowStyleParam == null ? null : Integer.parseInt(goodShowStyleParam);
			String name = StringUtils.trimToNull(request.getParameter("name"));
			String headimg = StringUtils.trimToNull(request.getParameter("headimg"));
			String serviceType = StringUtils.trimToNull(request.getParameter("service_type"));

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢商品详情
			List sqlParams = new ArrayList();
			boolean conditionHas = false;
			if (goodShowStyle != null)
				sqlParams.add(goodShowStyle);
			if (name != null)
				sqlParams.add(name);
			if (headimg != null)
				sqlParams.add(headimg);
			if (serviceType != null)
				sqlParams.add(serviceType);
			if (!sqlParams.isEmpty())
				conditionHas = true;
			sqlParams.add(mallId);
			if (conditionHas) {
				StringBuilder sql = new StringBuilder("update t_mall set ");
				StringBuilder sql1 = new StringBuilder()
						.append(goodShowStyleParam == null ? "" : ",`good_show_style`=?")
						.append(name == null ? "" : ",`name`=?").append(headimg == null ? "" : ",`headimg`=?")
						.append(serviceType == null ? "" : ",`service_type`=?");
				sql.append(sql1.substring(1)).append(" where id=?");
				pst = connection.prepareStatement(sql.toString());
				for (int i = 0; i < sqlParams.size(); i++) {
					pst.setObject(i + 1, sqlParams.get(i));
				}

				int n = pst.executeUpdate();
				pst.close();
				if (n == 0) {
					throw new InteractRuntimeException("商城不存在");
				} else if (n > 1) {
					throw new InteractRuntimeException("操作失败,发现多条记录");
				}
				pst.close();
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

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查詢商品详情
			pst = connection.prepareStatement(
					"INSERT INTO `t_mall_good` (`id`, `mall_id`, `name`, `tags`, `type1`, `type1_name`, `type2`, `type2_name`,  `onsale`, `detail`, `params`, `detail_pics`, `cover`, `add_time`,`base_alter_time`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
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

			connection = RrightwayDataSource.dataSource.getConnection();
			connection.setAutoCommit(false);
			// 查詢商品详情
			boolean baseAlter = false;
			boolean sqlParamsEmpty = false;
			List sqlParams = new ArrayList();
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
			sqlParamsEmpty = sqlParams.isEmpty();
			sqlParams.add(goodId);
			if (!sqlParamsEmpty) {
				String s = (name == null ? "" : " ,`name`=?") + (tags == null ? "" : " ,`tags`=?")
						+ (type1 == null ? "" : " ,`type1`=?") + (type1Name == null ? "" : " ,`type1_name`=?")
						+ (type2 == null ? "" : " ,`type2`=?") + (type2Name == null ? "" : " ,`type2_name`=?")
						+ (onsale == null ? "" : " ,`onsale`=?") + (detail == null ? "" : " ,`detail`=?")
						+ (params == null ? "" : " ,`params`=?") + (detailPics == null ? "" : " ,`detail_pics`=?")
						+ (cover == null ? "" : " ,`cover`=?") + (!baseAlter ? "" : " ,`base_alter_time`=?");
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
					boolean sqlParams1Empty = false;
					List sqlParams1 = new ArrayList();
					if (skuInventory != null)
						sqlParams1.add(skuInventory);
					if (skuPrice != null)
						sqlParams1.add(skuPrice);
					if (skuOriginalPrice != null)
						sqlParams1.add(skuOriginalPrice);
					sqlParams1Empty = sqlParams1.isEmpty();
					sqlParams1.add(skuId);
					if (!sqlParams1Empty) {
						String ss = (skuInventory == null ? "" : " ,inventory=?")
								+ (skuPrice == null ? "" : " ,price=?")
								+ (skuOriginalPrice == null ? "" : " ,original_price=?");
						ss = ss.substring(2);
						pst = connection.prepareStatement("update `t_mall_good_sku` set " + ss + " where id=?");
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
			connection = RrightwayDataSource.dataSource.getConnection();

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
			connection = RrightwayDataSource.dataSource.getConnection();

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
			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection
					.prepareStatement("update t_mall_good set mall_del_time=? and mall_del=? where id=? and mall_id=?");
			pst.setObject(1, new Date().getTime());
			pst.setObject(2, 1);
			pst.setObject(3, goodId);
			pst.setObject(4, mallId);
			int n = pst.executeUpdate();
			pst.close();
			if (n == 0)
				throw new InteractRuntimeException("商品不存在");
			else if (n > 1)
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
			String skuId = StringUtils.trimToNull(request.getParameter("sku_id"));
			if (skuId == null)
				throw new InteractRuntimeException("sku_id 不能空");
			// 业务处理
			connection = RrightwayDataSource.dataSource.getConnection();
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
}
