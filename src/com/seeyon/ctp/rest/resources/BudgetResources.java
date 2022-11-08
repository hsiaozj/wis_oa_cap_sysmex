package com.seeyon.ctp.rest.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.bjev.common.CtrlFieldType;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.template.util.CAPFormUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.DataContainer;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.json.JSONUtil;

@Path("cap4/formSapBudget")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class BudgetResources extends BaseResource{
	private static Log log = LogFactory.getLog(BudgetResources.class);
	
	public static List<CtrlFieldType> loadInfo(){
		List<CtrlFieldType> tmpList = new ArrayList<CtrlFieldType>();
		CtrlFieldType temp = null;
		temp = new CtrlFieldType("AMOUNT", "可用预算金额", Enums.FieldType.DECIMAL, new FormFieldComEnum[] { FormFieldComEnum.TEXT, FormFieldComEnum.TEXTAREA});
		tmpList.add(temp);
		
		return tmpList;
	}
	
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("getBudgetFieldInfo")
	public Response getEinvoiceFieldInfo(@QueryParam("formid") String formId, @QueryParam("fieldType") String fieldType, @QueryParam("currentField") String currentField){
		log.info("进入方法");
		Map<String, Object> res = new HashMap<String, Object>();
		Map<String, Object> budgetFields = new HashMap<String, Object>();

		List<CtrlFieldType> tmpList = loadInfo();
		for (CtrlFieldType type : tmpList) {
			budgetFields.put(type.getKey(), type.getText());
		}
		DataContainer formFieldMap = getFieldsJsonObject(formId, fieldType, currentField);
		res.put("budgetMap", budgetFields);
		res.put("formFieldMap", formFieldMap);
		log.info("einvoiceFields:"+budgetFields);
		log.info("formFieldMap:"+formFieldMap);
		return success(res);
	}
	
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("getSapFields")
	public Response getFormFields(@QueryParam("formid") String formId, @QueryParam("fieldType") String fieldType, @QueryParam("currentField") String currentField) {
		DataContainer result = getFieldsJsonObject(formId, fieldType, currentField);
		return success(result);
	}
	
	private DataContainer getFieldsJsonObject(@QueryParam("formid") String formId, @QueryParam("fieldType") String fieldType, @QueryParam("currentField") String currentField) {
		CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
		FormBean formBean = cap4FormManager.getEditingForm(Long.valueOf(formId));
		FormTableBean currentTable = formBean.getFormTableBeanByFieldName(currentField);
		DataContainer result = new DataContainer();

		List<FormFieldBean> currentTableFields = currentTable.getFields();
		for (FormFieldBean field : currentTableFields) {
			if ((!field.isCustomerCtrl()) && (isFieldCanBeChoose(field.getInputType()))) {
				result.put(field.getName(), field.getJsonObj4Design(false));
			}
		}
		return result;
	}
	
	private boolean isFieldCanBeChoose(String inputType) {
		boolean result = false;
		List<CtrlFieldType> tmpList = loadInfo();
		for (CtrlFieldType type : tmpList) {
			FormFieldComEnum[] comEnums = type.getSupportFieldType();
			for (FormFieldComEnum comEnum : comEnums) {
				if (comEnum.getKey().equalsIgnoreCase(inputType)) {
					result = true;
					break;
				}
			}
			if (result) {
				break;
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Produces({MediaType.APPLICATION_JSON})
	@Path("checkSapMapping")
	public Response checkMapping(Map<String, Object> params) {
		Long formId = Long.valueOf(Long.parseLong("" + params.get("formId")));
		CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
		FormBean formBean = cap4FormManager.getEditingForm(Long.valueOf(formId.longValue()));
		Object datas = params.get("datas");
		List<Map<String, String>> mappings = (List<Map<String, String>>) datas;
		boolean checkResult = true;
		String errorMsg = "";
		Map<String, Object> result = new HashMap<String, Object>();
		for (Map<String, String> mapping : mappings) {
			String source = (String) mapping.get("source");
			String target = (String) mapping.get("target");
			log.info("source:"+source+"==="+target);
			FormFieldBean fieldBean = formBean.getFieldBeanByName(target);
			CtrlFieldType sourceType = getEnumByKey(source);
			FormFieldComEnum[] supportTypes = sourceType.getSupportFieldType();
			boolean hasEquals = false;
			for (FormFieldComEnum supportField : supportTypes) {
				log.info("one:"+supportField.getKey());
				log.info("two:"+supportField.getText());
				
				log.info("tree:"+ResourceUtil.getString("form.input.inputtype.org.account.label"));
				if (Enums.FieldType.DECIMAL.getKey().equals(fieldBean.getInputType())) {
					if (fieldBean.getFieldType().equals(Enums.FieldType.DECIMAL.getKey())) {
						break;
					}
					hasEquals = true;
					break;
				}
			}
			if (!hasEquals) {
				String supportTypeStrs = "";
				FormFieldComEnum[] types = sourceType.getSupportFieldType();
				log.info("type:"+types);
				for (int i = 0; i < types.length; i++) {
					FormFieldComEnum t = types[i];
					if (i == types.length - 1) {
						supportTypeStrs = supportTypeStrs + t.getText();
					} else {
						supportTypeStrs = supportTypeStrs + t.getText() + "、";
					}
				}
				String fieldType = "";
				if (fieldBean.isNumberField()) {
					fieldType = "数字";
				} else {
					fieldType = fieldBean.getFieldCtrl().getText();
				}
				errorMsg = sourceType.getText() + "[" + supportTypeStrs + "]->" + fieldBean.getDisplay() + "[" + fieldType + "] 类型不一致！";
				result.put("errorMsg", errorMsg);
				checkResult = false;
				break;
			}
		}
		result.put("result", String.valueOf(true));
		return success(result);
	}
	
	private CtrlFieldType getEnumByKey(String key) {
		List<CtrlFieldType> tmpList = loadInfo();
		for (CtrlFieldType type : tmpList) {
			if (type.getKey().equals(key)) {
				return type;
			}
		}
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	@Path("parseAndFillBackSap")
	public Response parseAndFillBack(@QueryParam("name") String name, @QueryParam("formId") String formId, @QueryParam("fieldName") String fieldName, @QueryParam("masterId") String masterId, @QueryParam("subId") String subId, @QueryParam("data") String data, @QueryParam("fieldId") String fieldId) throws BusinessException {
		//log.info("name值:"+name+"====="+"formId值:"+formId+"====="+"fieldName值:"+fieldName+"====="+"masterId值:"+masterId+"====="+"subId值:"+subId+"====="+"data值:"+data);
		log.info("fieldID:"+fieldId);
		CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
		FormBean formBean = cap4FormManager.getForm(Long.valueOf(formId), false);
		FormFieldBean field = formBean.getFieldBeanByName(fieldName);
		//log.info("field值:"+field);
		String customParams = field.getCustomParam();
		//log.info("customParams值:"+customParams);
		FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.valueOf(Long.parseLong(masterId)));
		//log.info("cacheFormData值:"+cacheFormData.getAllDataJSONString());
		if (null == cacheFormData) {
			throw new BusinessException("表单数据在session中找不到（masterId:" + masterId + "），请尝试重新打开。");
		}
		FormDataSubBean formSubData = null;
		if (!field.isMasterField()) {
			formSubData = cacheFormData.getFormDataSubBeanById(field.getOwnerTableName(), Long.valueOf(Long.parseLong(subId)));
			if (null == formSubData) {
				throw new BusinessException("自定义控件是明细表字段，但是通过明细表行id：" + subId + "找不到明细表数据");
			}
		}
		Map<String, Object> definition = null;
		List<Map<String, String>> array = null;
		if (Strings.isNotBlank(customParams)) {
			definition = (Map<String, Object>) JSONUtil.parseJSONString(customParams);
			array = (List) definition.get("mapping");
			log.info("array值:"+array);
		}
		JSONObject jsonObject = (JSONObject) JSON.parseObject(data).get("data");
		log.info("jsonObject值:"+jsonObject);
		String amount = jsonObject.get("AMOUNT")+"";
		Map<String, Object> result = new HashMap<String, Object>();
		
//		Map valMap = new HashMap();
//		valMap.put("showValue", amount);
//		valMap.put("showValue2", amount);
//		valMap.put("value", amount);
//		result.put(fieldId, valMap);
		if (array != null) {
			for (Map<String, String> defMap : array) {
				String source = (String) defMap.get("source");
				String target = (String) defMap.get("target");
				FormFieldBean conffield = formBean.getFieldBeanByName(target);
				log.info("conffield值:"+conffield);
				Object val = jsonObject.getString(source);
				val = val == null ? "" : val;
				if (conffield.isMasterField()) {
					log.info("进入主表");
					cacheFormData.addFieldValue(target, val);
					val = cacheFormData.getFieldValue(target);
					log.info("val值:"+val);
					cacheFormData.addFieldChanges4Calc(conffield, val, null);
					Map<String, Object> tempRes = CAPFormUtil.getDisplayValueMap(val, conffield, null);
					log.info("tempRes值:"+tempRes);
					result.put(target, tempRes);
				} else {
					formSubData.addFieldValue(target, val);
					val = formSubData.getFieldValue(target);
					cacheFormData.addFieldChanges4Calc(conffield, val, formSubData);
					Map<String, Object> tempRes = CAPFormUtil.getDisplayValueMap(val, conffield, null);
					result.put(target, tempRes);
				}
			}
		}
		log.info("result值为："+result);
		return success(result);
	}
}
