package passion.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import passion.dao.entity.HpClientHomepageRollpicture;

public interface HpClientHomepageRollpictureMapper {

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_rollpicture
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(String id);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_rollpicture
	 * @mbg.generated
	 */
	int insert(HpClientHomepageRollpicture record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_rollpicture
	 * @mbg.generated
	 */
	int insertSelective(HpClientHomepageRollpicture record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_rollpicture
	 * @mbg.generated
	 */
	HpClientHomepageRollpicture selectByPrimaryKey(String id);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_rollpicture
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(HpClientHomepageRollpicture record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_hp_client_homepage_rollpicture
	 * @mbg.generated
	 */
	int updateByPrimaryKey(HpClientHomepageRollpicture record);
    
   
}