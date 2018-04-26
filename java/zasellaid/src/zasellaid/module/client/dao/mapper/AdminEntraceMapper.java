package zasellaid.module.client.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import zasellaid.entity.PagingPage;

public interface AdminEntraceMapper {

	@Select("<script>select admin_look_report_today adminLookReportToday,id userId,realname,insert(phone, 4, 4, '****') phone,(select count(id) from t_trace_record where user_id=t.id and from_unixtime(trace_time/1000,'%Y-%m-%d')=DATE_SUB(curdate(),INTERVAL 0 DAY)) traceCountToday,(select count(id) from t_trace_record where user_id=t.id and from_unixtime(trace_time/1000,'%Y-%m-%d')=DATE_SUB(curdate(),INTERVAL 1 DAY)) traceCountYesterday,(select count(id) from t_contact_hospital where client_user_id=t.id and customer_type=1) juniorCustomerCount,(select count(id) from t_contact_hospital where client_user_id=t.id and customer_type=2) tracingCustomerCount,(select count(id) from t_contact_hospital where client_user_id=t.id and customer_type=3) vitalCustomerCount,(select if(count(id)>0,1,0) from t_daily_report where user_id=t.id and date=curdate()) dailyReportSubmitIs,(select if(superior_review is not null and superior_review != '',1,0) from t_daily_report where user_id=t.id and date=curdate()) dailyReportReviewIs,t.if_admin ifAdmin from t_client_user t where t.admin_user_id=#{userId} <if test=\"keyw != null\"> and t.phone like '%${keyw}%' and t.realname like '%${keyw}%' </if> order by t.register_time desc limit #{page.start},#{page.pageSize}</script>")
	List<Map> home_ClientUsers(@Param("userId") String userId,@Param("keyw") String keyw, @Param("page") PagingPage page);

	@Select("<script>select count(id) from t_client_user t where t.admin_user_id=#{userId} <if test=\"keyw != null\"> and t.phone like '%${keyw}%' and t.realname like '%${keyw}%' </if></script>")
	Long home_ClientUsersCount(@Param("userId") String userId,@Param("keyw") String keyw);

	@Select("<script>select type,content,trace_time traceTime from t_trace_record t where t.contact_hospital_id=#{contactHospitalId}  order by t.trace_time desc limit #{page.start},#{page.pageSize}</script>")
	List<Map> traceRecord_records(@Param("contactHospitalId") String contactHospitalId, @Param("page") PagingPage page);

	@Select("<script>select count(id) from t_trace_record t  where t.contact_hospital_id=#{contactHospitalId}</script>")
	Long traceRecord_recordsCount(@Param("contactHospitalId") String contactHospitalId);

	@Select("<script>select add_time addTime,city_name cityName,province_name provinceName,customer_type customerType,name,id contactHospitalId,(select max(trace_time) from t_trace_record where contact_hospital_id=t.id) lastTraceTime from t_contact_hospital t <trim prefix=\"where\" prefixOverrides=\"and\"> and client_user_id=#{clientUserId} <if test=\"customerType != null\">and t.customer_type=#{customerType}</if><if test=\"traceStatus != null\">and t.trace_status=#{traceStatus}</if></trim> order by lastTraceTime desc limit #{page.start},#{page.pageSize}</script>")
	List<Map> userContactHospital_hospitals(@Param("clientUserId") String clientUserId,
			@Param("customerType") Byte customerType, @Param("traceStatus") Byte traceStatus,
			@Param("page") PagingPage page);

	@Select("<script>select count(t.id) from t_contact_hospital t <trim prefix=\"where\" prefixOverrides=\"and\"> and client_user_id=#{clientUserId} <if test=\"customerType != null\">and t.customer_type=#{customerType}</if><if test=\"traceStatus != null\">and t.trace_status=#{traceStatus}</if></trim></script>")
	Long userContactHospital_hospitalsCount(@Param("clientUserId") String clientUserId,
			@Param("customerType") Byte customerType, @Param("traceStatus") Byte traceStatus);
}