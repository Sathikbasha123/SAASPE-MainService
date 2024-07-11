package saaspe.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("docusign-urls")
public class UrlConfig {

	private List<UrlEntry> urls = new ArrayList<>();

	public List<UrlEntry> getUrls() {
		return urls;
	}

	public void setUrls(List<UrlEntry> urls) {
		this.urls = urls;
	}

	public static class UrlEntry {
		private String name;
		private String url;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
	}
}
