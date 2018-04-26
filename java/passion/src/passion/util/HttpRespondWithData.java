package passion.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import passion.constant.SysConstant;
import passion.entity.BaseResVo;
import passion.entity.InteractRuntimeException;

public class HttpRespondWithData {

	public static Logger logger = Logger.getLogger(HttpRespondWithData.class);

	public static void todo(HttpServletRequest request, HttpServletResponse response, int code, String codeMsg,
			Object data) throws IOException {
		BaseResVo resVo = new BaseResVo();
		resVo.setCode(code);
		resVo.setCodeMsg(codeMsg);
		resVo.setRequestId((String) request.getAttribute("requestId"));
		resVo.setData(data);

		String resString = JSON.toJSONString(resVo, SerializerFeature.WriteMapNullValue);
		logger.info(resString);

		response.setCharacterEncoding(SysConstant.SYS_CHARSET);
		response.setStatus(200);
		response.setContentType("application/json;charset=" + SysConstant.SYS_CHARSET);
		response.getWriter().write(resString);
	}

	public static void exception(HttpServletRequest request, HttpServletResponse response, Exception ex)
			throws IOException {
		int code = 0;
		String codeMsg = null;
		Object data = null;
		if (ex instanceof InteractRuntimeException) {
			InteractRuntimeException ire = (InteractRuntimeException) ex;
			code = ire.getCode();
			if (SysConstant.project_environment.equals("test"))
				codeMsg = StringUtils.trim(new StringBuilder(ire.getMessage()).append(" : ")
						.append(ExceptionUtils.getStackTrace(ire)).toString());
			else
				codeMsg = StringUtils.trim(ire.getMessage());
			data = ire.getData();
		} else {
			code = 98;
		}

		if (StringUtils.isEmpty(codeMsg)) {
			if (request.getLocale().getLanguage().equals("zh")
					|| StringUtils.isEmpty(request.getHeader("accept-language")))
				codeMsg = SysConstant.errorCodeCnMapper.getProperty(code + "");
			else
				codeMsg = SysConstant.errorCodeEnMapper.getProperty(code + "");
		}

		BaseResVo resVo = new BaseResVo();
		resVo.setCode(code);
		resVo.setCodeMsg(codeMsg);
		resVo.setRequestId((String) request.getAttribute("requestId"));
		resVo.setData(data);

		String resString = JSON.toJSONString(resVo, SerializerFeature.WriteMapNullValue);
		logger.info(resString);

		response.setCharacterEncoding(SysConstant.SYS_CHARSET);
		response.setStatus(200);
		response.setContentType("application/json;charset=" + SysConstant.SYS_CHARSET);
		response.getWriter().write(resString);
	}

	public static void main(String[] args) {

	}
}
