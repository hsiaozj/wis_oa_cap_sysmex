package com.seeyon.apps.bjev.services;

import java.util.List;
import java.util.Map;

public interface FormServices {
	
	public void pushMessage(String message, String loginName, String registerCode) throws Exception;
	
	public List<Map> getValueByMap(String[] returnFields, Map<String, Object> hm, String tableName, String fieldsLogic)
			throws Exception;
	
	public List<Map> selectDataBySql(String sql) throws Exception;
}
