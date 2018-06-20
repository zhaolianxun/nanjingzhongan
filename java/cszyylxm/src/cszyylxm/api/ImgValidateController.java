package cszyylxm.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import redis.clients.jedis.Jedis;
import cszyylxm.constant.SysConstant;
import cszyylxm.util.HttpRespondWithData;
import cszyylxm.util.ImgValidateCode;

@Controller("api.ImgValidationCode")
@RequestMapping(value = "/imgvalidationcode")
public class ImgValidateController {

	public static Logger logger = Logger.getLogger(ImgValidateController.class);

	/**
	 * 加载1级商品分类
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/new")
	public void goodTypeEntrance(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Jedis jedis = null;
		try {
			// 获取请求参数

			// 业务处理
			jedis = SysConstant.jedisPool.getResource();
			// 查詢主轮播图
			ImgValidateCode ivc = new ImgValidateCode();
			String code = ivc.createCode();
			jedis.setex(code, 5 * 60, code);
			jedis.close();
			jedis = null;
			// 返回结果
			ivc.write(response.getOutputStream());
		} catch (Exception e) {
			// 处理异常
			logger.info(ExceptionUtils.getStackTrace(e));
			HttpRespondWithData.exception(request, response, e);
		} finally {
			// 释放资源
			if (jedis != null)
				jedis.close();
		}
	}

}
