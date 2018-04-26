package passion.module.hospitalpublicity.hospital.api;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import passion.module.client.api.BaseController;

@Controller("passion.module.hospitalpublicity.hospital.api.ClientUser")
@RequestMapping(value = "/hp/h/clientuser")
public class ClientUser extends BaseController {

	public static Logger logger = Logger.getLogger(ClientUser.class);

}
