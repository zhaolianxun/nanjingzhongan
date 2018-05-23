package easywin.module.mall.api;

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
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mall.api.GoodTypeEntrance")
@RequestMapping(value = "/m/e/goodtype")
public class GoodTypeEntrance {

	public static Logger logger = Logger.getLogger(GoodTypeEntrance.class);

	@RequestMapping(value = "")
	public void goodTypeEntrance(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
				throw new InteractRuntimeException("mall_id不可空");

			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select id typeId,name from t_mall_good_type where mall_id=? and level=1 order by id asc");
			pst.setObject(1, mallId);
			ResultSet rs = pst.executeQuery();
			JSONArray type1s = new JSONArray();
			while (rs.next()) {
				JSONObject type1 = new JSONObject();
				type1.put("typeId", rs.getObject(1));
				type1.put("name", rs.getObject(2));
				type1s.add(type1);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("type1s", type1s);
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

	@RequestMapping(value = "/type2s")
	public void type2s(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
				throw new InteractRuntimeException("mall_id不可空");
			String type1IdParam = request.getParameter("type1_id");
			if (type1IdParam == null || type1IdParam.trim() == "")
				throw new InteractRuntimeException("type1_id不可空");
			int type1Id = Integer.parseInt(type1IdParam);
			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select id,name,cover from t_mall_good_type where mall_id=? and level=2 and upid=? order by id asc");
			pst.setObject(1, mallId);
			pst.setObject(2, type1Id);
			ResultSet rs = pst.executeQuery();
			JSONArray type2s = new JSONArray();
			while (rs.next()) {
				JSONObject type2 = new JSONObject();
				type2.put("typeId", rs.getObject(1));
				type2.put("name", rs.getObject(2));
				type2.put("cover", rs.getObject(3));
				type2s.add(type2);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("type2s", type2s);
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

	@RequestMapping(value = "/typegoods")
	public void typeGoods(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
				throw new InteractRuntimeException("mall_id不可空");
			String type1IdParam = StringUtils.trimToNull(request.getParameter("type1_id"));
			Integer type1Id = type1IdParam == null ? null : Integer.parseInt(type1IdParam);
			String type2IdParam = StringUtils.trimToNull(request.getParameter("type2_id"));
			Integer type2Id = type2IdParam == null ? null : Integer.parseInt(type2IdParam);
			String type3IdParam = StringUtils.trimToNull(request.getParameter("type3_id"));
			Integer type3Id = type3IdParam == null ? null : Integer.parseInt(type3IdParam);

			String sortParam = StringUtils.trimToNull(request.getParameter("sort"));
			int sort = sortParam == null ? 0 : Integer.parseInt(sortParam);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");
			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();

			// 查询所有商品
			List sqlParams = new ArrayList();
			sqlParams.add(mallId);
			if (type1Id != null)
				sqlParams.add(type1Id);
			if (type2Id != null)
				sqlParams.add(type2Id);
			if (type3Id != null)
				sqlParams.add(type3Id);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);

			pst = connection.prepareStatement(
					"select * from (select t.id,t.add_time,t.cover,t.name,(select min(price) from t_mall_good_sku where good_id=t.id) price,(select min(original_price) from t_mall_good_sku where good_id=t.id) original_price,t.saled_count from t_mall_good t where t.onsale=1 and t.mall_id=?"
							+ (type1Id == null ? "" : " and t.type1=? ") + (type2Id == null ? "" : " and t.type2=? ")
							+ (type3Id == null ? "" : " and t.type3=? ") + " ) tt"
							+ (sort == 1 ? " order by tt.saled_count desc"
									: (sort == 2 ? " order by tt.price desc"
											: (sort == 3 ? " order by tt.price asc" : " order by tt.add_time desc")))
							+ " limit ?,?");
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			ResultSet rs = pst.executeQuery();
			JSONArray goods = new JSONArray();
			while (rs.next()) {
				JSONObject good = new JSONObject();
				good.put("goodId", rs.getObject("id"));
				good.put("cover", rs.getObject("cover"));
				good.put("name", rs.getObject("name"));
				good.put("price", rs.getObject("price"));
				good.put("originalPrice", rs.getObject("original_price"));
				good.put("saledCount", rs.getObject("saled_count"));
				goods.add(good);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("goods", goods);
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
