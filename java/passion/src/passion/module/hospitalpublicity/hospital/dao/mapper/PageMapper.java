package passion.module.hospitalpublicity.hospital.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import passion.entity.PagingPage;

@Repository("passion.module.hospitalpublicity.hospital.dao.mapper.PageMapper")
public interface PageMapper {

	@Select("select hospital_name hospitalName,introduction,picture,entering_time enteringTime from t_hp_hospital where id=#{hospitalId}")
	Map homeInfo(String hospitalId);

	/**
	 * 医生列表页
	 */
	@Select("select id doctorId,name,photo,jobtitle,introduction from t_hp_doctor where hospital_id=#{hospitalId} order by name asc limit #{page.start},#{page.pageSize} ")
	List<Map> getDoctorsPageDoctors(@Param("hospitalId") String hospitalId, @Param("page") PagingPage page);

	/**
	 * 医生列表页-医生总数
	 */
	@Select("select count(1) from t_hp_doctor where hospital_id=#{hospitalId}")
	Long getDoctorsPageDoctorsCount(@Param("hospitalId") String hospitalId);

	/**
	 * 科室列表页
	 */
	@Select("select id departmentId,name,picture,introduction from t_hp_department where hospital_id=#{hospitalId} order by name asc limit #{page.start},#{page.pageSize} ")
	List<Map> getDepartmentsPageDepartments(@Param("hospitalId") String hospitalId, @Param("page") PagingPage page);

	/**
	 * 科室列表页-科室总数
	 */
	@Select("select count(1) from t_hp_department where hospital_id=#{hospitalId}")
	Long getDepartmentsPageDepartmentsCount(@Param("hospitalId") String hospitalId);

	/**
	 * 科室列表页
	 */
	@Select("select id activityId,title,title_picture titlePicture,summary,content,create_time createTime from t_hp_activity where hospital_id=#{hospitalId} order by create_time desc limit #{page.start},#{page.pageSize} ")
	List<Map> getActivitiesPageActivities(@Param("hospitalId") String hospitalId, @Param("page") PagingPage page);

	/**
	 * 科室列表页-科室总数
	 */
	@Select("select count(1) from t_hp_activity where hospital_id=#{hospitalId}")
	Long getActivitiesPageActivitiesCount(@Param("hospitalId") String hospitalId);

	/**
	 * 项目列表页
	 */
	@Select("select id projectId,title,title_picture,content from t_hp_project where hospital_id=#{hospitalId} order by create_time desc limit #{page.start},#{page.pageSize} ")
	List<Map> getProjectsPageProjects(@Param("hospitalId") String hospitalId, @Param("page") PagingPage page);

	/**
	 * 项目列表页-项目总数
	 */
	@Select("select count(1) from t_hp_project where hospital_id=#{hospitalId}")
	Long getProjectsPageProjectsCount(@Param("hospitalId") String hospitalId);

	/**
	 * 特色科室列表页
	 */
	@Select("select d.id departmentId,d.name,d.picture,d.introduction from t_hp_client_homepage_featureddepartment chfd inner join t_hp_department d on chfd.department_id = d.id where d.hospital_id=#{hospitalId} order by name asc limit #{page.start},#{page.pageSize} ")
	List<Map> getFeaturedDepartmentsPageDepartments(@Param("hospitalId") String hospitalId,
			@Param("page") PagingPage page);

	/**
	 * 特色科室列表页-科室总数
	 */
	@Select("select count(1) from t_hp_client_homepage_featureddepartment chfd inner join t_hp_department d on chfd.department_id = d.id where d.hospital_id=#{hospitalId}")
	Long getFeaturedDepartmentsPageDepartmentsCount(@Param("hospitalId") String hospitalId);

	/**
	 * 查询客户端首页轮播图列表页
	 */
	@Select("select id,type,picture,sequence_no,link_id from t_hp_client_homepage_rollpicture where hospital_id=#{hospitalId} order by sequence_no asc limit #{page.start},#{page.pageSize} ")
	List<Map> getClientHomePageRollPictures(@Param("hospitalId") String hospitalId, @Param("page") PagingPage page);

	/**
	 * 查询客户端首页轮播图列表页-轮播图总数
	 */
	@Select("select count(1) from t_hp_client_homepage_rollpicture  where hospital_id=#{hospitalId}")
	Long getClientHomePageRollPicturesCount(@Param("hospitalId") String hospitalId);

}