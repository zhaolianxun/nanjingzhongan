package passion.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import passion.constant.SysConstant;

/**
 * Servlet Filter implementation class LoggerFilter
 */
public class LoggerFilter implements Filter {
	private static Logger logger = Logger.getLogger(LoggerFilter.class);

	/**
	 * Default constructor.
	 */
	public LoggerFilter() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpReq = (HttpServletRequest) request;

		request.setCharacterEncoding(SysConstant.SYS_CHARSET);
		Date now = new Date();

		String requestId = SysConstant.sdf1.format(now) + RandomStringUtils.randomNumeric(4);

		request.setAttribute("requestId", requestId);
		request.setAttribute("requestTime", now);

		logger.info(new StringBuilder().append("track new request ").append(requestId).append(" 【")
				.append(httpReq.getMethod()).append(" ").append(httpReq.getRequestURI()).append("?")
				.append(StringUtils.trimToEmpty(httpReq.getQueryString())).append("】").toString());

		chain.doFilter(request, response);

		logger.info(new StringBuilder("the request ").append(requestId).append(" takes ")
				.append(new Date().getTime() - now.getTime()).append("ms").toString());
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

	public static void main(String[] args) {
		String a = "a" + 1;
		String s = new StringBuilder("a" + 1).toString();
		String f = new StringBuilder("a" + 1).toString();
		System.out.println(s == f);
		a(new StringBuilder("a" + 1).toString());
	}

	private static void a(String s) {
		String a = "a" + 1;
		System.out.println(a == s);
	}
}
