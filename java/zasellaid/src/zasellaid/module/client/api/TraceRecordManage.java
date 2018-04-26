package zasellaid.module.client.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;

import zasellaid.constant.SysConstant;
import zasellaid.dao.entity.ContactHospital;
import zasellaid.dao.entity.TraceRecord;
import zasellaid.dao.mapper.ContactHospitalMapper;
import zasellaid.dao.mapper.TraceRecordMapper;
import zasellaid.entity.InteractRuntimeException;
import zasellaid.entity.PagingPage;
import zasellaid.module.client.business.GetLoginStatus;
import zasellaid.module.client.dao.mapper.TraceRecordManageMapper;
import zasellaid.module.client.entity.UserLoginStatus;
import zasellaid.module.client.task.RefreshUserAlive;
import zasellaid.util.HttpRespondWithData;

@Controller("controller.client.TraceRecordManage")
@RequestMapping(value = "/c/tr")
public class TraceRecordManage {

	public static Logger logger = Logger.getLogger(TraceRecordManage.class);

	@RequestMapping(value = "/addtext", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void addText(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String contactHospitalId = StringUtils.trimToNull(request.getParameter("contact_hospital_id"));
			if (contactHospitalId == null)
				throw new InteractRuntimeException("contact_hospital_id can`t be empty.");

			String content = StringUtils.trimToNull(request.getParameter("content"));
			if (content == null)
				throw new InteractRuntimeException("content can`t be empty.");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			session = SysConstant.sqlSessionFactory.openSession(false);
			ContactHospitalMapper contactHospitalMapper = session.getMapper(ContactHospitalMapper.class);
			TraceRecordMapper traceRecordMapper = session.getMapper(TraceRecordMapper.class);

			ContactHospital contactHospital = contactHospitalMapper.selectByPrimaryKey(contactHospitalId);
			if (contactHospital == null)
				throw new InteractRuntimeException("联系医院不存在");
			if (!contactHospital.getClientUserId().equals(loginStatus.getUserId()))
				throw new InteractRuntimeException("不是您的联系医院");

			contactHospital.setTraceStatus(1);
			contactHospital.setLastTraceTime(new Date().getTime());
			contactHospitalMapper.updateByPrimaryKeySelective(contactHospital);

			TraceRecord traceRecord = new TraceRecord();
			String traceRecordId = RandomStringUtils.randomNumeric(12);
			traceRecord.setId(traceRecordId);
			traceRecord.setType(1);
			traceRecord.setUserId(loginStatus.getUserId());
			traceRecord.setContent(content);
			traceRecord.setTraceTime(new Date().getTime());
			traceRecord.setContactHospitalId(contactHospitalId);
			traceRecordMapper.insertSelective(traceRecord);

			session.commit();

			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("traceRecordId", traceRecordId);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (session != null)
				session.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/addvoice", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void addVoice(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String contactHospitalId = StringUtils.trimToNull(request.getParameter("contact_hospital_id"));
			if (contactHospitalId == null)
				throw new InteractRuntimeException("contact_hospital_id can`t be empty.");

			String content = StringUtils.trimToNull(request.getParameter("content"));
			if (content == null)
				throw new InteractRuntimeException("content can`t be empty.");
			String voiceDurationParam = StringUtils.trimToNull(request.getParameter("voice_duration"));
			if (voiceDurationParam == null)
				throw new InteractRuntimeException("voice_duration can`t be empty.");
			int voiceDuration = Integer.parseInt(voiceDurationParam);
			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			session = SysConstant.sqlSessionFactory.openSession(false);
			ContactHospitalMapper contactHospitalMapper = session.getMapper(ContactHospitalMapper.class);
			TraceRecordMapper traceRecordMapper = session.getMapper(TraceRecordMapper.class);

			ContactHospital contactHospital = contactHospitalMapper.selectByPrimaryKey(contactHospitalId);
			if (contactHospital == null)
				throw new InteractRuntimeException("联系医院不存在");
			if (!contactHospital.getClientUserId().equals(loginStatus.getUserId()))
				throw new InteractRuntimeException("不是您的联系医院");

			contactHospital.setTraceStatus(1);
			contactHospital.setLastTraceTime(new Date().getTime());
			contactHospitalMapper.updateByPrimaryKeySelective(contactHospital);

			TraceRecord traceRecord = new TraceRecord();
			String traceRecordId = RandomStringUtils.randomNumeric(12);
			traceRecord.setId(traceRecordId);
			traceRecord.setType(2);
			traceRecord.setUserId(loginStatus.getUserId());
			traceRecord.setContent(content);
			traceRecord.setTraceTime(new Date().getTime());
			traceRecord.setContactHospitalId(contactHospitalId);
			traceRecord.setVoiceDuration(voiceDuration);
			traceRecordMapper.insertSelective(traceRecord);

			session.commit();
			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("traceRecordId", traceRecordId);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (session != null)
				session.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/addimg", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void addImg(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// TODO 获取请求参数
			String contactHospitalId = StringUtils.trimToNull(request.getParameter("contact_hospital_id"));
			if (contactHospitalId == null)
				throw new InteractRuntimeException("contact_hospital_id can`t be empty.");

			String content = StringUtils.trimToNull(request.getParameter("content"));
			if (content == null)
				throw new InteractRuntimeException("content can`t be empty.");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			session = SysConstant.sqlSessionFactory.openSession(false);
			ContactHospitalMapper contactHospitalMapper = session.getMapper(ContactHospitalMapper.class);
			TraceRecordMapper traceRecordMapper = session.getMapper(TraceRecordMapper.class);

			ContactHospital contactHospital = contactHospitalMapper.selectByPrimaryKey(contactHospitalId);
			if (contactHospital == null)
				throw new InteractRuntimeException("联系医院不存在");
			if (!contactHospital.getClientUserId().equals(loginStatus.getUserId()))
				throw new InteractRuntimeException("不是您的联系医院");

			contactHospital.setTraceStatus(1);
			contactHospital.setLastTraceTime(new Date().getTime());
			contactHospitalMapper.updateByPrimaryKeySelective(contactHospital);

			TraceRecord traceRecord = new TraceRecord();
			String traceRecordId = RandomStringUtils.randomNumeric(12);
			traceRecord.setId(traceRecordId);
			traceRecord.setType(3);
			traceRecord.setUserId(loginStatus.getUserId());
			traceRecord.setContent(content);
			traceRecord.setTraceTime(new Date().getTime());
			traceRecord.setContactHospitalId(contactHospitalId);
			traceRecordMapper.insertSelective(traceRecord);

			session.commit();

			RefreshUserAlive.bean.run(loginStatus.getUserId());
			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("traceRecordId", traceRecordId);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (session != null)
				session.rollback();
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	@RequestMapping(value = "/hostory", method = RequestMethod.POST)
	public void hospitals(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SqlSession session = null;
		try {
			// 获取并处理参数
			String contactHospitalId = StringUtils.trimToNull(request.getParameter("contact_hospital_id"));
			if (contactHospitalId == null)
				throw new InteractRuntimeException("contact_hospital_id can`t be empty.");

			String pageNoParam = StringUtils.trimToNull(request.getParameter("page_no"));
			long pageNo = StringUtils.isNotEmpty(pageNoParam) ? Long.valueOf(pageNoParam) : 1;
			String pageSizeParam = StringUtils.trimToNull(request.getParameter("page_size"));
			long pageSize = StringUtils.isNotEmpty(pageSizeParam) ? Long.valueOf(pageSizeParam) : 30;
			if (pageSize > 300)
				throw new InteractRuntimeException("page_size有误");

			// 业务处理
			UserLoginStatus loginStatus = GetLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			session = SysConstant.sqlSessionFactory.openSession(true);
			TraceRecordManageMapper traceRecordManageMapper = session.getMapper(TraceRecordManageMapper.class);

			Long totalItemCount = traceRecordManageMapper.traceRecordsHostoryCount(contactHospitalId);
			PagingPage pagingPage = new PagingPage(pageSize, pageNo, totalItemCount);

			List<Map> traceRecords = traceRecordManageMapper.traceRecordsHostory(contactHospitalId, pagingPage);

			// 返回结果
			JSONObject data = new JSONObject();
			JSONObject traceRecordsJson = new JSONObject();
			traceRecordsJson.put("items", traceRecords);
			traceRecordsJson.put("pageNo", pagingPage.getPageNo());
			traceRecordsJson.put("pageSize", pagingPage.getPageSize());
			traceRecordsJson.put("totalItemCount", pagingPage.getTotalItemCount());
			traceRecordsJson.put("totalPageCount", pagingPage.getTotalPageCount());
			data.put("traceRecords", traceRecordsJson);

			RefreshUserAlive.bean.run(loginStatus.getUserId());
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (session != null)
				session.close();
		}
	}

	public static void main(String[] args) {
		System.out.println(Byte.SIZE);
		System.out.println(Integer.SIZE);
	}
}
