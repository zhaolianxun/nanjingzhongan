package zasellaid.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;



@Controller("zasellaid.TestController")
@RequestMapping(value = "/test")
public class TestController {

	public static Logger logger = Logger.getLogger(TestController.class);

	@RequestMapping(value = "/test1", method = RequestMethod.POST)
	public void provinces(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");//注册驱动
		String url = "jdbc:mysql://localhost:3306/mydb1";
		String username = "";
		String password ="";
		Connection con = DriverManager.getConnection(url, username, password);
		PreparedStatement ps =	con.prepareStatement("ss");
	}
}
