package passion.module.client.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import passion.dao.entity.WxMiniappTemplate;
import passion.entity.PagingPage;

public interface AppManageEntranceMapper {

	/**
	 * 应用列表
	 */
	@Select("<script>select t.use_endtime useEndtime,u.phone,u.username,t.id appId,t.nick_name nickName,t.head_img headImg,t.qrcode_url qrcodeUrl from t_wx_miniapp t inner join t_client_user u on t.user_id=u.id <trim prefix=\"where\" prefixOverrides=\"and\"><if test=\"phone != null\"> and u.phone=#{phone} </if><if test=\"username != null\"> and u.username=#{username} </if></trim> order by t.bind_time  desc limit #{page.start},#{page.pageSize}</script>")
	List<Map> apps(@Param("phone") String phone, @Param("username") String username, @Param("page") PagingPage page);

	/**
	 * 应用总数
	 */
	@Select("<script>select count(t.id) from t_wx_miniapp t inner join t_client_user u on t.user_id=u.id <trim prefix=\"where\" prefixOverrides=\"and\"><if test=\"phone != null\"> and u.phone=#{phone} </if><if test=\"username != null\"> and u.username=#{username} </if></trim></script>")
	Long appCount(@Param("phone") String phone, @Param("username") String username);

}