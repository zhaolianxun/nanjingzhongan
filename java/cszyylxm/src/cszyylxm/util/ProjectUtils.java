package cszyylxm.util;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class ProjectUtils {

	public static boolean equalsWithAny(CharSequence cs, CharSequence... css) {
		for (CharSequence c : css) {
			if (cs == c || cs.equals(c))
				return true;
		}
		return false;
	}

	public static void zhenNaWebMeiBanFa(String str) {
		if ("undefined".equals(str))
			throw new InteractRuntimeException("又淘气了，你传个'undefined'叫我如何是好呢");
		if ("null".equals(str))
			throw new InteractRuntimeException("又淘气了，你传个'null'叫我如何是好呢");
	}

	public static Map<String, String> URLRequest(String URL) {
		URL = URLDecoder.decode(URL);
		Map<String, String> mapRequest = new HashMap<String, String>();

		String[] arrSplit = null;

		String[] ss = URL.split("\\?");
		if (ss.length <= 1)
			return mapRequest;
		if (ss.length > 2)
			return mapRequest;

		String strUrlParam = ss[1];
		if (strUrlParam == null || strUrlParam.isEmpty()) {
			return mapRequest;
		}
		// 每个键值为一组 www.2cto.com
		arrSplit = strUrlParam.split("[&]");
		for (String strSplit : arrSplit) {
			String[] arrSplitEqual = strSplit.split("[=]");

			// 解析出键值
			if (arrSplitEqual.length > 1) {
				// 正确解析
				mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
			} else {
				if (arrSplitEqual[0] != "") {
					// 只有参数没有值，不加入
					mapRequest.put(arrSplitEqual[0], "");
				}
			}
		}
		return mapRequest;
	}
}
