package rrightway.module.mallmanage.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

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

import rrightway.entity.InteractRuntimeException;
import rrightway.module.plat.business.GetLoginStatus;
import rrightway.module.plat.entity.UserLoginStatus;
import rrightway.util.RrightwayDataSource;
import rrightway.util.HttpRespondWithData;

@Controller("mallManage.api.NoticeManageEntrance")
@RequestMapping(value = "/mm/{mallId}/e/notice")
public class NoticeManageEntrance {

	public static Logger logger = Logger.getLogger(NoticeManageEntrance.class);

	@RequestMapping(value = "/list")
	public void list(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
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

			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select id,content from t_mall_notice where mall_id=? order by add_time desc limit ?,?");
			pst.setObject(1, mallId);
			pst.setObject(2, pageSize * (pageNo - 1));
			pst.setObject(3, pageSize);
			ResultSet rs = pst.executeQuery();
			JSONArray items = new JSONArray();
			while (rs.next()) {
				JSONObject item = new JSONObject();
				item.put("id", rs.getObject("id"));
				item.put("content", rs.getObject("content"));
				items.add(item);
			}
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject notices = new JSONObject();
			notices.put("items", items);
			data.put("notices", notices);
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
	public void add(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String content = StringUtils.trimToNull(request.getParameter("content"));
			if (content == null)
				throw new InteractRuntimeException("content 不可空");
			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢商品详情
			pst = connection.prepareStatement("insert into t_mall_notice (mall_id,content,add_time) values(?,?,?)",
					PreparedStatement.RETURN_GENERATED_KEYS);

			pst.setObject(1, mallId);
			pst.setObject(2, content);
			pst.setObject(3, new Date().getTime());

			int n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");
			ResultSet rs = pst.getGeneratedKeys();
			int id = rs.getInt(1);
			pst.close();

			// 返回结果
			JSONObject data = new JSONObject();
			data.put("noticeId", id);
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

	@RequestMapping(value = "/del/{noticeId}")
	public void del(@PathVariable("mallId") String mallId, @PathVariable("noticeId") String noticeId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			connection = RrightwayDataSource.dataSource.getConnection();
			// 查詢商品详情
			pst = connection.prepareStatement("delete from t_mall_notice where id=?");
			pst.setObject(1, noticeId);

			int n = pst.executeUpdate();
			pst.close();
			if (n == 0) {
				throw new InteractRuntimeException("目标不存在");
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
