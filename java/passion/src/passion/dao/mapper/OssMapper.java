package passion.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import passion.dao.entity.ClientUser;

public interface OssMapper {
	@Insert("<script>insert oss.t_unused_file values <foreach collection=\"urls\" item=\"item\"  separator=\",\"><if test=\"item != null and item != ''\">(#{item})</if></foreach></script>")
	int delOss(@Param("urls") List<String> urls);

	@Insert("<script>insert oss.t_unused_file values <foreach collection=\"urls\" item=\"item\"  separator=\",\"><if test=\"item != null and item != ''\">(#{item})</if></foreach></script>")
	int delOssOfArray(@Param("urls") String... urls);
}