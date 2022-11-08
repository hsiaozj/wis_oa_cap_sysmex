package com.seeyon.apps.bjev.services.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.bjev.common.ClientResource;
import com.seeyon.client.CTPRestClient;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.form.modules.engin.base.formData.FormDataDAO;
import com.bjev.apps.service.DataHandleService;


public abstract class DataHandleSyncDataImpl implements DataHandleService{
	private static Log log = LogFactory.getLog(DataHandleSyncDataImpl.class);
	
	private FormDataDAO formDataDAO = (FormDataDAO)AppContext.getBean("formDataDAO");	
	
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	 
	/**
	 * 填充RFC/BAPI同步底表映射配置信息
	 * @param map
	 * @return
	 * @throws Exception
	 */
	 public Map<String, Object> fillSapConfigInfos(Map<String, Object> map) throws Exception{
		return null;
	 }
	 
	 
	/**
	 * 填充REST同步底表映射配置信息
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> fillRestConfigInfos(Map<String, Object> map) throws Exception{
		return null;
	}
	
	/**
	 * 填充webservice同步底表映射配置信息
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> fillWebserviceConfigInfos(Map<String, Object> map) throws Exception{
		return null;
	}


	public List<Map> getValueByMap(String[] returnFields, Map<String, Object> hm, String tableName, String fieldsLogic)
			throws Exception {
		return formDataDAO.getValueByMap(returnFields, hm, tableName, fieldsLogic);
	}

	public void syncData(Map<String, Object> paramMap) throws Exception {
		Map<String, Object> procResult = (Map<String, Object>)paramMap.get("procResult");
		Map<String, Object> configMap = (Map<String, Object>)paramMap.get("configMap");
		Map<String, Object> commonParamMap = (Map<String, Object>)configMap.get("commonParamMap");
		List<String[]> primaryKeyList = (List<String[]>)configMap.get("primaryKeyList");
		String tableCode = (String)configMap.get("tableCode");
		
		JSONArray rst4Updates = (JSONArray)procResult.get("rst4Updates");
		
		Iterator<Object> updateIterator = rst4Updates.iterator();
		while (updateIterator.hasNext()) {
			List<Map> list = null;
			JSONObject updateObj = (JSONObject) updateIterator.next();
			JSONObject jsonMainTable = (JSONObject) updateObj.get("mainTable");
			if(jsonMainTable == null || jsonMainTable.size() == 0) {
				continue;
			}
			
			JSONObject formmainDatas = (JSONObject) jsonMainTable.get("values");

			String formmainTableName = jsonMainTable.getString("tableName");
			Map<String, Object> mainDataMap = formmainDatas.toJavaObject(Map.class);
			if(StringUtils.isBlank(formmainTableName)) {
				continue;
			}

			Map dataMap = new HashMap();
			if(primaryKeyList != null && primaryKeyList.size()>0) {
				for(String [] keys : primaryKeyList) {
					dataMap.put(keys[1], mainDataMap.get(keys[0]));
				}
				list = this.getValueByMap(null, dataMap, formmainTableName, "and");
			}
			
			if(list != null && list.size() > 0) {
				Map valMap = list.get(0);
	    		Long formmainId = 0l;
	    		Object id = valMap.get("id");
				if(id instanceof BigDecimal) {
					formmainId = ((BigDecimal)id).longValue();
				}else if(id instanceof Long) {
					formmainId = (Long)id;
				}
	    		this.updateData(tableCode, formmainId, formmainTableName, updateObj, commonParamMap);
			}else {
				this.addData(tableCode, formmainTableName, updateObj, commonParamMap);
			}
		}
	}
	
	/**
	 * 同步底表是添加新数据
	 * @param tableCode
	 * @param tableName
	 * @param jsonObject
	 * @param oaTableMappingRestMap
	 * @param commonParamMap
	 */
	public void addData(String tableCode, String tableName,JSONObject jsonObject, Map<String, Object> commonParamMap) {
		try {
			String xml = this.getDataXml(tableName, jsonObject);
			log.info("addData:"+xml);
			CTPRestClient client = (CTPRestClient)commonParamMap.get("restClient");
			if(client == null) {
				client = ClientResource.getInstance().resouresClent();
			}
			String syncAccount = (String)commonParamMap.get("syncAccount");
			if(StringUtils.isBlank(syncAccount)) {
				syncAccount = ClientResource.getInstance().getBinduser();
			}
			Map res = new HashMap();
			res.put("loginName", syncAccount);
			res.put("dataXml", xml);
			
			String result = client.post("form/import/"+tableCode, res, String.class);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 同步底表时更新已有数据
	 * @param tableCode
	 * @param formmainId
	 * @param tableName
	 * @param jsonObject
	 * @param oaTableMappingRestMap
	 * @param commonParamMap
	 */
	public void updateData(String tableCode, Long formmainId, String tableName,JSONObject jsonObject, Map<String, Object> commonParamMap) {
		try {
			String xml = this.getDataXml(tableName, jsonObject);
			log.info("updateData:"+xml);
			CTPRestClient client = (CTPRestClient)commonParamMap.get("restClient");
			if(client == null) {
				client = ClientResource.getInstance().resouresClent();
			}
			String syncAccount = (String)commonParamMap.get("syncAccount");
			if(StringUtils.isBlank(syncAccount)) {
				syncAccount = ClientResource.getInstance().getBinduser();
			}
			Map res = new HashMap();
			res.put("templateCode", tableCode);
			res.put("moduleId", formmainId+"");//主表ID(String型)
			res.put("loginName", syncAccount);//OA人员登录名(String型)
			res.put("dataXml", xml);//更新字段XML信息(String型)
			
			String result = client.put("form/update", res, String.class);
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public String getDataXml(String tableName,JSONObject jsonObject) throws Exception {
		JSONObject jsonMainTable = (JSONObject) jsonObject.get("mainTable");
		JSONObject formmainDatas = (JSONObject) jsonMainTable.get("values");	
		JSONObject subTableData = (JSONObject)jsonObject.get("subTableData");

		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		xml.append("<forms version=\"2.1\">");
		xml.append("<formExport>");
		xml.append("<summary  name=\""+tableName+"\"/>");
		xml.append("<definitions/>");
		xml.append("<values>");
		
		//拼接主表值
		String mainTableExtVal = this.getExtXmlVal("", formmainDatas);
		if(StringUtils.isNotBlank(mainTableExtVal)) {
			xml.append(mainTableExtVal);
		}
		xml.append("</values>");
		xml.append("<subForms>");
		
		if(subTableData != null && subTableData.size() > 0) {
			//拼接从表字段定义
			Iterator<Entry<String, Object>> subTableIter = subTableData.entrySet().iterator();
			while(subTableIter.hasNext()) {
				Entry<String, Object> entry = subTableIter.next();
				String subTableName = entry.getKey();
				JSONArray subDatas = (JSONArray)entry.getValue();
				xml.append("<subForm>");
				xml.append("<definitions/>");
				xml.append("<values>");
				if(subDatas == null || subDatas.size()==0) {
					subDatas = new JSONArray();
					subDatas.add(new JSONObject());
				}
				//拼接从表值
				for(int i = 0; i < subDatas.size(); i++) {
					JSONObject data = (JSONObject)subDatas.get(i);
					String isHas = data.getString("isHas");
					Long formsonId = data.getLong("formsonId");
					String titleStr = "";
					xml.append("<row>");
					if(StringUtils.isNotBlank(isHas) && "1".equals(isHas)) {
						titleStr="<title><![CDATA["+formsonId+"]]></title>";
					}
					
					String extVal = this.getExtXmlVal(titleStr, data);
					if(StringUtils.isNotBlank(extVal)) {
						xml.append(extVal);
					}
					xml.append("</row>");
				}
				xml.append("</values>");
				xml.append("</subForm>");
			}
		}
		xml.append("</subForms>");
		xml.append("</formExport>");
		xml.append("</forms>");
		
		String xmlStr = xml.toString();
		xmlStr = xmlStr.replaceAll("(<!\\[CDATA\\[null\\]\\]>|<!\\[CDATA\\[\\]\\]>)", "");
		return xmlStr;
	}
	   
	   
	 public String getExtXmlVal(String title, JSONObject data)throws Exception{
		StringBuffer xml = new StringBuffer();
		Iterator<Entry<String, Object>> dataIters = data.entrySet().iterator();
		while(dataIters.hasNext()) {
			Entry<String, Object> entry = dataIters.next();
			String key = entry.getKey();
			Object val = entry.getValue();
			
			Date tempDate = null;
			String valStr = null;
			if(val instanceof Date) {
				tempDate = (Date)val;
			}else if(val instanceof Long) {
				Long l = (Long)val;
				valStr = (l == null ? "" : l.longValue()+"");
			}else if(val instanceof Integer) {
				Integer in = (Integer)val;
				valStr = (in == null ? "" : in.intValue()+"");
			}else if(val instanceof BigDecimal) {
				BigDecimal bd = (BigDecimal)val;
				valStr = (bd == null ? "" : bd.toString());
			}else if(val instanceof Boolean) {
				Boolean bl = (Boolean)val;
				valStr = (bl == null ? "" : bl.toString());
			}else{
				valStr = (String)val;
			}
	
			if(tempDate != null) {
				valStr = formatter.format(tempDate);
			}
			xml.append("<column  name=\""+key+"\" ><value><![CDATA["+valStr+"]]></value>"+title+"</column>");
		}
  		return xml.toString();
   }
	
	@Override
	public Map<String, Object> getWriteBackConfig(Map<String, Object> map) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
