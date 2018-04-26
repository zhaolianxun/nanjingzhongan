package easywin.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import easywin.constant.SysConstant;
import easywin.entity.BaseResVo;
import easywin.entity.InteractRuntimeException;

public class HttpRespondWithData {

	public static Logger logger = Logger.getLogger(HttpRespondWithData.class);

	/**
	 * 接口正常返回数据
	 * 
	 * @param request
	 * @param response
	 * @param code
	 * @param codeMsg
	 * @param data
	 * @throws IOException
	 */
	public static void todo(HttpServletRequest request, HttpServletResponse response, int code, String codeMsg,
			Object data) throws IOException {
		// 组装返回数据
		BaseResVo resVo = new BaseResVo();
		resVo.setCode(code);
		resVo.setCodeMsg(codeMsg);
		resVo.setRequestId((String) request.getAttribute("requestId"));
		resVo.setData(data);

		String resString = JSON.toJSONString(resVo, SerializerFeature.WriteMapNullValue);
		logger.info(resString);

		// 通过response返回输出
		response.setCharacterEncoding(SysConstant.SYS_CHARSET);
		response.setStatus(200);
		response.setContentType("application/json;charset=" + SysConstant.SYS_CHARSET);
		response.getWriter().write(resString);
	}

	/**
	 * 当接口中抛出异常通过该方法返回相应数据
	 * 
	 * @param request
	 * @param response
	 * @param ex
	 * @throws IOException
	 */
	public static void exception(HttpServletRequest request, HttpServletResponse response, Exception ex)
			throws IOException {
		// 通过异常对象ex找到对应错误信息
		int code = 0;
		String codeMsg = null;
		Object data = null;
		// InteractRuntimeException是我自己写的类，继承了RuntimeException，同时加入了新的成员变量code
		// 接口里人为抛出的异常均为InteractRuntimeException对象(throw new
		// InteractRuntimeException(20)),20就是code,code的值是多少是自己定义的，不同的地方不同的code
		if (ex instanceof InteractRuntimeException) {
			InteractRuntimeException ire = (InteractRuntimeException) ex;
			code = ire.getCode();
			codeMsg = StringUtils.trim(ire.getMessage());
			data = ire.getData();
		} else {
			code = 98;
		}

		// 如果ex中message为空，则查询配置文件中code对应的错误消息
		if (StringUtils.isEmpty(codeMsg)) {
			if (request.getLocale().getLanguage().equals("zh")
					|| StringUtils.isEmpty(request.getHeader("accept-language")))
				codeMsg = SysConstant.errorCodeCnMapper.getProperty(code + "");
			else
				codeMsg = SysConstant.errorCodeEnMapper.getProperty(code + "");
		}

		// 组装返回数据
		BaseResVo resVo = new BaseResVo();
		resVo.setCode(code);
		resVo.setCodeMsg(codeMsg);
		resVo.setRequestId((String) request.getAttribute("requestId"));
		resVo.setData(data);

		String resString = JSON.toJSONString(resVo, SerializerFeature.WriteMapNullValue);
		logger.info(resString);

		// 通过response返回输出
		response.setCharacterEncoding(SysConstant.SYS_CHARSET);
		response.setStatus(200);
		response.setContentType("application/json;charset=" + SysConstant.SYS_CHARSET);
		response.getWriter().write(resString);
	}

	public static void main(String[] args) {

	}
}
