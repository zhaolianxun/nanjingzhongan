package easywin.module.mallmanage.api;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;

import easywin.constant.SysConstant;
import easywin.entity.InteractRuntimeException;
import easywin.module.plat.business.GetLoginStatus;
import easywin.module.plat.entity.UserLoginStatus;
import easywin.util.EasywinDataSource;
import easywin.util.HttpRespondWithData;

@Controller("mallManage.api.MallWxPayInfoEntrance")
@RequestMapping(value = "/mm/{mallId}/e/wxpayinfo")
public class MallWxPayInfoEntrance {

	public static Logger logger = Logger.getLogger(MallWxPayInfoEntrance.class);

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

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢主轮播图
			pst = connection.prepareStatement(
					"select reverse(substring_index(reverse(replace(wx_mchcertpath,'`\\`','/')),'/',1)) wx_mchcert_filename,wx_mchid,wx_mchkey,if(ISNULL(wx_mchcertpath) || LENGTH(trim(wx_mchcertpath))<1,0,1) mchcert_setted from t_app where id=?");
			pst.setObject(1, mallId);
			ResultSet rs = pst.executeQuery();
			JSONObject item = new JSONObject();
			if (rs.next()) {
				item.put("mchid", rs.getObject("wx_mchid"));
				item.put("mchkey", rs.getObject("wx_mchkey"));
				item.put("mchcertSetted", rs.getObject("mchcert_setted"));
				item.put("mchcertFilename", rs.getObject("wx_mchcert_filename"));
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

	@RequestMapping(value = "/set")
	public void alter(@PathVariable("mallId") String mallId, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		Connection connection = null;
		PreparedStatement pst = null;
		try {
			// 获取请求参数
			String mchid = StringUtils.trimToNull(request.getParameter("mchid"));
			String mchkey = StringUtils.trimToNull(request.getParameter("mchkey"));
			String token = StringUtils.trimToNull(request.getParameter("token"));
			FileItem mchcert = null;

			// 业务处理
			DiskFileItemFactory f = new DiskFileItemFactory();// 磁盘对象
			f.setRepository(new File(SysConstant.project_ossroot)); // 设置临时目录
			f.setSizeThreshold(1024 * 8); // 8k的缓冲区,文件大于8K则保存到临时目录
			ServletFileUpload upload = new ServletFileUpload(f);// 声明解析request的对象
			upload.setHeaderEncoding("UTF-8"); // 处理文件名中文
			upload.setFileSizeMax(1024 * 1024 * 5);// 设置每个文件最大为5M
			upload.setSizeMax(1024 * 1024 * 10);// 一共最多能上传10M

			List<FileItem> list = upload.parseRequest(request);// 解析
			for (FileItem ff : list) {
				if (ff.getFieldName().equals("token"))
					token = StringUtils.trimToNull(ff.getString());
				if (ff.getFieldName().equals("mchid"))
					mchid = StringUtils.trimToNull(ff.getString("UTF-8"));
				if (ff.getFieldName().equals("mchkey"))
					mchkey = StringUtils.trimToNull(ff.getString("UTF-8"));
				if (ff.getFieldName().equals("mchcert")) {
					if (ff.isFormField())
						throw new InteractRuntimeException("mchcert 必须是文件");
					mchcert = ff;
				}
			}

			UserLoginStatus loginStatus = GetLoginStatus.todo(token);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (mchid == null)
				throw new InteractRuntimeException("mchid 不能空");
			if (mchkey == null)
				throw new InteractRuntimeException("mchkey 不能空");
			String mchcertPath = null;
			if (mchcert != null) {
				// 保存文件
				File path = new File(SysConstant.project_ossroot, mallId);// 获取文件要保存的目录
				if (!path.exists())
					path.mkdirs();
				String ss = mchcert.getName().substring(mchcert.getName().lastIndexOf("\\") + 1);// 解析文件名
				File mchcertSave = new File(path, new String(ss.getBytes(), "utf-8"));
				FileUtils.copyInputStreamToFile( // 直接使用commons.io.FileUtils
						mchcert.getInputStream(), mchcertSave);
				mchcertPath = mchcertSave.getAbsolutePath();
			}

			connection = EasywinDataSource.dataSource.getConnection();
			// 查詢商品详情
			List sqlParams = new ArrayList();
			sqlParams.add(mchid);
			sqlParams.add(mchkey);
			sqlParams.add(mchcertPath);
			sqlParams.add(mallId);
			pst = connection.prepareStatement(
					new StringBuilder("update t_app set wx_mchid=?,wx_mchkey=?,wx_mchcertpath=? where id=?")
							.toString());
			for (int i = 0; i < sqlParams.size(); i++) {
				pst.setObject(i + 1, sqlParams.get(i));
			}
			int n = pst.executeUpdate();
			pst.close();
			if (n == 0)
				throw new InteractRuntimeException("商城不存在");

			// 更新‘必要信息’是否补全的标致（businessbase_fill）
			pst = connection.prepareStatement(
					"update t_app u inner join t_mall t on u.id=t.id set u.businessbase_fill=(case when (isnull(t.name)||length(trim(t.name))=0)||(isnull(u.wx_mchid)||length(trim(u.wx_mchid))=0)||(isnull(u.wx_mchkey)||length(trim(u.wx_mchkey))=0)||(isnull(u.wx_mchcertpath)||length(trim(u.wx_mchcertpath))=0) then 0 else 1 end) where u.id=?");
			pst.setObject(1, mallId);
			n = pst.executeUpdate();
			if (n == 0)
				throw new InteractRuntimeException("操作失败");
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
