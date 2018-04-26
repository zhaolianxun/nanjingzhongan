package zasellaid.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Select;

public interface AdminareaMapper {
	@Select("select id,name from `cm-plat`.t_adminarea where level=1 order by name asc")
	List<Map> getProvinces();

	@Select("select id,name from `cm-plat`.t_adminarea where level=2 and upid=#{provinceId} order by name asc")
	List<Map> getCities(int provinceId);

	@Select("select id,name from `cm-plat`.t_adminarea where level=3 and upid=#{cityId} order by name asc")
	List<Map> getDistincts(int cityId);
}