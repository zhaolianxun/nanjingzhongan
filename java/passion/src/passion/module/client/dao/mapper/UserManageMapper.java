package passion.module.client.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import passion.dao.entity.ClientUser;
import passion.entity.PagingPage;

public interface UserManageMapper {

	@Select("select id userId,nickname,username,phone,if_loginable ifLoginable,loginunable_reason loginunableReason from t_client_user order by register_time desc limit #{page.start},#{page.pageSize}")
	List<Map> users(@Param("page") PagingPage page);

	@Select("select count(id) from t_client_user")
	Long userCount();

	@Select("select * from t_client_user where username=#{username} and password_md5 = #{passwordMd5}")
	@ResultMap("passion.dao.mapper.ClientUserMapper.BaseResultMap")
	ClientUser loginByUsernameAndPwdCheck(@Param("username") String username, @Param("passwordMd5") String passwordMd5);

	@Select("select count(1) from t_client_user where phone=#{phone}")
	int phoneCheck(@Param("phone") String phone);

	@Select("select count(1) from t_client_user where username=#{username}")
	int usernameCheck(@Param("username") String username);

	@Select("select * from t_client_user where phone=#{phone}")
	@ResultMap("passion.dao.mapper.ClientUserMapper.BaseResultMap")
	ClientUser getByPhone(@Param("phone") String phone);

	@Select("select username from t_client_user where id=#{userId}")
	String getUsername(@Param("userId") String userId);
}