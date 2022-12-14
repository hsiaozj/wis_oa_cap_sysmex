package com.seeyon.apps.bjev.services.webService.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bjev.apps.util.XmlUtil;
import com.seeyon.apps.bjev.services.WsBaseService;
import com.seeyon.apps.bjev.services.impl.DataHandleSyncDataImpl;
import com.seeyon.apps.bjev.services.pojo.DataItem;
import com.seeyon.apps.bjev.services.pojo.InterfaceElem;
import com.seeyon.apps.bjev.services.pojo.Mdata;
import com.seeyon.apps.bjev.services.pojo.MdataOutput;
import com.seeyon.apps.bjev.services.pojo.WisBaseOutput;
import com.seeyon.apps.bjev.services.webService.services.MdataService;
import com.seeyon.apps.bjev.util.WsFlow;
import com.seeyon.apps.bjev.util.CAP4FormKit;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;

public class MdataServiceImpl extends DataHandleSyncDataImpl implements MdataService, WsBaseService{
	private static final Logger log = Logger.getLogger(MdataServiceImpl.class);
	
	private CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
	
	private static ExecutorService servicesPool = Executors.newFixedThreadPool(25);
	
	private static int constSize = Integer.parseInt(WsFlow.getSth("const_size"));
	
	private static String comanyMapping = WsFlow.getSth("companyMapping");
		
	private static Map<String, Object> comanyMappingConfig = new HashMap<String, Object>();
	
	@Override
	public MdataOutput syncMdata(Mdata input) throws Exception {
		return procMdata(input);
	}
	
	@Override
	public WisBaseOutput doWsWork(Object input) throws Exception {
		return syncMdata((Mdata)input);
	}

	private synchronized MdataOutput procMdata(Mdata input) {
		MdataOutput mdataOutput = new MdataOutput();
		mdataOutput.setResult("S");
		mdataOutput.setMessage("????????????");

		try {
			String xml = XmlUtil.getXml(input);
			log.info("syncMdata----:"+xml);
			
			String xmlString = input.getXmlString();
			
			if(StringUtils.isBlank(xmlString) && !xmlString.contains("Entry")) {
				mdataOutput.setResult("E");
				mdataOutput.setMessage("????????????");
				return mdataOutput;
			}
			if(xmlString.contains("&lt;") || xmlString.contains("&gt;")) {
				xmlString = xmlString.replace("&lt;", "<").replace("&gt;", ">");
			}
			log.info("xmlString----:"+xmlString);
			InterfaceElem elem = XmlUtil.getBean(xmlString, InterfaceElem.class);
			
			DataItem[] items = elem.getItems();
			if(items == null || items.length < 1) {
				mdataOutput.setResult("E");
				mdataOutput.setMessage("????????????");
				return mdataOutput;
			}
			mdataOutput.setId(input.getMsgid());
			
			if(comanyMappingConfig.size() == 0) {
//				log.info("initConfigMap-------------");
				initConfigMap(comanyMappingConfig);
			}
//			log.info("comanyMappingConfig:"+ JSONObject.toJSONString(comanyMappingConfig));
			
			Map<String, List<DataItem>> companyMappingMdata = new HashMap<String, List<DataItem>>();//?????????????????????
			for(DataItem dataItem : items) {
				String cflag = dataItem.getcFlag();
				String bukrs = dataItem.getBukrs();
				
				if(StringUtils.isNotBlank(cflag)) {
					cflag = cflag.replaceAll("\\s*", "").toUpperCase();
					Map<String, Object> relationInfoMap = this.getRelationInfoMap(bukrs, cflag);
					String tableCode = (String)relationInfoMap.get("tableCode");
					Map<String, Object> configMap = (Map<String, Object>)relationInfoMap.get("configMap");
					
					List<DataItem> mdataList = companyMappingMdata.get(bukrs+"_"+cflag);
					if(mdataList == null) {
						mdataList = new ArrayList<DataItem>();
					}
					if(mdataList.size() == constSize) {
						final List<DataItem> tempList = mdataList;
						servicesPool.execute(new ProcessDataService(tableCode, configMap, tempList));
						mdataList = new ArrayList<DataItem>(); 
					}else {
						mdataList.add(dataItem);
					}
					companyMappingMdata.put(bukrs+"_"+cflag, mdataList);
				}
			}
//			log.info("companyMappingMdata:"+ JSONObject.toJSONString(companyMappingMdata));
			//???????????????????????????
			if(companyMappingMdata.size() > 0) {
				Iterator<Entry<String, List<DataItem>>> iter = companyMappingMdata.entrySet().iterator();
				while(iter.hasNext()) {
					Entry<String, List<DataItem>> entry = iter.next();
					String key = entry.getKey();
					String[] tempArr = key.split("_");
					
					String bukrs = tempArr[0];//????????????
					String cflag = tempArr[1];//??????
					
					List<DataItem> dlist = entry.getValue();
					if(dlist.size() > 0) {
						Map<String, Object> relationInfoMap = this.getRelationInfoMap(bukrs, cflag);

						String tableCode = (String)relationInfoMap.get("tableCode");
						Map<String, Object> configMap = (Map<String, Object>)relationInfoMap.get("configMap");
						servicesPool.execute(new ProcessDataService(tableCode, configMap, dlist));
					}
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			mdataOutput.setResult("E");
			mdataOutput.setMessage("????????????-" + ex.getMessage());
		}
		return mdataOutput;
	}

	class ProcessDataService implements Runnable{
		private List<DataItem> dataList;
		
		private String tableCode;
		
		private Map<String, Object> configMap;
		
		public ProcessDataService(String tableCode, Map<String, Object> configMap, List<DataItem> dataList) {
			this.tableCode = tableCode;
			this.configMap = configMap;
			this.dataList = dataList;
		}
		
		@Override
		public void run() {
			try {
				configMap.put("dataList", dataList);
				configMap.put("tableCode", tableCode);
				configMap.put("commonParamMap", new HashMap());
				Map<String, Object> procResult = procMappingData(configMap);
				
				Map<String, Object> paramMap = new HashMap();
				paramMap.put("configMap", configMap);
				paramMap.put("procResult", procResult);
				
				syncData(paramMap);
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public Map<String, Object> procMappingData(Map<String, Object> map) throws Exception {
		Map<String, Object> result = new HashMap();
		
		String formmaintableName = "";
		JSONArray rst4Updates = new JSONArray();

		List<DataItem> dataList = (List<DataItem>)map.get("dataList");

		String tableCode = (String)map.get("tableCode");
		FormBean cap4formBean = CAP4FormKit.getFormBean(cap4FormManager, tableCode);
		formmaintableName = CAP4FormKit.getTableNameByDisPlayName(cap4formBean, "", true);
		
		for(DataItem dataItem : dataList) {
			JSONObject rst4Update = new JSONObject();
			
			JSONObject rowData = new JSONObject();
			rowData.put("tableName", formmaintableName.toLowerCase());
			JSONObject rowValues = new JSONObject();

			rst4Updates.add(rst4Update);
			
			String cflag = dataItem.getcFlag();
			if(StringUtils.isNotBlank(cflag)) {
				cflag = cflag.replaceAll("\\s*", "").toUpperCase();
			}
			String mdata = dataItem.getMdata();
			if(StringUtils.isBlank(mdata)) {
				continue;
			}
			
			switch(cflag) {
				case "C": //????????????
					rowValues.put("????????????", dataItem.getBukrs() == null ? "" : dataItem.getBukrs());
//					rowValues.put("??????????????????", mdata.replaceAll("\\s*", ""));
//					rowValues.put("??????????????????", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					rowValues.put("??????????????????", mdata.replaceAll("\\s*", ""));
					rowValues.put("??????????????????", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					break;
				case "W": //wbs
					rowValues.put("SAP????????????", dataItem.getBukrs() == null ? "" : dataItem.getBukrs());
					rowValues.put("WBS??????1", mdata.replaceAll("\\s*", ""));
					rowValues.put("????????????", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					break;
				case "V": //?????????
					rowValues.put("????????????", dataItem.getBukrs() == null ? "" : dataItem.getBukrs());
					rowValues.put("???????????????", mdata.replaceAll("\\s*", ""));
					rowValues.put("???????????????", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					rowValues.put("??????", dataItem.getMtext1() == null ? "" : dataItem.getMtext1());
					rowValues.put("????????????", dataItem.getMtext2() == null ? "" : dataItem.getMtext2());
					rowValues.put("????????????", dataItem.getMtext3() == null ? "" : dataItem.getMtext3());
					rowValues.put("??????", dataItem.getMtext4() == null ? "" : dataItem.getMtext4());
					break;
				case "M": //????????????
					rowValues.put("????????????", dataItem.getBukrs() == null ? "" : dataItem.getBukrs());
					rowValues.put("?????????????????????", mdata.replaceAll("\\s*", ""));
					rowValues.put("???????????????1", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					rowValues.put("???????????????2", dataItem.getMtext1() == null ? "" : dataItem.getMtext1());
					rowValues.put("???????????????3", dataItem.getMtext2() == null ? "" : dataItem.getMtext2());
					rowValues.put("???????????????4", dataItem.getMtext3() == null ? "" : dataItem.getMtext3());
					rowValues.put("???????????????5", dataItem.getMtext4() == null ? "" : dataItem.getMtext4());
					break;
				case "F": //????????????
					rowValues.put("????????????", dataItem.getBukrs() == null ? "" : dataItem.getBukrs());
					rowValues.put("?????????????????????", mdata.replaceAll("\\s*", ""));
					rowValues.put("???????????????1", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					rowValues.put("???????????????2", dataItem.getMtext1() == null ? "" : dataItem.getMtext1());
					rowValues.put("???????????????3", dataItem.getMtext2() == null ? "" : dataItem.getMtext2());
					rowValues.put("???????????????4", dataItem.getMtext3() == null ? "" : dataItem.getMtext3());
					rowValues.put("???????????????5", dataItem.getMtext4() == null ? "" : dataItem.getMtext4());
					break;
			}

			rowData.put("values", rowValues);
			rst4Update.put("mainTable", rowData);
		}
		result.put("rst4Updates", rst4Updates);
		return result;
	}
	
	/**
	 * ??????????????????????????????????????????????????????????????????
	 * @param bukrs
	 * @param cflag
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> getRelationInfoMap(String bukrs, String cflag) throws Exception{
		Map<String, Object> resultMap = new HashMap();
		Map<String, Object> markMappingConfig = (Map<String, Object>)comanyMappingConfig.get(bukrs);					
		Map<String, Map<String, Object>> codeMappingConfig = (Map<String, Map<String, Object>>)markMappingConfig.get(cflag);
		
		Iterator<Entry<String, Map<String, Object>>> iter = codeMappingConfig.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Map<String, Object>> entry = iter.next();
			String tableCode = entry.getKey();
			Map<String, Object> configMap = entry.getValue();
			resultMap.put("tableCode", tableCode);
			resultMap.put("configMap", configMap);
		}
		return resultMap;
	}
	
	private void initConfigMap(Map<String, Object> comanyMappingConfig) throws Exception{
		String[] arr = comanyMapping.split("\\|");
		for(String str : arr) {
			//1050:[wbs_001,fc_001,supplier_001,cp_001,fee_001]
			String[] tempArr = str.split(":");
			String comany = tempArr[0];
			//[wbs_001,fc_001,supplier_001,cp_001,fee_001]
			String temp = tempArr[1].replaceAll("(\\[|\\])", "");
			String[] codes = temp.split(",");

			Map<String, Object> markMappingConfig = new HashMap<String, Object>();
			for(String code : codes) {
				FormBean cap4formBean = CAP4FormKit.getFormBean(cap4FormManager, code);
				Map<String, Map<String, Object>> codeMappingConfig = new HashMap<String, Map<String, Object>>();
				
				if(code.contains("fc")) {//????????????
					List<String[]> cPrimaryKeyList = new ArrayList();
					Map<String, Object> cConfigMap = new HashMap();
					
					String fundCenterCodeField = CAP4FormKit.getFieldTaleId(cap4formBean, "??????????????????");
					String companyCodeField1 = CAP4FormKit.getFieldTaleId(cap4formBean, "????????????");
					cPrimaryKeyList.add(new String[] {"??????????????????", fundCenterCodeField});
					cPrimaryKeyList.add(new String[] {"????????????", companyCodeField1});
					cConfigMap.put("primaryKeyList", cPrimaryKeyList);
					
					codeMappingConfig.put(code, cConfigMap);
					markMappingConfig.put("C", codeMappingConfig);
				}else if(code.contains("wbs")) {//wbs
					List<String[]> wbsPrimaryKeyList = new ArrayList();
					Map<String, Object> wbsConfigMap = new HashMap();
					
					String wbsCodeField = CAP4FormKit.getFieldTaleId(cap4formBean, "WBS??????1");
					String companyCodeField2 = CAP4FormKit.getFieldTaleId(cap4formBean, "SAP????????????");
					wbsPrimaryKeyList.add(new String[] {"WBS??????1", wbsCodeField});
					wbsPrimaryKeyList.add(new String[] {"SAP????????????", companyCodeField2});
					wbsConfigMap.put("primaryKeyList", wbsPrimaryKeyList);
					
					codeMappingConfig.put(code, wbsConfigMap);
					markMappingConfig.put("W", codeMappingConfig);
				}else if(code.contains("supplier")) {//?????????
					List<String[]> vPrimaryKeyList = new ArrayList();
					Map<String, Object> vConfigMap = new HashMap();
					
					String supplierCodeField = CAP4FormKit.getFieldTaleId(cap4formBean, "???????????????");
					String companyCodeField3 = CAP4FormKit.getFieldTaleId(cap4formBean, "????????????");
					vPrimaryKeyList.add(new String[] {"???????????????", supplierCodeField});
					vPrimaryKeyList.add(new String[] {"????????????", companyCodeField3});
					vConfigMap.put("primaryKeyList", vPrimaryKeyList);
					
					codeMappingConfig.put(code, vConfigMap);
					markMappingConfig.put("V", codeMappingConfig);
				}else if(code.contains("cp")) {//????????????
					List<String[]> mPrimaryKeyList = new ArrayList();
					Map<String, Object> mConfigMap = new HashMap();
				
					String primaryKeyField1 = CAP4FormKit.getFieldTaleId(cap4formBean, "?????????????????????");
					mPrimaryKeyList.add(new String[] {"?????????????????????", primaryKeyField1});
					mConfigMap.put("primaryKeyList", mPrimaryKeyList);
					
					codeMappingConfig.put(code, mConfigMap);
					markMappingConfig.put("M", codeMappingConfig);
				}else if(code.contains("fee")) {//????????????
					List<String[]> fPrimaryKeyList = new ArrayList();
					Map<String, Object> fConfigMap = new HashMap();
					
					String primaryKeyField2 = CAP4FormKit.getFieldTaleId(cap4formBean, "?????????????????????");
					String companyCodeField4 = CAP4FormKit.getFieldTaleId(cap4formBean, "????????????");
					fPrimaryKeyList.add(new String[] {"?????????????????????", primaryKeyField2});
					fPrimaryKeyList.add(new String[] {"????????????", companyCodeField4});
					fConfigMap.put("primaryKeyList", fPrimaryKeyList);
					
					codeMappingConfig.put(code, fConfigMap);
					markMappingConfig.put("F", codeMappingConfig);
				}
			}
			comanyMappingConfig.put(comany, markMappingConfig);
		}
	}
}
