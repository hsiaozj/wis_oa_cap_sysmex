package com.seeyon.apps.bjev.enp.travelBudget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.seeyon.apps.bjev.enp.BusinessEnpService;
import com.seeyon.apps.bjev.util.CAP4FormKit;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.form.modules.engin.base.formData.FormDataDAO;

public class TravelBudgetEnpImpl implements BusinessEnpService{
	private FormDataDAO formDataDAO = (FormDataDAO) AppContext.getBean("formDataDAO");
	private static final Logger log = Logger.getLogger(TravelBudgetEnpImpl.class);
	private CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
	
	@Override
	public Map<String, Object> preProcessMethod(Map<String, Object> paramMap) throws Exception {
		String templateCode = (String) paramMap.get("code");
//		String templateCode = "TravelBudget";
		FormBean cap4formBean = CAP4FormKit.getFormBean(cap4FormManager, templateCode);
		String tableName = CAP4FormKit.getTableNameByDisPlayName(cap4formBean, "", true);//获取到主表表名
		String  MEMBERField =  CAP4FormKit.getFieldTaleId(cap4formBean, "申请人");
		String  UNITField =  CAP4FormKit.getFieldTaleId(cap4formBean, "申请部门");
		
		List<Map> formainList = (List<Map>)paramMap.get(tableName);
		if(formainList != null && formainList.size() > 0) {
			Map dataMap = formainList.get(0);
			
			dataMap.put("field0999", "K2");//发送方
			dataMap.put("field0998", "SAP");//接收方
			SimpleDateFormat simpleDateFormat1= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			String date=simpleDateFormat1.format(new Date());
			dataMap.put("field0997", date);//发送时间
			dataMap.put("field0996", "k2002");//接口ID
			
			//dataMap.replace(MEMBERField, dataMap.get(MEMBERField)==null?"":getPerson(dataMap.get(MEMBERField)+""));
			//dataMap.replace(UNITField, dataMap.get(UNITField)==null?"":getDepartment(dataMap.get(UNITField)+""));
		}
		//log.info("处理后的数据："+paramMap);
		return paramMap;
	}

	private String getDepartment(String value) throws Exception {
		Map hm=new HashMap();
		String code="";
		hm.put("ID",value);
		List<Map> valueList = this.getValueByMap(null, hm, "ORG_UNIT", "and");
		if(valueList!=null&&valueList.size()>0) {
			Map ValueMap = valueList.get(0);
			code=(String) ValueMap.get("code");
		}
		log.info("转换后的部门code"+code);
		return code;
	}

	private String getPerson(String value) throws Exception {
		Map hm=new HashMap();
		String code="";
		hm.put("ID",value);
		List<Map> valueList = this.getValueByMap(null, hm, "ORG_MEMBER", "and");
		if(valueList!=null&&valueList.size()>0) {
			Map ValueMap = valueList.get(0);
			code=(String) ValueMap.get("code");
		}
		log.info("转换后的code"+code);
		return code;
	}

	private List<Map> getValueByMap(String[] returnFields, Map<String, Object> hm, String tableName,
			String fieldsLogic) throws Exception{
		return formDataDAO.getValueByMap(returnFields, hm, tableName, fieldsLogic);
	}

	@Override
	public Map<String, Object> finishedProcessMethod(Map<String, Object> paramMap) throws Exception {
		return paramMap;
	}

}
