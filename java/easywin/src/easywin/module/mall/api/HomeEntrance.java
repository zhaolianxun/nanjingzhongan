package easywin.module.mall.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
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
import easywin.module.mall.business.GetLoginStatus;
import easywin.module.mall.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mall.api.HomeEntrance")
@RequestMapping(value = "/m/e/home")
public class HomeEntrance {

	public static Logger logger = Logger.getLogger(HomeEntrance.class);

	@RequestMapping(value = "")
	public void entrance(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

			UserLoginStatus loginStatus = GetLoginStatus.todo(request);

			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection
					.prepareStatement("select pic,type,link from t_mall_mainrotation where mall_id=? order by id asc");
			pst.setObject(1, mallId);
			ResultSet rs = pst.executeQuery();
			JSONArray mainrotaions = new JSONArray();
			while (rs.next()) {
				JSONObject mainrotaion = new JSONObject();
				mainrotaion.put("pic", rs.getObject(1));
				mainrotaion.put("type", rs.getObject(2));
				mainrotaion.put("link", rs.getObject(3));
				mainrotaions.add(mainrotaion);
			}
			pst.close();

			if (mainrotaions.isEmpty()) {
				JSONObject mainrotaion = new JSONObject();
				mainrotaion.put("pic", "http://passion.njshangka.com/oss/easywin/defaultmainroll.jpg");
				mainrotaion.put("type", 0);
				mainrotaion.put("link", null);
				mainrotaions.add(mainrotaion);
			}
			// 查詢公告
			pst = connection.prepareStatement(
					"select content from t_mall_notice where mall_id=? order by add_time desc limit 0,20");
			pst.setObject(1, mallId);
			rs = pst.executeQuery();
			JSONArray notices = new JSONArray();
			while (rs.next()) {
				JSONObject notice = new JSONObject();
				notice.put("content", rs.getObject(1));
				notices.add(notice);
			}
			pst.close();

			pst = connection.prepareStatement("select name from t_mall where id=?");
			pst.setObject(1, mallId);
			rs = pst.executeQuery();
			String mallName = null;
			if (rs.next()) {
				mallName = rs.getString("name");
			}
			pst.close();
			if (notices.isEmpty()) {
				JSONObject notice = new JSONObject();
				notice.put("content", "欢迎使用" + mallName);
				notices.add(notice);
			}

			// 查询所有商品
			pst = connection.prepareStatement(
					"select t.id goodId,t.cover,t.name,(select min(price) from t_mall_good_sku where good_id=t.id) price,(select min(original_price) from t_mall_good_sku where good_id=t.id) originalPrice,t.saled_count saledCount from t_mall_good t where t.onsale=1 and t.mall_id=? order by t.add_time desc limit ?,?");
			pst.setObject(1, mallId);
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
			rs = pst.executeQuery();
			JSONArray goods = new JSONArray();
			while (rs.next()) {
				JSONObject good = new JSONObject();
				good.put("goodId", rs.getObject(1));
				good.put("cover", rs.getObject(2));
				good.put("name", rs.getObject(3));
				good.put("price", rs.getObject(4));
				good.put("originalPrice", rs.getObject(5));
				good.put("saledCount", rs.getObject(6));
				goods.add(good);
			}
			pst.close();

			
			// 查询优惠券
			List sqlParams = new ArrayList();
			if (loginStatus != null) {
				sqlParams.add(loginStatus.getUserId());
				sqlParams.add(loginStatus.getUserId());
			}
			sqlParams.add(mallId);
			pst = connection.prepareStatement(new StringBuilder(
					"select t.id,t.title,t.desc,t.type,t.type1_uptomoney,t.type1_submoney,t.type1_starttime,t.type1_endtime,")
							.append(loginStatus == null ? " 0 getted"
									: " (select if(count(id)>0,1,0) from t_mall_usercoupon where coupon_id=t.id and user_id=?) getted")
							.append(" ,")
							.append(loginStatus == null ? " 0 used"
									: " (select ifnull(used,0) from t_mall_usercoupon where coupon_id=t.id and user_id=?) used")
							.append(" from t_mall_coupon t left join t_mall_usercoupon tt on t.id=tt.coupon_id where t.mall_id=? order by t.add_time desc limit 0,30")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));
			rs = pst.executeQuery();
			JSONArray coupons = new JSONArray();
			while (rs.next()) {
				JSONObject coupon = new JSONObject();
				coupon.put("title", rs.getObject("title"));
				coupon.put("couponId", rs.getObject("id"));
				coupon.put("desc", rs.getObject("desc"));
				coupon.put("type", rs.getObject("type"));
				coupon.put("type1Starttime", rs.getObject("type1_starttime"));
				coupon.put("type1Endtime", rs.getObject("type1_endtime"));
				coupon.put("type1Uptomoney", rs.getObject("type1_uptomoney"));
				coupon.put("type1Submoney", rs.getObject("type1_submoney"));
				coupon.put("used", rs.getObject("used"));
				coupon.put("getted", rs.getObject("getted"));
				coupons.add(coupon);
			}
			pst.close();
			// 返回结果
			JSONObject data = new JSONObject();
			data.put("goodStyle", 1);
			data.put("goods", goods);
			data.put("notices", notices);
			data.put("mainrotaions", mainrotaions);
			data.put("coupons", coupons);
			data.put("mallName", mallName);
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

	@RequestMapping(value = "paging")
	public void paging(HttpServletRequest request, HttpServletResponse response) throws Exception {
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

			// 业务处理
			connection = EasywinDataSource.dataSource.getConnection();

			// 查询所有商品
			pst = connection.prepareStatement(
					"select t.id goodId,t.cover,t.name,(select min(price) from t_mall_good_sku where good_id=t.id) price,(select min(original_price) from t_mall_good_sku where good_id=t.id) originalPrice,t.saled_count saledCount from t_mall_good t where t.onsale=1 and t.mall_id=? order by t.add_time desc limit ?,?");
			pst.setObject(1, mallId);
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray goods = new JSONArray();
			while (rs.next()) {
				JSONObject good = new JSONObject();
				good.put("goodId", rs.getObject(1));
				good.put("cover", rs.getObject(2));
				good.put("name", rs.getObject(3));
				good.put("price", rs.getObject(4));
				good.put("originalPrice", rs.getObject(5));
				good.put("saledCount", rs.getObject(6));
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

	@RequestMapping(value = "/getcoupon")
	public void getcoupon(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mallId = StringUtils.trimToNull(request.getParameter("mall_id"));
			if (mallId == null)
				throw new InteractRuntimeException("mall_id不可空");
			String couponId = StringUtils.trimToNull(request.getParameter("coupon_id"));
			if (couponId == null)
				throw new InteractRuntimeException("coupon_id不可空");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = EasywinDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(
					"select t.type,t.type1_uptomoney,t.type1_submoney,t.type1_starttime,t.type1_endtime,(select if(count(id)>0,1,0) from t_mall_usercoupon where coupon_id=t.id and user_id=?) getted from t_mall_coupon t where t.id=?");
			pst.setObject(1, loginStatus.getUserId());
			pst.setObject(2, couponId);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				int type = rs.getInt("type");
				int getted = rs.getInt("getted");
				if (getted == 1)
					throw new InteractRuntimeException("您已经领取");
				long type1Starttime = rs.getLong("type1_starttime");
				long type1Endtime = rs.getLong("type1_endtime");
				if (type == 1) {
					if (new Date().getTime() > type1Endtime)
						throw new InteractRuntimeException("已结束");
					if (new Date().getTime() < type1Starttime)
						throw new InteractRuntimeException("未开始");
				}
			} else
				throw new InteractRuntimeException("卡券不存在");

			pst.close();

			pst = connection.prepareStatement(
					"INSERT INTO `t_mall_usercoupon` ( `mall_id`, `user_id`, `coupon_id`, `get_time`) VALUES ( ?, ?, ?, ?)");
			pst.setObject(1, mallId);
			pst.setObject(2, loginStatus.getUserId());
			pst.setObject(3, couponId);
			pst.setObject(4, new Date().getTime());
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
