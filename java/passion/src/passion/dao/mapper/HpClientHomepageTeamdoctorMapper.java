package passion.dao.mapper;

import passion.dao.entity.HpClientHomepageTeamdoctor;

public interface HpClientHomepageTeamdoctorMapper {

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_teamdoctor
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(String doctorId);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_teamdoctor
	 * @mbg.generated
	 */
	int insert(HpClientHomepageTeamdoctor record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_teamdoctor
	 * @mbg.generated
	 */
	int insertSelective(HpClientHomepageTeamdoctor record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_teamdoctor
	 * @mbg.generated
	 */
	HpClientHomepageTeamdoctor selectByPrimaryKey(String doctorId);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_teamdoctor
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(HpClientHomepageTeamdoctor record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_teamdoctor
	 * @mbg.generated
	 */
	int updateByPrimaryKey(HpClientHomepageTeamdoctor record);
}