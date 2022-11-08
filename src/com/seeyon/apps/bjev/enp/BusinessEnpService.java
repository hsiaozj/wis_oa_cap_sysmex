package com.seeyon.apps.bjev.enp;

import java.util.Map;

public interface BusinessEnpService {
	
	/**
	 * 前置预处理方法
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> preProcessMethod(Map<String, Object> paramMap) throws Exception; 
	
	/**
	 * 结束后处理方法
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> finishedProcessMethod(Map<String, Object> paramMap) throws Exception; 
}
