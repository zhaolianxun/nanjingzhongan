package passion.dao.mapper;

import passion.dao.entity.WxMiniappTemplate;

public interface WxMiniappTemplateMapper {

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_wx_miniapp_template
	 * @mbg.generated
	 */
	int deleteByPrimaryKey(String id);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_wx_miniapp_template
	 * @mbg.generated
	 */
	int insert(WxMiniappTemplate record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_wx_miniapp_template
	 * @mbg.generated
	 */
	int insertSelective(WxMiniappTemplate record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_wx_miniapp_template
	 * @mbg.generated
	 */
	WxMiniappTemplate selectByPrimaryKey(String id);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_wx_miniapp_template
	 * @mbg.generated
	 */
	int updateByPrimaryKeySelective(WxMiniappTemplate record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table t_wx_miniapp_template
	 * @mbg.generated
	 */
	int updateByPrimaryKey(WxMiniappTemplate record);

	
}