package com.seeyon.apps.bjev.services.impl.financialBudget;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.bjev.services.webService.SendDataServiceImpl;
import com.seeyon.apps.bjev.util.CAP4FormKit;
import com.seeyon.apps.bjev.util.LogUtil;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.form.modules.engin.base.formData.FormDataDAO;
/**
 * 财务预算申请单创建报文处理与发送
 * 
 * 
 * **/
public class FinancialBudgetServiceImpl extends SendDataServiceImpl implements FinancialBudgetService{
	private static final Log log = LogFactory.getLog(FinancialBudgetServiceImpl.class);
	private CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
	private FormDataDAO formDataDAO = (FormDataDAO) AppContext.getBean("formDataDAO");

	@Override
	public Map<String, Object> doSapSyncMethod(Map<String, Object> map) throws Exception {
		Map<String, Object> returnResult = new HashMap();
		try {
			String xml = (String) map.get("xml");
			xml =xml.replace("&", "&amp;");
			String templateCode = (String) map.get("code");
			//			String templateCode = "FinancialBudget";
			FormBean cap4formBean = CAP4FormKit.getFormBean(cap4FormManager, templateCode);
			List<FormTableBean> formTableBeanList = cap4formBean.getSubTableBean();

			FormTableBean formTableBean = formTableBeanList.get(0);
			String formmainName = CAP4FormKit.getTableNameByDisPlayName(cap4formBean, "", true);
			String formsonName = CAP4FormKit.getTableNameByDisPlayName(cap4formBean, "SAP基金中心", false);//从表表名

			Map<String, Object> documentData = (Map<String, Object>) map.get("documentData");

			List<Map> formainList = (List<Map>) documentData.get(formmainName);
			Map dataMap = formainList.get(0);
			String numberField = CAP4FormKit.getFieldTaleId(cap4formBean, "SAP外部单据号");
			String sapNumberField = CAP4FormKit.getFieldTaleId(cap4formBean, "SAP预算单号");
			String sapNoteField = CAP4FormKit.getFieldTaleId(cap4formBean, "SAP返回结果");
			String number = (String) dataMap.get(numberField);
			log.info("外部单据号是：" + numberField + "," + number);

			FormDataMasterBean form4DataMasterBean = (FormDataMasterBean) map.get("form4DataMasterBean");
			FormDataSubBean subBean = null;
			Map<String, List<FormDataSubBean>> allSubBeansMap = form4DataMasterBean.getSubTables();
			List<FormDataSubBean> formSubBeanlist = allSubBeansMap.get(formsonName);
			if (formSubBeanlist != null && formSubBeanlist.size() > 0) {
				subBean = formSubBeanlist.get(0);
			}
			List<Map> formsonList = (List<Map>) documentData.get(formsonName);
			String subXml = getSubXml(formsonList, subBean);

			StringBuilder sb = new StringBuilder(xml);
			int index = sb.indexOf("</ITEMS>");
			sb.insert(index, subXml);



			String value = "oa";
			OMElement isInput = new StAXOMBuilder(new ByteArrayInputStream(sb.toString().getBytes("UTF-8"))).getDocumentElement();
			OMElement result = doSapMethod(isInput, map, value);


			for (int i = 1; i < 4; i++) {

				Thread.sleep(1 * 60 * 1000);

				Map hm = new HashMap();
				String sapNumber = "";
				String sapNote = "";
				hm.put(numberField, number);
				List<Map> dateList = this.getValueByMap(null, hm, formmainName, "and");
				if (dateList != null && dateList.size() > 0) {
					Map dateValMap = dateList.get(0);
					sapNumber = dateValMap.get(sapNumberField) == null ? "" : dateValMap.get(sapNumberField) + "";
					sapNote = dateValMap.get(sapNoteField) == null ? "" : dateValMap.get(sapNoteField) + "";
					log.info("查询出来的sap返回结果为：" + sapNumber + "," + sapNote);
				}

				log.info("查询SAP预算单号结果第：" + i + "次，时间是：" + i * 60);

				if (StringUtils.isNotBlank(sapNumber) && StringUtils.isNotBlank(sapNote)) {
					returnResult.put("status", "S");
					returnResult.put("message", "财务预算单创建成功");
					return returnResult;
				} else if (StringUtils.isBlank(sapNumber) && StringUtils.isNotBlank(sapNote)) {
					returnResult.put("status", "E");
					returnResult.put("message", "财务预算单创建不成功，请查看SAP返回结果");
					return returnResult;
				}

			}

			returnResult.put("status", "E");
			returnResult.put("message", "财务预算单创建不成功，SAP未及时推送结果数据");
		}catch (Exception ex){
			ex.printStackTrace();
			log.info("财务预算单创建出现异常"+ex.getMessage());

			String message = "财务预算单创建不成功，系统推送数据时出现异常:";
			String errInfo = LogUtil.getTrace(ex);
			if(StringUtils.isNotBlank(errInfo)) {
				if(errInfo.length() > 450) {
					message += ","+errInfo.substring(0, 450);
				}else {
					message += ","+errInfo;
				}
			}
			returnResult.put("status", "E");
			returnResult.put("message", message);
		}
		return returnResult;
	}

	private List<Map> getValueByMap(String[] returnFields, Map<String, Object> hm, String tableName,
			String fieldsLogic) throws Exception{
		return formDataDAO.getValueByMap(returnFields, hm, tableName, fieldsLogic);
	}
	private String getSubXml(List<Map> forsonList, FormDataSubBean subBean) throws Exception {
		StringBuffer buf = new StringBuffer();
		String FISTLField = CAP4FormKit.getFieldTaleId(subBean, "SAP基金中心");
		String KOSTLField = CAP4FormKit.getFieldTaleId(subBean, "SAP成本中心");
		String ARWRBTRField = CAP4FormKit.getFieldTaleId(subBean, "申请金额");
		String POSIDField = CAP4FormKit.getFieldTaleId(subBean, "SAPWBS编码");
		String EXITEMField = CAP4FormKit.getFieldTaleId(subBean, "SAP费用项目");
		String FIPOSField = CAP4FormKit.getFieldTaleId(subBean, "SAP承诺项目");

		for(Map forsonMap : forsonList) {
			String FISTL = (String)forsonMap.get(FISTLField) == null ? "" : (String)forsonMap.get(FISTLField);
			String KOSTL = (String)forsonMap.get(KOSTLField) == null ? "" : (String)forsonMap.get(KOSTLField);
			String ARWRBTR = (String)forsonMap.get(ARWRBTRField) == null ? "" : (String)forsonMap.get(ARWRBTRField);
			String POSID = (String)forsonMap.get(POSIDField) == null ? "" : (String)forsonMap.get(POSIDField);
			String EXITEM = (String)forsonMap.get(EXITEMField) == null ? "" : (String)forsonMap.get(EXITEMField);
			String FIPOS= (String)forsonMap.get(FIPOSField) == null ? "" : (String)forsonMap.get(FIPOSField);

			String item="<ITEM>"+
					"<FISTL>"+FISTL+"</FISTL>"+
					"<KOSTL>"+KOSTL+"</KOSTL>"+
					"<EXITEM>"+EXITEM+"</EXITEM>"+
					"<POSID>"+POSID+"</POSID>"+
					"<ARWRBTR>"+ARWRBTR+"</ARWRBTR>"+
					"<ZRSV01></ZRSV01>"+
					"<ZRSV02>"+FIPOS+"</ZRSV02>"+
					"<ZRSV03></ZRSV03>"+
					"<ZRSV04></ZRSV04>"+
					"<ZRSV05></ZRSV05>"+
					"</ITEM>";

			buf.append(item);
		}
		return buf.toString();
	}


}
