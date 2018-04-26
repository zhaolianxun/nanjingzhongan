package zasellaid.module.client.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import zasellaid.entity.PagingPage;

public interface TraceRecordManageMapper {

	@Select("<script>select voice_duration voiceDuration,type,content,trace_time traceTime from t_trace_record t where t.contact_hospital_id=#{contactHospitalId} order by t.trace_time desc limit #{page.start},#{page.pageSize}</script>")
	List<Map> traceRecordsHostory(@Param("contactHospitalId") String contactHospitalId,@Param("page") PagingPage page);

	@Select("<script>select count(id) from t_trace_record t where t.contact_hospital_id=#{contactHospitalId}</script>")
	Long traceRecordsHostoryCount(String contactHospitalId);
}