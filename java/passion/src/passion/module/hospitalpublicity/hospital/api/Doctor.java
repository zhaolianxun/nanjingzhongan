package passion.module.hospitalpublicity.hospital.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;

import passion.dao.entity.HpDoctor;
import passion.dao.mapper.HpClientHomepageTeamdoctorMapper;
import passion.dao.mapper.HpDoctorMapper;
import passion.entity.InteractRuntimeException;
import passion.module.client.api.BaseController;
import passion.module.hospitalpublicity.hospital.business.GetUserLoginStatus;
import passion.module.hospitalpublicity.hospital.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;

@Controller("passion.module.hospitalpublicity.hospital.api.Doctor")
@RequestMapping(value = "/hp/h/doctor")
public class Doctor extends BaseController {

	public static Logger logger = Logger.getLogger(Doctor.class);

	@Autowired
	protected HpDoctorMapper hpDoctorMapper;
	@Autowired
	protected HpClientHomepageTeamdoctorMapper hpClientHomepageTeamdoctorMapper;

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("name不可空");
			String photo = StringUtils.trimToNull(request.getParameter("photo"));
			if (photo == null)
				throw new InteractRuntimeException("photo不可空");
			String introduction = StringUtils.trimToNull(request.getParameter("introduction"));
			if (introduction == null)
				throw new InteractRuntimeException("introduction不可空");
			String jobtitle = StringUtils.trimToNull(request.getParameter("jobtitle"));
			if (jobtitle == null)
				throw new InteractRuntimeException("jobtitle不可空");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpDoctor doctor = new HpDoctor();
			String doctorId = RandomStringUtils.randomNumeric(12);
			doctor.setId(doctorId);
			doctor.setHospitalId(loginStatus.getHospitalId());
			doctor.setName(name);
			doctor.setPhoto(photo);
			doctor.setIntroduction(introduction);
			doctor.setJobtitle(jobtitle);
			hpDoctorMapper.insertSelective(doctor);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("doctorId", doctorId);
			HttpRespondWithData.todo(request, response, 0, null, data);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));

			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/alter", method = RequestMethod.POST)
	public void alter(HttpServletRequest request, HttpServletResponse response) throws Exception {
		TransactionStatus traStatus = null;
		try {
			// 接口预处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);
			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			// TODO 获取请求参数
			String doctorId = StringUtils.trimToNull(request.getParameter("doctor_id"));
			if (doctorId == null)
				throw new InteractRuntimeException("doctor_id不可空");
			String name = StringUtils.trim(request.getParameter("name"));
			if (name == "")
				throw new InteractRuntimeException("name不可空");
			String photo = StringUtils.trim(request.getParameter("photo"));
			if (photo == "")
				throw new InteractRuntimeException("photo不可空");
			String introduction = StringUtils.trim(request.getParameter("introduction"));
			if (introduction == "")
				throw new InteractRuntimeException("introduction不可空");
			String jobtitle = StringUtils.trim(request.getParameter("jobtitle"));
			if (jobtitle == "")
				throw new InteractRuntimeException("jobtitle不可空");

			// TODO 业务处理
			// 新数据库事务
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			traStatus = txManager.getTransaction(def);

			HpDoctor doctor = hpDoctorMapper.selectByPrimaryKey(doctorId);
			if (doctor == null)
				throw new InteractRuntimeException("医生不存在");
			if (!doctor.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("不是您医院的医生");

			if (photo != null)
				ossMapper.delOssOfArray(doctor.getPhoto());

			doctor.setName(name);
			doctor.setPhoto(photo);
			doctor.setIntroduction(introduction);
			doctor.setJobtitle(jobtitle);
			hpDoctorMapper.updateByPrimaryKeySelective(doctor);

			txManager.commit(traStatus);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (traStatus != null)
				txManager.rollback(traStatus);
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/del", method = RequestMethod.POST)
	public void del(HttpServletRequest request, HttpServletResponse response) throws Exception {
		TransactionStatus traStatus = null;
		try {
			// 接口预处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			// TODO 获取请求参数
			String doctorId = StringUtils.trimToNull(request.getParameter("doctor_id"));
			if (doctorId == null)
				throw new InteractRuntimeException("doctor_id不可空");
			// TODO 业务处理

			// 新数据库事务
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			traStatus = txManager.getTransaction(def);

			HpDoctor doctor = hpDoctorMapper.selectByPrimaryKey(doctorId);
			if (doctor == null)
				throw new InteractRuntimeException("医生不存在");
			if (!doctor.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("不是您医院的医生");

			if (StringUtils.isNotEmpty(doctor.getPhoto()))
				ossMapper.delOssOfArray(doctor.getPhoto());
			hpDoctorMapper.deleteByPrimaryKey(doctorId);
			hpClientHomepageTeamdoctorMapper.deleteByPrimaryKey(doctorId);

			txManager.commit(traStatus);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);

		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			if (traStatus != null)
				txManager.rollback(traStatus);
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// TODO 释放资源
		}
	}
}
