
package saaspe.utils;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import saaspe.model.DocusignUserCache;

@Component
public class RedisUtility {
	@Autowired
	private RedisTemplate<String, String> template;

	@Autowired
	private ObjectMapper objectMapper;

	public void setValue(final String key, TokenCache cache, final Date date) {
		try {
			String json = objectMapper.writeValueAsString(cache);
			template.opsForValue().set(key, json);
			template.expireAt(key, date);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public TokenCache getValue(final String key) {
		String json = template.opsForValue().get(key);
		if (json != null) {
			try {
				return objectMapper.readValue(json, TokenCache.class);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void setDocusingValue(final String key, final DocusignUserCache cache) {
		try {
			String json = objectMapper.writeValueAsString(cache);
			template.opsForValue().set(key, json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	public DocusignUserCache getDocusignValue(final String key) {
		String json = template.opsForValue().get(key);
		if (json != null) {
			try {
				return objectMapper.readValue(json, DocusignUserCache.class);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void deleteKeyFromRedis(String key) {
		template.delete(key);
	}
}
