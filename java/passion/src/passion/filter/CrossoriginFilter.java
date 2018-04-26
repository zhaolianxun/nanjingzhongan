package passion.filter;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

/**
 * Servlet Filter implementation class CrossOriginFilter
 */
public class CrossoriginFilter implements Filter {
	private Pattern exclude = null;

	/**
	 * Default constructor.
	 */
	public CrossoriginFilter() {
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
		if (exclude != null && exclude.matcher(httpReq.getRequestURI()).find()) {
			chain.doFilter(request, response);
			return;
		}

		HttpServletResponse hResp = (HttpServletResponse) response;
		hResp.setHeader("Access-Control-Allow-Origin", "*");
		hResp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,PUT");
		hResp.setHeader("Access-Control-Max-Age", "3600");
		hResp.setHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");

		// pass the request along the filter chain
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		String excludeParam = fConfig.getInitParameter("exclude");
		if (StringUtils.isNotEmpty(excludeParam))
			exclude = Pattern.compile(excludeParam);
	}

}
