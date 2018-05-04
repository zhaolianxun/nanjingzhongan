package rrightway.module.plat.api.mobile;

import java.math.BigDecimal;
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

import rrightway.entity.InteractRuntimeException;
import rrightway.util.HttpRespondWithData;
import rrightway.util.RrightwayDataSource;

@Controller("plat.api.mobile.SearchEntrance")
@RequestMapping(value = "/p/m/searche")
public class SearchEntrance {

	public static Logger logger = Logger.getLogger(SearchEntrance.class);

	@RequestMapping(value = "/search")
	public void home(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String keyw = StringUtils.trimToNull(request.getParameter("keyw"));
			String type2idParam = StringUtils.trimToNull(request.getParameter("type2_id"));
			Integer type2Id = type2idParam == null ? null : Integer.parseInt(type2idParam);
			String type1idParam = StringUtils.trimToNull(request.getParameter("type1_id"));
			Integer type1Id = type1idParam == null ? null : Integer.parseInt(type1idParam);
			String couponIfParam = StringUtils.trimToNull(request.getParameter("coupon_if"));
			Integer couponIf = couponIfParam == null ? null : Integer.parseInt(couponIfParam);
			String wayToShopParam = StringUtils.trimToNull(request.getParameter("way_to_shop"));
			Integer wayToShop = wayToShopParam == null ? null : Integer.parseInt(wayToShopParam);
			String buyerMincreditMinParam = StringUtils.trimToNull(request.getParameter("buyer_mincredit_min"));
			Integer buyerMincreditMin = buyerMincreditMinParam == null ? null
					: Integer.parseInt(buyerMincreditMinParam);
			String payPriceMinParam = StringUtils.trimToNull(request.getParameter("pay_price_min"));
			BigDecimal payPriceMin = payPriceMinParam == null ? null : new BigDecimal(payPriceMinParam);
			String payPriceMaxParam = StringUtils.trimToNull(request.getParameter("pay_price_max"));
			BigDecimal payPriceMax = payPriceMaxParam == null ? null : new BigDecimal(payPriceMaxParam);
			// 1综合 2销量 3最新 4付款金额升序 5付款金额降序 6奖金升序 7奖金降序
			String sortbyParam = StringUtils.trimToNull(request.getParameter("sortby"));
			int sortby = sortbyParam == null ? 1 : Integer.parseInt(sortbyParam);
			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = pageNoParam == null ? 1 : Long.parseLong(pageNoParam);
			if (pageNo <= 0)
				throw new InteractRuntimeException("page_no有误");
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			int pageSize = pageSizeParam == null ? 30 : Integer.parseInt(pageSizeParam);
			if (pageSize <= 0)
				throw new InteractRuntimeException("page_size有误");
			// 业务处理

			List sqlParams = new ArrayList();
			if (keyw != null) {
				keyw = new StringBuilder("%").append(keyw).append("%").toString();
				sqlParams.add(keyw);
			}
			if (type1Id != null)
				sqlParams.add(type1Id);
			if (type2Id != null)
				sqlParams.add(type2Id);
			if (wayToShop != null)
				sqlParams.add(wayToShop);
			if (buyerMincreditMin != null)
				sqlParams.add(buyerMincreditMin);
			if (payPriceMin != null)
				sqlParams.add(payPriceMin);
			if (payPriceMax != null)
				sqlParams.add(payPriceMax);
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);
			connection = RrightwayDataSource.dataSource.getConnection();

			pst = connection.prepareStatement(new StringBuilder(
					"select id,gift_cover,gift_name,pay_price,return_money,buyer_num from t_activity where buy_way=2 ")
							.append(keyw == null ? "" : " and gift_name like ? ")
							.append(type1Id == null ? "" : " and gift_type1_id=? ")
							.append(type2Id == null ? "" : " and gift_type2_id=? ")
							.append(wayToShop == null ? "" : " and way_to_shop=? ")
							.append(buyerMincreditMin == null ? "" : " and buyer_mincredit >= ? ")
							.append(payPriceMin == null ? "" : " and pay_price >= ? ")
							.append(payPriceMax == null ? "" : " and pay_price <= ? ")
							.append(sortby == 1 || sortby == 2 ? " order by buyer_num desc "
									: sortby == 3 ? " order by publish_time desc "
											: sortby == 4 ? " order by pay_price asc "
													: sortby == 5 ? " order by pay_price desc "
															: sortby == 6 ? " order by return_money-pay_price asc "
																	: sortby == 7
																			? " order by return_money-pay_price desc "
																			: " order by buyer_num desc ")
							.append(" limit ?,? ").toString());
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getInt("id"));
				item.put("giftCover", rs.getString("gift_cover"));
				item.put("giftName", rs.getString("gift_name"));
				item.put("payPrice", rs.getBigDecimal("pay_price"));
				item.put("returnMoney", rs.getBigDecimal("return_money"));
				item.put("buyerNum", rs.getInt("buyer_num"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject activities = new JSONObject();
			activities.put("items", items);
			data.put("activities", activities);
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