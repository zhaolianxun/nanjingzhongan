package passion.module.hospitalpublicity.client.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import passion.entity.PagingPage;

@Repository("passion.module.hospitalpublicity.client.dao.mapper.PageMapper")
public interface PageMapper {

	/**
	 * 项目页-项目列表
	 */
	@Select("select id projectId,title,title_picture titlePicture from t_hp_project where hospital_id=#{hospitalId} order by create_time desc limit #{page.start},#{page.pageSize} ")
	List<Map> getProjectPageProjects(@Param("hospitalId") String hospitalId, @Param("page") PagingPage page);

	/**
	 * 项目页-项目列表-总数
	 */
	@Select("select count(1) from t_hp_project where hospital_id=#{hospitalId}")
	Long getProjectPageProjectsCount(@Param("hospitalId") String hospitalId);

	/**
	 * 项目信息页
	 */
	@Select("select title,content,create_time createTime from t_hp_project where id=#{projectId}")
	Map getProjectInfoPage(@Param("projectId") String projectId);

	/**
	 * 活动页-活动列表
	 */
	@Select("select id activityId,title,title_picture titlePicture,summary from t_hp_activity where hospital_id=#{hospitalId} order by create_time desc limit #{page.start},#{page.pageSize} ")
	List<Map> getActivityPageActivities(@Param("hospitalId") String hospitalId, @Param("page") PagingPage page);

	/**
	 * 活动页-活动列表-总数
	 */
	@Select("select count(1) from t_hp_activity where hospital_id=#{hospitalId}")
	Long getActivityPageActivitiesCount(@Param("hospitalId") String hospitalId);

	/**
	 * 活动信息页
	 */
	@Select("select title,content,summary,create_time createTime from t_hp_activity where id=#{activityId}")
	Map getActivityInfoPage(@Param("activityId") String activityId);

	/**
	 * 首页-特色科室
	 */
	@Select("select d.id departmentId,d.picture,d.name from t_hp_client_homepage_featureddepartment uhpfd inner join t_hp_department d on uhpfd.hospital_id=#{hospitalId} and uhpfd.department_id=d.id order by uhpfd.sequence_no asc")
	List<Map> getHomePageFeaturedDepartments(String hospitalId);

	/**
	 * 首页-轮播图列表
	 */
	@Select("select id,picture,type,link_id linkId from t_hp_client_homepage_rollpicture chr where chr.hospital_id=#{hospitalId} order by chr.sequence_no asc")
	List<Map> getHomePageRollPictures(String hospitalId);

	/**
	 * 首页-团队医生
	 */
	@Select("select d.id doctorId,d.name,d.photo,d.jobtitle,d.introduction from t_hp_client_homepage_teamdoctor uhpdt inner join t_hp_doctor d on uhpdt.hospital_id = #{hospitalId} and uhpdt.doctor_id=d.id order by uhpdt.sequence_no asc")
	List<Map> getHomePageTeamDoctors(String hospitalId);

	/**
	 * 医生信息页
	 */
	@Select("select name,introduction,jobtitle from t_hp_doctor where id = #{doctorId}")
	Map getDoctorInfoPage(String doctorId);

	/**
	 * 查看更多医生页-医生列表
	 */
	@Select("select d.id doctorId,d.name,d.photo,d.jobtitle,d.introduction from  t_hp_doctor d where d.hospital_id = #{hospitalId} and d.id not in (select doctor_id from t_hp_client_homepage_teamdoctor)  order by d.name asc limit #{page.start},#{page.pageSize}")
	List<Map> getDoctorLookMorePageDoctors(@Param("hospitalId") String hospitalId, @Param("page") PagingPage page);

	/**
	 * 查看更多医生页-医生列表总数
	 */
	@Select("select count(1) from  t_hp_doctor d where d.hospital_id = #{hospitalId} and d.id not in (select doctor_id from t_hp_client_homepage_teamdoctor)")
	Long getDoctorLookMorePageDoctorsCount(@Param("hospitalId") String hospitalId);

	/**
	 * 医院信息页
	 */
	@Select("select hospital_name hospitalName,introduction,picture from t_hp_hospital where id = #{hospitalId}")
	Map getHospitalInfoPage(String hospitalId);
}