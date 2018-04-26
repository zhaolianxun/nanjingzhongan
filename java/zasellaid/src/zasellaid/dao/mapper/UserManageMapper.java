package zasellaid.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import zasellaid.dao.entity.ClientUser;
import zasellaid.entity.PagingPage;

public interface UserManageMapper {

	@Select("select id userId,realname,phone,if_loginable ifLoginable,loginunable_reason loginunableReason from t_client_user order by register_time desc limit #{page.start},#{page.pageSize}")
	List<Map> users(@Param("page") PagingPage page);

	@Select("select count(id) from t_client_user")
	Long userCount();

	@Select("select count(1) from t_client_user where phone=#{phone}")
	int phoneCheck(@Param("phone") String phone);


	@Select("select username from t_client_user where id=#{userId}")
	String getUsername(@Param("userId") String userId);
}