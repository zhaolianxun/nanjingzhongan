package zasellaid.module.client.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import zasellaid.entity.PagingPage;

public interface ContactHospitalManageMapper {

	@Select("<script>select * from (select dean_name deanName,add_time addTime,city_name cityName,province_name provinceName,customer_type customerType,name,id contactHospitalId,(select max(trace_time) from t_trace_record where contact_hospital_id=t.id) lastTraceTime from t_contact_hospital t <trim prefix=\"where\" prefixOverrides=\"and\"> and client_user_id=#{clientUserId} <if test=\"customerType != null\"> and t.customer_type=#{customerType} </if><if test=\"traceStatus != null\"> and t.trace_status=#{traceStatus} </if><if test=\"provinceId != null\"> and t.province_id=#{provinceId} </if><if test=\"cityId != null\"> and t.city_id=#{cityId} </if></trim>) tt <trim prefix=\"where\" prefixOverrides=\"and\"><if test=\"traceStarttime != null\"> and tt.lastTraceTime &gt; #{traceStarttime} </if><if test=\"traceEndtime != null\"> and tt.lastTraceTime &lt; #{traceEndtime} </if></trim> order by tt.lastTraceTime desc limit #{page.start},#{page.pageSize}</script>")
	List<Map> hospitals(@Param("clientUserId") String clientUserId,@Param("customerType") Byte customerType, @Param("traceStarttime") Long traceStarttime,
			@Param("traceEndtime") Long traceEndtime, @Param("traceStatus") Byte traceStatus,
			@Param("provinceId") Integer provinceId, @Param("cityId") Integer cityId, @Param("page") PagingPage page);

	@Select("<script>select count(tt.contactHospitalId) from (select add_time addTime,city_name cityName,province_name provinceName,customer_type customerType,name,id contactHospitalId,(select max(trace_time) from t_trace_record where contact_hospital_id=t.id) lastTraceTime from t_contact_hospital t <trim prefix=\"where\" prefixOverrides=\"and\"> and client_user_id=#{clientUserId} <if test=\"customerType != null\"> and t.customer_type=#{customerType} </if><if test=\"traceStatus != null\"> and t.trace_status=#{traceStatus} </if><if test=\"provinceId != null\"> and t.province_id=#{provinceId} </if><if test=\"cityId != null\"> and t.city_id=#{cityId} </if></trim>) tt <trim prefix=\"where\" prefixOverrides=\"and\"><if test=\"traceStarttime != null\"> and tt.lastTraceTime &gt; #{traceStarttime} </if><if test=\"traceEndtime != null\"> and tt.lastTraceTime &lt; #{traceEndtime} </if></trim></script>")
	Long hospitalsCount(@Param("clientUserId") String clientUserId,@Param("customerType") Byte customerType, @Param("traceStarttime") Long traceStarttime,
			@Param("traceEndtime") Long traceEndtime, @Param("traceStatus") Byte traceStatus,
			@Param("provinceId") Integer provinceId, @Param("cityId") Integer cityId);

	@Select("<script>select * from (select dean_name deanName,add_time addTime,city_name cityName,province_name provinceName,customer_type customerType,name,id contactHospitalId,(select max(trace_time) from t_trace_record where contact_hospital_id=t.id) lastTraceTime from t_contact_hospital t <trim prefix=\"where\" prefixOverrides=\"and\"> and client_user_del=0 and client_user_id=#{clientUserId} <if test=\"customerType != null\"> and t.customer_type=#{customerType} </if><if test=\"traceStatus != null\"> and t.trace_status=#{traceStatus} </if><if test=\"provinceId != null\"> and t.province_id=#{provinceId} </if><if test=\"cityId != null\"> and t.city_id=#{cityId} </if></trim>) tt <trim prefix=\"where\" prefixOverrides=\"and\"> <if test=\"traceStarttime != null\"> and tt.lastTraceTime &gt; #{traceStarttime} </if><if test=\"traceEndtime != null\"> and tt.lastTraceTime &lt; #{traceEndtime} </if></trim> order by tt.addTime desc limit #{page.start},#{page.pageSize}</script>")
	List<Map> hospitals1(@Param("clientUserId") String clientUserId,@Param("customerType") Byte customerType, @Param("traceStarttime") Long traceStarttime,
			@Param("traceEndtime") Long traceEndtime, @Param("traceStatus") Byte traceStatus,
			@Param("provinceId") Integer provinceId, @Param("cityId") Integer cityId, @Param("page") PagingPage page);

	@Select("<script>select count(tt.contactHospitalId) from (select add_time addTime,city_name cityName,province_name provinceName,customer_type customerType,name,id contactHospitalId,(select max(trace_time) from t_trace_record where contact_hospital_id=t.id) lastTraceTime from t_contact_hospital t <trim prefix=\"where\" prefixOverrides=\"and\"> and client_user_del=0 and client_user_id=#{clientUserId} <if test=\"customerType != null\"> and t.customer_type=#{customerType} </if><if test=\"traceStatus != null\"> and t.trace_status=#{traceStatus} </if><if test=\"provinceId != null\"> and t.province_id=#{provinceId} </if><if test=\"cityId != null\"> and t.city_id=#{cityId} </if></trim>) tt <trim prefix=\"where\" prefixOverrides=\"and\"><if test=\"traceStarttime != null\"> and tt.lastTraceTime &gt; #{traceStarttime} </if><if test=\"traceEndtime != null\"> and tt.lastTraceTime &lt; #{traceEndtime} </if></trim></script>")
	Long hospitals1Count(@Param("clientUserId") String clientUserId,@Param("customerType") Byte customerType, @Param("traceStarttime") Long traceStarttime,
			@Param("traceEndtime") Long traceEndtime, @Param("traceStatus") Byte traceStatus,
			@Param("provinceId") Integer provinceId, @Param("cityId") Integer cityId);
	
	@Select("select distinct province_id id,province_name name from t_contact_hospital order by province_name asc")
	List<Map> existProvinces();

	@Select("select distinct city_id id,city_name name from t_contact_hospital where province_id=#{provinceId} order by city_name asc")
	List<Map> existCities(int provinceId);
}