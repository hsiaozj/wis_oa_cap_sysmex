package com.seeyon.apps.bjev.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bjev.apps.service.DataHandleService;
import com.seeyon.apps.bjev.enp.BusinessEnpService;
import com.seeyon.apps.bjev.services.webService.BaseService;
import com.seeyon.apps.bjev.services.webService.SendDataService;
import com.seeyon.apps.bjev.util.CAP4FormKit;
import com.seeyon.apps.bjev.util.LogUtil;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.workflow.supernode.BaseSuperNodeAction;
import com.seeyon.ctp.workflow.supernode.SuperNodeResponse;
import com.seeyon.ctp.workflow.supernode.enums.SuperNodeEnums.RunAction;

/******
 * 	
      采购预算申请单业务超级节点
 * 
 * 
 * 
 * **/
public class PurchaseBudgetNode extends BaseSuperNodeAction {
	private static final Log log = LogFactory.getLog(PurchaseBudgetNode.class);

	private ColManager colManager = (ColManager) AppContext.getBean("colManager");

	private TemplateManager templateManager = (TemplateManager) AppContext.getBean("templateManager");

	private OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");

	@Override
	public void cancelAction(String token, String activityId, Map<String, Object> param) {

	}

	@Override
	public SuperNodeResponse confirmAction(String token, String activityId, Map<String, Object> param) {
		return executeAction(token, activityId, param);
	}


	@Override
	public SuperNodeResponse executeAction(String token, String activityId, Map<String, Object> param) {
		log.info("============采购预算申请单创建开始==============");
		SuperNodeResponse response = new SuperNodeResponse();
		SendDataService sendDataService = null;
		try {
			Long summaryid = null;
			CtpTemplate ctpTemplate = null;
			FormDataMasterBean form4DataMasterBean = null;
			Map<String, Object> resultMap = null;
			Long formId = null;
			Object id = param.get("WF_SUPER_NODE_SUMMARY_ID");
			if(id != null) {
				summaryid = Long.parseLong(id+"");
			}
			Map<String, Object> data = getFormData(param);
			if(data != null) {
				form4DataMasterBean = (FormDataMasterBean)data.get("formDataBean");
			}

			ColSummary colSummary = colManager.getSummaryById(summaryid);
			if(colSummary != null) {
				ctpTemplate = templateManager.getCtpTemplate(colSummary.getTempleteId());
				formId = colSummary.getFormAppid();//表单ID
			}

			String currentLoginName = "";//当前节点处理人登录账号
			long modidyMemberId = form4DataMasterBean.getModifyMemberId();

			V3xOrgMember member = orgManager.getMemberById(modidyMemberId);
			if(member != null) {
				currentLoginName = member.getLoginName();
			}

			String templeteNumber = ctpTemplate.getTempleteNumber();
			log.info("采购预算审批单流程模板编号"+templeteNumber);

			Map<String, Object> documentData = new HashMap();
			List<Map> forMainList = new ArrayList();

			if (form4DataMasterBean != null) {
				// 获取主表缓存数据
				forMainList = CAP4FormKit.getFormDataMasterBeanData(form4DataMasterBean, "yyyyMMdd"); // 获取主表数据
				String forMainTableName = form4DataMasterBean.getFormTable().getTableName();// 获得主表的表名
				documentData.put(forMainTableName, forMainList);// 将数据与表名放入list集合中

				// 获取从表数据缓存
				Map<String, List<FormDataSubBean>> subTables = form4DataMasterBean.getSubTables();// 获得从表中表格结构
				Map<String, List<Map>> subTableData = CAP4FormKit.getSubTableData(subTables, "yyyyMMdd");// 获取从表中的数据
				documentData.putAll(subTableData);

				documentData.put("code",templeteNumber);
				log.info("缓存下来的数据："+documentData);

				BusinessEnpService purchaseBudgetEnpService=(BusinessEnpService) AppContext.getBean("purchaseBudgetEnpService");
				Map<String,Object> purchaseBudgetData= purchaseBudgetEnpService.preProcessMethod(documentData);

				// 数据处理器
				Map<String, Object> handleParamMap = new HashMap();
				handleParamMap.put("configCategroy", templeteNumber);
				handleParamMap.put("documentData", purchaseBudgetData);

				DataHandleService wshandleService = (DataHandleService) AppContext.getBean("dataHandleWebservice");
				Map<String, Object> handleResultData = wshandleService.procMappingData(handleParamMap);
				log.info("进入采购处理器以后"+handleResultData);

				handleResultData.put("documentData", purchaseBudgetData);
				handleResultData.put("form4DataMasterBean", form4DataMasterBean);
				handleResultData.put("formId", formId);
				handleResultData.put("code", templeteNumber);
				//log.info("处理器调用后数据为："+handleResultData);

				sendDataService = (SendDataService) AppContext.getBean("purchaseBudgetService");
				Map<String, Object> resultData = sendDataService.doSapSyncMethod(handleResultData);
				log.info("resultData数据结果是："+resultData);

				resultMap = purchaseBudgetEnpService.finishedProcessMethod(resultData);
			}
			if(resultMap != null) {
				String status = (String) resultMap.get("status");
				if (StringUtils.isNotBlank(status) && "E".equals(status.trim())) {
					response.setReturnCode(RunAction.BACK.getKey());
					response.setReturnMsg((String) resultMap.get("message"));
					log.info("采购预算单创建失败！");
				}else {
					response.setReturnCode(RunAction.FORWARD.getKey());
					response.setReturnMsg((String) resultMap.get("message"));
					log.info("采购预算单创建成功！");
				}
			}
			response.setSuccess(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			String message="采购预算审批单,系统推送数据时出现异常";
			response.setReturnMsg(message);
			response.setReturnCode(RunAction.BACK.getKey());
			log.info("节点：purchaseBudgetNode异常:",ex);

			//String message = "系统异常";
			String errInfo = LogUtil.getTrace(ex);
			if(StringUtils.isNotBlank(errInfo)) {
				if(errInfo.length() > 450) {
					message += ","+errInfo.substring(0, 450);
				}else {
					message += ","+errInfo;
				}
			}
		}

		return response;
	}

	@Override
	public String getNodeId() {
		return "purchaseBudgetNode";
	}

	@Override
	public String getNodeName() {
		return "采购预算申请单超级节点";
	}

	@Override
	public int getOrder() {
		return 100001;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getFormData(Map<String, Object> params) {
		return (Map<String, Object>) params.get("CTP_FORM_DATA");
	}

}
