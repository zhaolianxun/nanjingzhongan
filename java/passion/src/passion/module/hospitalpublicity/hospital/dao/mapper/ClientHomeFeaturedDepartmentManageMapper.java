package passion.module.hospitalpublicity.hospital.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ClientHomeFeaturedDepartmentManageMapper {

	@Select("select ifnull(max(sequence_no),0) from t_hp_client_homepage_featureddepartment where hospital_id = #{hospitalId} for update")
	byte selectMaxSequenceNoAndLockTable(@Param("hospitalId") String hospitalId);

	@Update("update t_hp_client_homepage_featureddepartment set sequence_no=sequence_no-1 where sequence_no > #{limit} and hospital_id = #{hospitalId}")
	int goPrevSequenceNo(@Param("limit") byte limit, @Param("hospitalId") String hospitalId);

	@Update("update t_hp_client_homepage_featureddepartment set sequence_no=sequence_no+1 where sequence_no >= #{limit} and hospital_id = #{hospitalId}")
	int goAfterSequenceNo(@Param("limit") byte limit, @Param("hospitalId") String hospitalId);

}