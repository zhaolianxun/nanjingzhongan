package passion.module.hospitalpublicity.hospital.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import passion.dao.entity.HpHospitalUser;

public interface UserModuleMapper {
	@Select("select * from t_hp_hospital_user where username=#{username} and password_md5 = #{passwordMd5}")
	@ResultMap("passion.dao.mapper.HpHospitalUserMapper.BaseResultMap")
	HpHospitalUser loginByUsernameAndPwdCheck(@Param("username") String username,
			@Param("passwordMd5") String passwordMd5);

}