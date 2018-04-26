package passion.module.client.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import passion.dao.entity.HpHospitalUser;
import passion.dao.entity.WxMiniapp;
import passion.entity.PagingPage;

public interface MyMiniappEntranceMapper {

	@Select("select * from t_hp_hospital_user t inner join t_hp_hospital u on t.hospital_id=u.id inner join t_wx_miniapp v on v.id=u.wx_miniapp_Id where u.wx_miniapp_Id=#{miniappId}")
	@ResultMap("passion.dao.mapper.HpHospitalUserMapper.BaseResultMap")
	HpHospitalUser getHospitalPublicityHospitalSuperadminUser(String miniappId);

	/**
	 * 应用列表
	 */
	@Select("select a.use_endtime useEndtime,a.committed_template_id committedTemplateId,u.lastest_template_id lastestTemplateId,a.id miniappId,a.template_id templateId,u.icon templateIcon,u.name templateName,a.head_img headImg,a.nick_name nickName,a.authorized,a.released,a.audit_status auditStatus,a.audit_fail_reason auditFailReason,a.code_commit codeCommit,a.submit_audit submitAudit,a.bind_time bindTime from t_wx_miniapp a inner join t_shopapp u on u.id=a.shopapp_id where a.user_id=#{userId} order by a.authorization_time desc limit #{page.start},#{page.pageSize}")
	List<Map> apps(@Param("userId") String userId, @Param("page") PagingPage page);

	/**
	 * 应用总数
	 */
	@Select("select count(a.id) from t_wx_miniapp a inner join t_shopapp u on u.id=a.shopapp_id where a.user_id=#{userId}")
	Long appCount(@Param("userId") String userId);

	@Select("select access_token from t_wx_miniapp where id=#{id}")
	@ResultType(String.class)
	String getAccessTokenById(@Param("id") String id);

	@Select("select m.audit_id auditId,m.access_token accessToken,m.user_id userId  from t_wx_miniapp m  where m.id=#{id}")
	@ResultType(GetCommitCodeInfoById.class)
	GetGetAuditStatusInfoById getGetAuditStatusInfoById(@Param("id") String id);

	public static class GetGetAuditStatusInfoById {
		public Integer auditId;
		public String accessToken;
		public String userId;

		public void setAuditId(Integer auditId) {
			this.auditId = auditId;
		}

		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

	}

	@Select("select m.app_id appId,m.access_token accessToken from t_wx_miniapp m  where m.id=#{id}")
	@ResultType(GetCommitCodeInfoById.class)
	GetCommitCodeInfoById getCommitCodeInfoById(@Param("id") String id);

	public static class GetCommitCodeInfoById {
		public String accessToken;
		public String appId;

		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}

		public void setAppId(String appId) {
			this.appId = appId;
		}

	}

	@Update("update t_wx_miniapp set audit_status=#{status},audit_fail_reason=#{failReason} where audit_id=#{auditId}")
	int updateAuditInfoByAuditId(@Param("status") int status, @Param("failReason") String failReason,
			@Param("auditId") int auditId);

	@Update("update t_wx_miniapp set authorized=0,access_token=null,expires_in=null,refresh_token=null,func_info=null,authorization_time=null where app_id=#{appId}")
	int unauthorized(@Param("appId") String appId);

	@Update("update t_wx_miniapp set code_commit=1,committed_template_id=#{templateId} where id=#{miniappId}")
	int codeCommitted(@Param("miniappId") String miniappId, @Param("templateId") String templateId);

	@Update("update t_wx_miniapp set auditted_template_id=committed_template_id,submit_audit=1,audit_id=#{auditId},audit_status=2,audit_fail_reason=null where id=#{miniappId}")
	int submitAudit(@Param("miniappId") String miniappId, @Param("auditId") long auditId);

	@Update("update t_wx_miniapp set template_id=auditted_template_id,released=1,show_status=1 where id=#{id}")
	int release(@Param("id") String id);

	@Update("update t_wx_miniapp set access_token=#{accessToken},expires_in=#{expiresIn},refresh_token=#{refreshToken} where id=#{id}")
	int refreshAccessToken(@Param("accessToken") String accessToken, @Param("expiresIn") int expiresIn,
			@Param("refreshToken") String refreshToken, @Param("id") String id);

	@Select("select id,refresh_token refreshToken,app_id appId from t_wx_miniapp where authorized=1 and ((authorization_time + expires_in*1000) - rpad(REPLACE(unix_timestamp(now(3)),'.',''),13,'0')) <= 600  limit 0,50")
	@ResultType(GetWaitingRefreshAccessTokenId.class)
	List<GetWaitingRefreshAccessTokenId> getWaitingRefreshAccessTokenIds();

	public static class GetWaitingRefreshAccessTokenId {
		public String refreshToken;
		public String appId;
		public String id;

		public void setRefreshToken(String refreshToken) {
			this.refreshToken = refreshToken;
		}

		public void setAppId(String appId) {
			this.appId = appId;
		}

		public void setId(String id) {
			this.id = id;
		}

	}

	@Select("select m.user_id userId,m.shopapp_id shopappId  from  t_wx_miniapp m  where m.id=#{miniappId}")
	@ResultType(GetCommitCodeInfoById.class)
	GetMiniappManagementInfo getMiniappManagementInfo(@Param("miniappId") String miniappId);

	public static class GetMiniappManagementInfo {
		public String userId;
		public String shopappId;

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public void setShopappId(String shopappId) {
			this.shopappId = shopappId;
		}


	}

	@Select("select count(*) from t_wx_miniapp where id=#{id} for update")
	@ResultType(Integer.class)
	int lockRowForCountById(@Param("id") String id);

	@Select("select * from t_wx_miniapp where app_id=#{appId}")
	@ResultMap("passion.dao.mapper.WxMiniappMapper.BaseResultMap")
	WxMiniapp selectByAppId(@Param("appId") String appId);

	@Select("select * from t_wx_miniapp where user_name=#{userName}")
	@ResultMap("passion.dao.mapper.WxMiniappMapper.BaseResultMap")
	WxMiniapp selectByUserName(String userName);
}