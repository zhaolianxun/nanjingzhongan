package passion.module.client.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import passion.dao.entity.WxMiniappTemplate;
import passion.entity.PagingPage;

public interface MiniappTemplateManageMapper {

	/**
	 * 应用列表
	 */
	@Select("<script>select id appId,icon,name,price,summary,intro_pic introPic from t_wx_miniapp_template order by create_time desc limit #{page.start},#{page.pageSize}</script>")
	List<Map> templates(@Param("page") PagingPage page);

	/**
	 * 应用总数
	 */
	@Select("select count(id) from t_wx_miniapp_template")
	Long templateCount();

	@Select("select * from t_wx_miniapp_template where wx_template_id=#{wxTemplateId} and wx_appid=#{wxAppid}")
	@ResultMap("passion.dao.mapper.WxMiniappTemplateMapper.BaseResultMap")
	WxMiniappTemplate getByWxTemplateIdAndWxAppid(@Param("wxTemplateId") int wxTemplateId,
			@Param("wxAppid") String wxAppid);

	@Select("select user_version from t_wx_miniapp_template where id=#{templateId}")
	@ResultType(String.class)
	String getUserVersion(int templateId);

	@Select("select id from t_wx_miniapp_template t where t.wx_template_id=(select max(wx_template_id) wx_template_id from t_wx_miniapp_template where shopapp_id=#{shopappId}) and shopapp_id=#{shopappId}")
	@ResultType(String.class)
	String getLastestTemplateId(String shopappId);
}