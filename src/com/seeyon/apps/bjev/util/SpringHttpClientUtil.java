package com.seeyon.apps.bjev.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

public class SpringHttpClientUtil {
	private static RestTemplate restTemplate;
	private static SimpleClientHttpRequestFactory factory = null;
	private static SpringHttpClientUtil instance = new SpringHttpClientUtil();
	
	static {
		factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(300000);
        factory.setReadTimeout(300000);
		restTemplate = new RestTemplate(factory);
		restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
			@Override
				public void handleError(ClientHttpResponse response) throws IOException{
				if(response.getRawStatusCode() != 401){
					super.handleError(response);
				}
			}
		});
	}


	public static SpringHttpClientUtil getInstance() {
		return instance;
	}
	
	public <T> T post(String url, Object paramObj, Class<T> responseType) throws Exception{
		ResponseEntity<T> entity = restTemplate.postForEntity(url, paramObj, responseType);
		if(entity != null) {
			throwException(entity);
			return entity.getBody();
		}
		return null;
	}
	
	public <T> T get(String url, Class<T> responseType) throws Exception{
		ResponseEntity<T> entity = restTemplate.getForEntity(url, responseType);
		if(entity != null) {
			throwException(entity);
			return entity.getBody();
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public <T> T exchange(String url, HttpMethod method, HttpEntity requestEntity, 
			Class<T> responseType, Map<String, Object> uriVariables) throws Exception {
		ResponseEntity<T> entity = restTemplate.exchange(url, method, requestEntity, responseType, uriVariables);
		if(entity != null) {
			throwException(entity);
			return entity.getBody();
		}
		return null;
	}
	
	@SuppressWarnings("static-access")
	private void throwException(@SuppressWarnings("rawtypes") ResponseEntity response) throws Exception {
		HttpStatus httpStatus = response.getStatusCode();
		if ((httpStatus.OK.value() != 200) && (httpStatus.INTERNAL_SERVER_ERROR.value() != 201) && (httpStatus.INTERNAL_SERVER_ERROR.value() != 301)) {
			throw new RuntimeException(
					"Failed : HTTP error code : " + httpStatus.toString() + " . " + response.toString());
		}
	}
	
}

