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
		mdataOutput.setMessage("传输成功");

		try {
			String xml = XmlUtil.getXml(input);
			log.info("syncMdata----:"+xml);
			
			String xmlString = input.getXmlString();
			
			if(StringUtils.isBlank(xmlString) && !xmlString.contains("Entry")) {
				mdataOutput.setResult("E");
				mdataOutput.setMessage("报文为空");
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
				mdataOutput.setMessage("报文为空");
				return mdataOutput;
			}
			mdataOutput.setId(input.getMsgid());
			
			if(comanyMappingConfig.size() == 0) {
//				log.info("initConfigMap-------------");
				initConfigMap(comanyMappingConfig);
			}
//			log.info("comanyMappingConfig:"+ JSONObject.toJSONString(comanyMappingConfig));
			
			Map<String, List<DataItem>> companyMappingMdata = new HashMap<String, List<DataItem>>();//对应公司主数据
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
			//处理剩余未同步数据
			if(companyMappingMdata.size() > 0) {
				Iterator<Entry<String, List<DataItem>>> iter = companyMappingMdata.entrySet().iterator();
				while(iter.hasNext()) {
					Entry<String, List<DataItem>> entry = iter.next();
					String key = entry.getKey();
					String[] tempArr = key.split("_");
					
					String bukrs = tempArr[0];//公司代码
					String cflag = tempArr[1];//标记
					
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
			mdataOutput.setMessage("传输失败-" + ex.getMessage());
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
				case "C": //基金中心
					rowValues.put("公司代码", dataItem.getBukrs() == null ? "" : dataItem.getBukrs());
//					rowValues.put("基金中心编码", mdata.replaceAll("\\s*", ""));
//					rowValues.put("基金中心名称", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					rowValues.put("成本中心编码", mdata.replaceAll("\\s*", ""));
					rowValues.put("成本中心名称", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					break;
				case "W": //wbs
					rowValues.put("SAP公司编号", dataItem.getBukrs() == null ? "" : dataItem.getBukrs());
					rowValues.put("WBS编码1", mdata.replaceAll("\\s*", ""));
					rowValues.put("项目名称", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					break;
				case "V": //供应商
					rowValues.put("公司代码", dataItem.getBukrs() == null ? "" : dataItem.getBukrs());
					rowValues.put("供应商编号", mdata.replaceAll("\\s*", ""));
					rowValues.put("供应商名称", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					rowValues.put("税号", dataItem.getMtext1() == null ? "" : dataItem.getMtext1());
					rowValues.put("银行名称", dataItem.getMtext2() == null ? "" : dataItem.getMtext2());
					rowValues.put("银行账号", dataItem.getMtext3() == null ? "" : dataItem.getMtext3());
					rowValues.put("地址", dataItem.getMtext4() == null ? "" : dataItem.getMtext4());
					break;
				case "M": //承诺项目
					rowValues.put("公司代码", dataItem.getBukrs() == null ? "" : dataItem.getBukrs());
					rowValues.put("主数据唯一标识", mdata.replaceAll("\\s*", ""));
					rowValues.put("主数据描述1", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					rowValues.put("主数据描述2", dataItem.getMtext1() == null ? "" : dataItem.getMtext1());
					rowValues.put("主数据描述3", dataItem.getMtext2() == null ? "" : dataItem.getMtext2());
					rowValues.put("主数据描述4", dataItem.getMtext3() == null ? "" : dataItem.getMtext3());
					rowValues.put("主数据描述5", dataItem.getMtext4() == null ? "" : dataItem.getMtext4());
					break;
				case "F": //费用项目
					rowValues.put("公司代码", dataItem.getBukrs() == null ? "" : dataItem.getBukrs());
					rowValues.put("主数据唯一标识", mdata.replaceAll("\\s*", ""));
					rowValues.put("主数据描述1", dataItem.getMtext0() == null ? "" : dataItem.getMtext0());
					rowValues.put("主数据描述2", dataItem.getMtext1() == null ? "" : dataItem.getMtext1());
					rowValues.put("主数据描述3", dataItem.getMtext2() == null ? "" : dataItem.getMtext2());
					rowValues.put("主数据描述4", dataItem.getMtext3() == null ? "" : dataItem.getMtext3());
					rowValues.put("主数据描述5", dataItem.getMtext4() == null ? "" : dataItem.getMtext4());
					break;
			}

			rowData.put("values", rowValues);
			rst4Update.put("mainTable", rowData);
		}
		result.put("rst4Updates", rst4Updates);
		return result;
	}
	
	/**
	 * 获取对应公司和主数据业务的模板编号和配置信息
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
				
				if(code.contains("fc")) {//基金中心
					List<String[]> cPrimaryKeyList = new ArrayList();
					Map<String, Object> cConfigMap = new HashMap();
					
					String fundCenterCodeField = CAP4FormKit.getFieldTaleId(cap4formBean, "成本中心编码");
					String companyCodeField1 = CAP4FormKit.getFieldTaleId(cap4formBean, "公司代码");
					cPrimaryKeyList.add(new String[] {"成本中心编码", fundCenterCodeField});
					cPrimaryKeyList.add(new String[] {"公司代码", companyCodeField1});
					cConfigMap.put("primaryKeyList", cPrimaryKeyList);
					
					codeMappingConfig.put(code, cConfigMap);
					markMappingConfig.put("C", codeMappingConfig);
				}else if(code.contains("wbs")) {//wbs
					List<String[]> wbsPrimaryKeyList = new ArrayList();
					Map<String, Object> wbsConfigMap = new HashMap();
					
					String wbsCodeField = CAP4FormKit.getFieldTaleId(cap4formBean, "WBS编码1");
					String companyCodeField2 = CAP4FormKit.getFieldTaleId(cap4formBean, "SAP公司编号");
					wbsPrimaryKeyList.add(new String[] {"WBS编码1", wbsCodeField});
					wbsPrimaryKeyList.add(new String[] {"SAP公司编号", companyCodeField2});
					wbsConfigMap.put("primaryKeyList", wbsPrimaryKeyList);
					
					codeMappingConfig.put(code, wbsConfigMap);
					markMappingConfig.put("W", codeMappingConfig);
				}else if(code.contains("supplier")) {//供应商
					List<String[]> vPrimaryKeyList = new ArrayList();
					Map<String, Object> vConfigMap = new HashMap();
					
					String supplierCodeField = CAP4FormKit.getFieldTaleId(cap4formBean, "供应商编号");
					String companyCodeField3 = CAP4FormKit.getFieldTaleId(cap4formBean, "公司代码");
					vPrimaryKeyList.add(new String[] {"供应商编号", supplierCodeField});
					vPrimaryKeyList.add(new String[] {"公司代码", companyCodeField3});
					vConfigMap.put("primaryKeyList", vPrimaryKeyList);
					
					codeMappingConfig.put(code, vConfigMap);
					markMappingConfig.put("V", codeMappingConfig);
				}else if(code.contains("cp")) {//承诺项目
					List<String[]> mPrimaryKeyList = new ArrayList();
					Map<String, Object> mConfigMap = new HashMap();
				
					String primaryKeyField1 = CAP4FormKit.getFieldTaleId(cap4formBean, "主数据唯一标识");
					mPrimaryKeyList.add(new String[] {"主数据唯一标识", primaryKeyField1});
					mConfigMap.put("primaryKeyList", mPrimaryKeyList);
					
					codeMappingConfig.put(code, mConfigMap);
					markMappingConfig.put("M", codeMappingConfig);
				}else if(code.contains("fee")) {//费用项目
					List<String[]> fPrimaryKeyList = new ArrayList();
					Map<String, Object> fConfigMap = new HashMap();
					
					String primaryKeyField2 = CAP4FormKit.getFieldTaleId(cap4formBean, "主数据唯一标识");
					String companyCodeField4 = CAP4FormKit.getFieldTaleId(cap4formBean, "公司代码");
					fPrimaryKeyList.add(new String[] {"主数据唯一标识", primaryKeyField2});
					fPrimaryKeyList.add(new String[] {"公司代码", companyCodeField4});
					fConfigMap.put("primaryKeyList", fPrimaryKeyList);
					
					codeMappingConfig.put(code, fConfigMap);
					markMappingConfig.put("F", codeMappingConfig);
				}
			}
			comanyMappingConfig.put(comany, markMappingConfig);
		}
	}
}
