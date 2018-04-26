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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;

import passion.dao.entity.HpClientHomepageFeatureddepartment;
import passion.dao.entity.HpDepartment;
import passion.dao.mapper.HpClientHomepageFeatureddepartmentMapper;
import passion.dao.mapper.HpDepartmentMapper;
import passion.entity.InteractRuntimeException;
import passion.module.client.api.BaseController;
import passion.module.hospitalpublicity.hospital.business.GetUserLoginStatus;
import passion.module.hospitalpublicity.hospital.dao.mapper.ClientHomeFeaturedDepartmentManageMapper;
import passion.module.hospitalpublicity.hospital.entity.UserLoginStatus;
import passion.util.HttpRespondWithData;

@Controller("passion.module.hospitalpublicity.hospital.api.Department")
@RequestMapping(value = "/hp/h/department")
public class Department extends BaseController {

	public static Logger logger = Logger.getLogger(Department.class);

	@Autowired
	protected HpDepartmentMapper hpDepartmentMapper;
	@Autowired
	protected HpClientHomepageFeatureddepartmentMapper clientHomePageFeaturedDepartmentMapper;

	@Autowired
	protected ClientHomeFeaturedDepartmentManageMapper clientHomeFeaturedDepartmentManageMapper;

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			// TODO 获取请求参数
			String name = StringUtils.trimToNull(request.getParameter("name"));
			if (name == null)
				throw new InteractRuntimeException("名不可空");
			String picture = StringUtils.trimToNull(request.getParameter("picture"));
			if (picture == null)
				throw new InteractRuntimeException("picture不可空");
			String introduction = StringUtils.trimToNull(request.getParameter("introduction"));
			if (introduction == null)
				throw new InteractRuntimeException("introduction不可空");
			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpDepartment department = new HpDepartment();
			String id = RandomStringUtils.randomNumeric(12);
			department.setId(id);
			department.setHospitalId(loginStatus.getHospitalId());
			department.setName(name);
			department.setPicture(picture);
			department.setIntroduction(introduction);
			hpDepartmentMapper.insertSelective(department);

			// TODO 返回结果
			JSONObject data = new JSONObject();
			data.put("departmentId", id);
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
			// TODO 获取请求参数
			String departmentId = StringUtils.trimToNull(request.getParameter("department_id"));
			if (departmentId == null)
				throw new InteractRuntimeException("department_id不可空");
			String name = StringUtils.trim(request.getParameter("name"));
			if (name == "")
				throw new InteractRuntimeException("name不可空");
			String picture = StringUtils.trim(request.getParameter("picture"));
			if (picture == "")
				throw new InteractRuntimeException("picture不可空");
			String introduction = StringUtils.trim(request.getParameter("introduction"));
			if (introduction == "")
				throw new InteractRuntimeException("introduction不可空");
			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			// 新数据库事务
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			traStatus = txManager.getTransaction(def);

			HpDepartment department = hpDepartmentMapper.selectByPrimaryKey(departmentId);
			if (department == null)
				throw new InteractRuntimeException("科室不存在");
			if (!department.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("不是您医院的科室");

			if (picture != null && StringUtils.isNotEmpty(department.getPicture()))
				ossMapper.delOssOfArray(department.getPicture());

			department.setName(name);
			department.setPicture(picture);
			department.setIntroduction(introduction);
			hpDepartmentMapper.updateByPrimaryKeySelective(department);

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
			// TODO 获取请求参数
			String departmentId = StringUtils.trimToNull(request.getParameter("department_id"));
			if (departmentId == null)
				throw new InteractRuntimeException("department_id不可空");
			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			// 新数据库事务
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			traStatus = txManager.getTransaction(def);

			HpDepartment department = hpDepartmentMapper.selectByPrimaryKey(departmentId);
			if (department == null)
				throw new InteractRuntimeException("科室不存在");
			if (!department.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("不是您医院的科室");

			if (StringUtils.isNotEmpty(department.getPicture()))
				ossMapper.delOssOfArray(department.getPicture());
			hpDepartmentMapper.deleteByPrimaryKey(departmentId);
			clientHomePageFeaturedDepartmentMapper.deleteByPrimaryKey(departmentId);

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

	@RequestMapping(value = "/jointoclienthomepagefeatureddepartment", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void joinToClientHomePageFeaturedDepartment(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			// TODO 获取请求参数
			String departmentId = StringUtils.trimToNull(request.getParameter("department_id"));
			if (departmentId == null)
				throw new InteractRuntimeException("department_id不可空");
			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpDepartment department = hpDepartmentMapper.selectByPrimaryKey(departmentId);
			if (department == null)
				throw new InteractRuntimeException("科室不存在");
			if (!department.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("不是您医院的科室");

			HpClientHomepageFeatureddepartment d = clientHomePageFeaturedDepartmentMapper
					.selectByPrimaryKey(departmentId);
			if (d != null)
				throw new InteractRuntimeException("已存在");

			byte maxSequenceNo = clientHomeFeaturedDepartmentManageMapper
					.selectMaxSequenceNoAndLockTable(department.getHospitalId());

			if (maxSequenceNo >= 127)
				throw new InteractRuntimeException("位置已满");

			HpClientHomepageFeatureddepartment chpfd = new HpClientHomepageFeatureddepartment();
			chpfd.setDepartmentId(departmentId);
			chpfd.setHospitalId(department.getHospitalId());
			chpfd.setSequenceNo((byte) (maxSequenceNo + 1));
			clientHomePageFeaturedDepartmentMapper.insertSelective(chpfd);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);

			throw e;
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/removefromclienthomepagefeatureddepartment", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void removeFromClientHomePageFeaturedDepartment(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			// TODO 获取请求参数
			String departmentId = StringUtils.trimToNull(request.getParameter("department_id"));
			if (departmentId == null)
				throw new InteractRuntimeException("department_id不可空");
			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpDepartment department = hpDepartmentMapper.selectByPrimaryKey(departmentId);
			if (department == null)
				throw new InteractRuntimeException("科室不存在");
			if (!department.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("不是您医院的科室");

			HpClientHomepageFeatureddepartment d = clientHomePageFeaturedDepartmentMapper
					.selectByPrimaryKey(departmentId);
			if (d == null)
				throw new InteractRuntimeException("不存在");

			// 锁表
			clientHomeFeaturedDepartmentManageMapper.selectMaxSequenceNoAndLockTable(department.getHospitalId());

			clientHomePageFeaturedDepartmentMapper.deleteByPrimaryKey(departmentId);
			clientHomeFeaturedDepartmentManageMapper.goPrevSequenceNo(d.getSequenceNo(), department.getHospitalId());

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
			throw e;
		} finally {
			// TODO 释放资源
		}
	}

	@RequestMapping(value = "/moveclienthomepagefeatureddepartment", method = RequestMethod.POST)
	@Transactional(rollbackFor = Exception.class)
	public void moveClientHomePageFeaturedDepartment(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			// TODO 获取请求参数
			String departmentId = StringUtils.trimToNull(request.getParameter("department_id"));
			if (departmentId == null)
				throw new InteractRuntimeException("department_id不可空");
			String targetDepartmentId = StringUtils.trimToNull(request.getParameter("target_department_id"));
			if (targetDepartmentId == null)
				throw new InteractRuntimeException("target_department_id不可空");

			// TODO 业务处理
			UserLoginStatus loginStatus = GetUserLoginStatus.todo(request);
			if (loginStatus == null)
				throw new InteractRuntimeException(20);

			if (loginStatus.getIfAdmin() != 1)
				throw new InteractRuntimeException("您不是管理员");

			HpDepartment department = hpDepartmentMapper.selectByPrimaryKey(departmentId);
			if (department == null)
				throw new InteractRuntimeException("科室不存在");
			if (!department.getHospitalId().equals(loginStatus.getHospitalId()))
				throw new InteractRuntimeException("不是您医院的科室");

			HpClientHomepageFeatureddepartment d = clientHomePageFeaturedDepartmentMapper
					.selectByPrimaryKey(departmentId);
			if (d == null)
				throw new InteractRuntimeException("不存在");

			HpClientHomepageFeatureddepartment td = clientHomePageFeaturedDepartmentMapper
					.selectByPrimaryKey(targetDepartmentId);

			byte sequenceNo = d.getSequenceNo();
			d.setSequenceNo(td.getSequenceNo());
			td.setSequenceNo(sequenceNo);

			clientHomePageFeaturedDepartmentMapper.updateByPrimaryKeySelective(d);
			clientHomePageFeaturedDepartmentMapper.updateByPrimaryKeySelective(td);

			// TODO 返回结果
			HttpRespondWithData.todo(request, response, 0, null, null);
		} catch (Exception e) {
			// TODO 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
			throw e;
		} finally {
			// TODO 释放资源
		}
	}
}
