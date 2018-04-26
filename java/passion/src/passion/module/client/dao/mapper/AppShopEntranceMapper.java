package passion.module.client.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import passion.entity.PagingPage;

public interface AppShopEntranceMapper {

	/**
	 * 应用列表
	 */
	@Select("<script>select id shopappId,remark,lastest_template_id templateId,icon,name,price,summary,intro_pic introPic from t_shopapp t  order by create_time desc limit #{page.start},#{page.pageSize}</script>")
	List<Map> apps(@Param("userId") String userId, @Param("page") PagingPage page);

	/**
	 * 应用总数
	 */
	@Select("select count(id) from t_shopapp")
	Long appsCount();

}