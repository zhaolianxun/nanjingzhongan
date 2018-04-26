package zasellaid.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import zasellaid.constant.SysConstant;

@Component
public class SpringContextInlay implements ApplicationContextAware{
	
	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		SysConstant.springContext = arg0;
	}
}
