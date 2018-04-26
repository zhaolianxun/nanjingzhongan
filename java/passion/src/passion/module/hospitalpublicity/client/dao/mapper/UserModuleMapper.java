package passion.module.hospitalpublicity.client.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository("passion.module.hospitalpublicity.client.dao.mapper.UserModuleMapper")
public interface UserModuleMapper {

	@Select("select id from t_hp_client_user where wx_open_id=#{wxOpenId} and hospital_id=#{hospitalId} for update")
	String lockAndGetIdByWxOpenIdAndHospitalId(@Param("wxOpenId") String wxOpenId,
			@Param("hospitalId") String hospitalId);

	@Select("select id from t_hp_hospital where wx_miniapp_id=#{wxMiniappId}")
	String getHospitalIdByWxMiniappId(@Param("wxMiniappId") String wxMiniappId);
}