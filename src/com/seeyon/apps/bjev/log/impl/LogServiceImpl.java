package com.seeyon.apps.bjev.log.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.seeyon.apps.bjev.common.ClientResource;
import com.seeyon.apps.bjev.log.LogService;
import com.seeyon.apps.bjev.util.CAP4FormKit;
import com.seeyon.apps.bjev.util.WsFlow;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.client.CTPRestClient;
import com.seeyon.ctp.common.AppContext;

public class LogServiceImpl implements LogService{
	private static final Logger log = Logger.getLogger(LogServiceImpl.class);
	
	private CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public void addLog(Map<String, Object> logInfo) {
		try {
			String tableCode = (String)logInfo.get("tableCode");
			if(StringUtils.isBlank(tableCode)) {
				tableCode = WsFlow.getSth("err_log");
			}
			
			Map<String, Object> paramMap = new HashMap();
			paramMap.putAll(logInfo);
			
			if(logInfo.containsKey("tableCode")) {
				paramMap.remove("tableCode");
			}

			FormBean cap4formBean = CAP4FormKit.getFormBean(cap4FormManager, tableCode);
			String tableName = cap4formBean.getMasterTableBean().getTableName();
			
			String xml = this.getDataXml(tableName, paramMap);
			log.info("LogServiceImpl:xml:"+xml);
			
			CTPRestClient client = ClientResource.getInstance().resouresClent();
			String syncAccount = ClientResource.getInstance().getBinduser();
			
			Map res = new HashMap();
			res.put("loginName", syncAccount);
			res.put("dataXml", xml);
			
			String result = client.post("form/import/"+tableCode, res, String.class);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private String getDataXml(String tableName, Map<String, Object> paramMap) throws Exception{
		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		xml.append("<forms version=\"2.1\">");
		xml.append("<formExport>");
		xml.append("<summary  name=\""+tableName+"\"/>");
		xml.append("<definitions/>");
		xml.append("<values>");

		Iterator<Entry<String, Object>> iter = paramMap.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Object> entry = iter.next();
			String key = entry.getKey();
			Object val = entry.getValue();
			
			Date tempDate = null;
			String valStr = null;
			if(val instanceof Date) {
				tempDate = (Date)val;
			}else if(val instanceof Long) {
				Long l = (Long)val;
				valStr = l.longValue()+"";
			}else if(val instanceof Integer) {
				Integer in = (Integer)val;
				valStr = in.intValue()+"";
			}else if(val instanceof BigDecimal) {
				BigDecimal bd = (BigDecimal)val;
				valStr = bd.toString();
			}else{
				valStr = (String)val;
			}
	
			if(tempDate != null) {
				valStr = formatter.format(tempDate);
			}
			xml.append("<column  name=\""+key+"\" ><value><![CDATA["+valStr+"]]></value></column>");
		}
		xml.append("</values>");
		xml.append("<subForms>");
		xml.append("</subForms>");
		xml.append("</formExport>");
		xml.append("</forms>");
		
		String xmlStr = xml.toString();
		xmlStr = xmlStr.replaceAll("(<!\\[CDATA\\[null\\]\\]>|<!\\[CDATA\\[\\]\\]>)", "");
		return xmlStr;
	}
	
}
