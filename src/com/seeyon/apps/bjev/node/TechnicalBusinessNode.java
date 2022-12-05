package com.seeyon.apps.bjev.node;

import com.bjev.apps.service.DataHandleService;
import com.seeyon.apps.bjev.enp.BusinessEnpService;
import com.seeyon.apps.bjev.services.impl.contractApproval.ContractApprovalService;
import com.seeyon.apps.bjev.util.CAP4FormKit;
import com.seeyon.apps.bjev.util.LogUtil;
import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.template.CtpTemplate;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.workflow.supernode.BaseSuperNodeAction;
import com.seeyon.ctp.workflow.supernode.SuperNodeResponse;
import com.seeyon.ctp.workflow.supernode.enums.SuperNodeEnums;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TechnicalBusinessNode extends BaseSuperNodeAction {
    private static final Log log = LogFactory.getLog(TechnicalBusinessNode.class);

    private ColManager colManager = (ColManager) AppContext.getBean("colManager");

    private TemplateManager templateManager = (TemplateManager) AppContext.getBean("templateManager");


    @Override
    public void cancelAction(String s, String s1, Map<String, Object> map) throws BusinessException {

    }

    @Override
    public SuperNodeResponse confirmAction(String token, String activityId, Map<String, Object> param) {
        return executeAction(token, activityId, param);
    }

    @Override
    public SuperNodeResponse executeAction(String token, String activityId, Map<String, Object> param)  {
        log.info("============技术商务合同审批开始==============");
        SuperNodeResponse response = new SuperNodeResponse();
        try{
            Long summaryid = null;
            CtpTemplate ctpTemplate = null;
            FormDataMasterBean form4DataMasterBean = null;
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
            }

            String templeteNumber = ctpTemplate.getTempleteNumber();
            log.info("技术商务合同审批流程模板编号"+templeteNumber);

            Map<String, Object> documentData = new HashMap();
            List<Map> forMainList = new ArrayList();

            if (form4DataMasterBean != null) {
                DataHandleService wshandleService = (DataHandleService) AppContext.getBean("dataHandleWebservice");
                // 获取主表缓存数据
                forMainList = CAP4FormKit.getFormDataMasterBeanData(form4DataMasterBean, "yyyy-MM-dd");
                String forMainTableName = form4DataMasterBean.getFormTable().getTableName();
                documentData.put(forMainTableName, forMainList);

                // 获取从表数据缓存
                Map<String, List<FormDataSubBean>> subTables = form4DataMasterBean.getSubTables();
                Map<String, List<Map>> subTableData = CAP4FormKit.getSubTableData(subTables, "yyyy-MM-dd");
                documentData.putAll(subTableData);
                documentData.put("code",templeteNumber);
                log.info("技术商务合同缓存下来的数据："+documentData);

                BusinessEnpService contractApprovalEnpService=(BusinessEnpService) AppContext.getBean("contractApprovalEnpService");
                Map<String,Object> businessInfo= contractApprovalEnpService.preProcessMethod(documentData);

                // 获取业务数据报文
                Map<String, Object> businessMap = new HashMap();
                businessMap.put("configCategroy", templeteNumber);
                businessMap.put("documentData", businessInfo);
                Map<String, Object> businessResult = wshandleService.procMappingData(businessMap);
                log.info("进入技术商务合同审批处理器业务信息："+businessResult);

                ContractApprovalService contractService= (ContractApprovalService) AppContext.getBean("contractApprovalService");
                Map<String, Object> resultMap = contractService.doSapSyncMethod(businessResult);
                log.info("resultData数据结果是："+resultMap);
                if(resultMap != null) {
                    String status = (String) resultMap.get("status");
                    if (StringUtils.isNotBlank(status) && "E".equals(status.trim())) {
                        response.setReturnCode(SuperNodeEnums.RunAction.BACK.getKey());
                        response.setReturnMsg((String) resultMap.get("message"));
                        log.info("技术商务审批失败！");
                    }else {
                        response.setReturnCode(SuperNodeEnums.RunAction.FORWARD.getKey());
                        response.setReturnMsg((String) resultMap.get("message"));
                        log.info("技术商务审批成功！");
                    }
                }
            }
            response.setSuccess(true);
        }catch (Exception ex){
            ex.printStackTrace();
            String message="技术商务合同审批单,系统推送数据时出现异常";
            log.info("节点：TechnicalBusinessNode异常:",ex);

            String errInfo = LogUtil.getTrace(ex);
            if(StringUtils.isNotBlank(errInfo)) {
                if(errInfo.length() > 450) {
                    message += ","+errInfo.substring(0, 450);
                }else {
                    message += ","+errInfo;
                }
            }
            response.setReturnMsg(message);
            response.setReturnCode(SuperNodeEnums.RunAction.BACK.getKey());
        }
        return response;
    }

    @Override
    public String getNodeId() {
        return "technicalBusinessNode";
    }

    @Override
    public String getNodeName() {
        return "技术商务合同审批超级节点";
    }

    @Override
    public int getOrder() {
        return 100079;
    }

    private Map<String, Object> getFormData(Map<String, Object> params) {
        return (Map<String, Object>) params.get("CTP_FORM_DATA");
    }

}
