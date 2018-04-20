package easywin.module.mall.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
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

@Controller("mall.api.HomeSearchEntrance")
@RequestMapping(value = "/m/e/homesearch")
public class HomeSearchEntrance {

	public static Logger logger = Logger.getLogger(HomeSearchEntrance.class);

	@RequestMapping(value = "/search")
	public void search(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String key = StringUtils.trimToNull(request.getParameter("key"));
			if (key != null && key.length() > 100)
				throw new InteractRuntimeException("key长度不能大于100");
			String mallId = request.getParameter("mall_id");
			if (mallId == null || mallId.trim() == "")
				throw new InteractRuntimeException("mall_id不可空");
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
			if (key != null)
				sqlParams.add(new StringBuilder("%").append(key).append("%").toString());
			sqlParams.add(pageSize * (pageNo - 1));
			sqlParams.add(pageSize);

			pst = connection.prepareStatement(
					new StringBuilder("select * from (select t.id,t.add_time,t.cover,t.name,(select min(price) from t_mall_good_sku where good_id=t.id) price,(select min(original_price) from t_mall_good_sku where good_id=t.id) original_price,t.saled_count from t_mall_good t where t.mall_id=?")
				.append(key == null ? "" : " and t.name like ?").append(") tt").append(sort == 1 ? " order by tt.saled_count desc"
						: (sort == 2 ? " order by tt.price desc"
								: (sort == 3 ? " order by tt.price asc" : " order by tt.add_time desc")))
							.append(" limit ?,?").toString());
			for (int i = 0; i < sqlParams.size(); i++)
				pst.setObject(i + 1, sqlParams.get(i));

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
