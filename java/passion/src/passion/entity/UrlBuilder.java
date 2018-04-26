package passion.entity;

import java.util.HashMap;
import java.util.Map;

public class UrlBuilder {
	private StringBuilder url;
	private Map<String, String> queryParams;

	public UrlBuilder(String url) {
		this.url = new StringBuilder(url);
		queryParams = new HashMap<String, String>();
	}

	public UrlBuilder addQueryParam(String name, String value) {
		if (name != null) {
			if (queryParams.isEmpty()) {
				url = url.append("?");
			} else
				url = url.append("&");
			url = url.append(name).append("=").append(value);
			queryParams.put(name, value);
		}
		return this;
	}

	public String getQueryParam(String name) {
		return queryParams.get(name);
	}

	public UrlBuilder addQueryParam(String name, String[] values) {
		if (name != null) {
			for (String valueTmp : values) {
				if (queryParams.isEmpty()) {
					url = url.append("?");
				} else
					url = url.append("&");
				url = url.append(name).append("=").append(valueTmp);
			}
		}
		return this;
	}

	public String getUrl() {
		return url.toString();
	}
}
