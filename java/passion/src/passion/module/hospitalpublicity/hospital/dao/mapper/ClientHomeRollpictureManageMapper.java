package passion.module.hospitalpublicity.hospital.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import passion.dao.entity.HpClientHomepageRollpicture;

public interface ClientHomeRollpictureManageMapper {

	@Select("select ifnull(max(sequence_no),0) from t_hp_client_homepage_rollpicture where hospital_id = #{hospitalId} for update")
	byte selectMaxSequenceNoAndLockTable(@Param("hospitalId") String hospitalId);

	@Update("update t_hp_client_homepage_rollpicture set sequence_no=sequence_no-1 where sequence_no > #{limit} and hospital_id = #{hospitalId}")
	int goPrevSequenceNo(@Param("limit") byte limit, @Param("hospitalId") String hospitalId);

	@Update("update t_hp_client_homepage_rollpicture set sequence_no=sequence_no+1 where sequence_no >= #{limit} and hospital_id = #{hospitalId}")
	int goAfterSequenceNo(@Param("limit") byte limit, @Param("hospitalId") String hospitalId);

	@Update("update t_hp_client_homepage_rollpicture set sequence_no=#{target} where sequence_no = #{src} and hospital_id = #{hospitalId}")
	int updateSequenceNoBySequenceNo(@Param("src") byte src, @Param("target") byte target,
			@Param("hospitalId") String hospitalId);

	@Select("select count(1) from t_hp_client_homepage_rollpicture where link_id=#{linkId}")
	int getLinkIdCount(@Param("linkId") String linkId);
}