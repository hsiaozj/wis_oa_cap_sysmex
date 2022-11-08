package com.seeyon.apps.bjev.services.webService.services.impl;

import com.seeyon.apps.bjev.services.pojo.*;
import com.seeyon.apps.bjev.services.webService.services.BudgetBusinesService;
import com.seeyon.apps.bjev.services.webService.services.HandleBudgetThred;
import com.seeyon.apps.bjev.services.webService.services.HandleOtherBudgetThred;
import com.seeyon.apps.bjev.util.CAP4FormKit;
import com.seeyon.apps.bjev.util.XmlUtil;
import com.seeyon.apps.bjev.services.FormServices;
import com.seeyon.apps.bjev.services.WsBaseService;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataDAO;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetBusinesServiceImpl implements BudgetBusinesService, WsBaseService {

    private static final Logger log = Logger.getLogger(BudgetBusinesServiceImpl.class);
    private CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
    private CAP4FormDataDAO cap4FormDataDAO = (CAP4FormDataDAO) AppContext.getBean("cap4FormDataDAO");
    private FormServices formServices = (FormServices) AppContext.getBean("pocFormServices");
    private String pbTableCode = "PurchaseBudget";
    private String tbTableCode = "TravelBudget";
    private String fbTableCode = "FinancialBudget";

    //A事业部模板编号
    private String aOrgPbTableCode = "PurchaseBudget_A";
    private String aOrgTbTableCode = "TravelBudget_A";
    private String aOrgFbTableCode = "FinancialBudget_A";

    private String registerCode = "2005";
    // 线程池管理器
    private CompletionService<List<Map>> pool = null;

    @Override
    public WisBaseOutput doWsWork(Object input) throws Exception {
        return procBackBudgetData((PbInput) input);
    }

    @Override
    public WisBaseOutput procBackBudgetData(PbInput input) throws Exception {
        log.info("input===>" + input);
        WbackOutput wbackOutput = new WbackOutput();
        wbackOutput.setMessage("传输成功");
        wbackOutput.setResult("S");
        try {
            String xml = XmlUtil.getXml(input);
            String msgid = input.getHead().getMsgid();

            String xmlString = input.getXmlString();
            log.info("xmlString====" + xmlString);
            if(StringUtils.isBlank(xmlString) && !xmlString.contains("Entry")) {
                wbackOutput.setResult("E");
                wbackOutput.setMessage("报文为空");
                return wbackOutput;
            }
            //线程处理K2预算
            String rstStrXml = XmlUtil.subStringBetween(xml, "<xmlString>", "</xmlString>");
            log.info("rstStrXml====" + rstStrXml);
            HandleOtherBudgetThred k2Thred = new HandleOtherBudgetThred(rstStrXml);
            Thread t = new Thread(k2Thred);
            t.start();

            if(xmlString.contains("&lt;") || xmlString.contains("&gt;")) {
                xmlString = xmlString.replace("&lt;", "<").replace("&gt;", ">");
            }
            log.info("xmlString====" + xmlString);

            PbInterface elem = XmlUtil.getBean(xmlString, PbInterface.class);
            PbItem[] items = elem.getItems();
            log.info("推送总数为:" + items.length);
            if(items == null || items.length < 1) {
                log.info("报文为空========");
                wbackOutput.setResult("E");
                wbackOutput.setMessage("报文为空");
                return wbackOutput;
            }

            List<PbItem> arrayList = new ArrayList();
            for(PbItem item : items){
                arrayList.add(item);
            }
            log.info("arrayList:size==" + arrayList.size());
            Map dataMap = procTableData(); //业务表单数据
            log.info("dataMap==" + dataMap);

            // 数据量大小
            int length = arrayList.size();
            int threadCount = 15;
            int taskCount = 0;
            // 每个线程处理的数据个数
            if(length < threadCount){
                threadCount = 1;
            }

            taskCount = length / threadCount ;
            log.info("taskCount===" + taskCount);

            ExecutorService threadpool = Executors.newFixedThreadPool(threadCount);
            pool = new ExecutorCompletionService<List<Map>>(threadpool);
            for (int j = 0; j < threadCount; j++) {
                // 每个线程任务数据list
                final List<PbItem> subData = new ArrayList();
                List<PbItem> modelList = null;
                if (j == (threadCount - 1)) {
                    modelList = arrayList.subList(j * taskCount, length);
                    subData.addAll(modelList);
                } else {
                    modelList = arrayList.subList(j * taskCount, (j + 1) * taskCount);
                    subData.addAll(modelList);
                }
                HandleBudgetThred execute = new HandleBudgetThred(String.valueOf(j),subData,
                        dataMap,registerCode);
                pool.submit(execute);
            }

            List<Map> result = new ArrayList<>();
            for (int z = 0; z < threadCount; z++) {
                // 每个线程处理结果集
                List<Map> threadResult = pool.take().get();
                log.info("threadResult===" + threadResult);
                result.addAll(threadResult);
            }
            log.info("多线程处理返回结果===" + result);

            if(result !=null && result.size()>0){
                RstMsgs rstMsgs = new RstMsgs();
                if(result.size() == 1){ //只有一条数据
                    Map valMap = result.get(0);
                    log.info("valMap===" + valMap);
                    String status = valMap.get("STATUS") +"";
                    String message = valMap.get("MSG") +"";
                    if("E".equals(status)) {
                        wbackOutput.setResult("E");
                        wbackOutput.setMessage(message);
                    }
                }else{
                    RstMsg[] resArr = new RstMsg[result.size()];
                    for(int i=0;i<result.size();i++){
                        RstMsg rstModel = new RstMsg();
                        Map valMap = result.get(i);
                        log.info("valMap===" + valMap);
                        String status = valMap.get("STATUS") +"";
                        String msg = valMap.get("MSG") +"";
                        String zoblnr = valMap.get("ZOBLNR") +"";
                        rstModel.setMsg(msg);
                        rstModel.setStatus(status);
                        rstModel.setZoblnr(zoblnr);
                        resArr[i] = rstModel;
                    }
                    rstMsgs.setRstMsg(resArr);
                    wbackOutput.setRstMsgs(rstMsgs);
                    threadpool.shutdownNow();
                }
            }
        }catch (Exception e){
            log.info("预算申请单处理返回异常,消息为:" + e.getMessage());
            wbackOutput.setResult("E");
            wbackOutput.setMessage("传输失败-" + e.getMessage());
        }
        return wbackOutput;
    }

    private Map<String,Map<String,String>> procTableData() {
        Map<String,Map<String,String>> dataMap = new HashMap();
        //采购预算单业务表单
        FormBean pbCap4formBean = CAP4FormKit.getFormBean(cap4FormManager, pbTableCode); //采购预算
        FormBean tbCap4formBean = CAP4FormKit.getFormBean(cap4FormManager, tbTableCode); //国内出差
        FormBean fbCap4formBean = CAP4FormKit.getFormBean(cap4FormManager, fbTableCode); //财务

        FormBean aOrgPbCap4formBean = CAP4FormKit.getFormBean(cap4FormManager, aOrgPbTableCode); //A采购预算
        FormBean aOrgTbCap4formBean = CAP4FormKit.getFormBean(cap4FormManager, aOrgTbTableCode); //A国内出差
        //FormBean aOrgFbCap4formBean = CAP4FormKit.getFormBean(cap4FormManager, aOrgFbTableCode); //A财务

        Map pbMap = getTableMap(pbCap4formBean);
        log.info("pbMap===" + pbMap);
        Map tbMap = getTableMap(tbCap4formBean);
        log.info("tbMap===" + tbMap);
        Map fbMap = getTableMap(fbCap4formBean);
        log.info("fbMap===" + fbMap);

        //A事业部表单信息
        Map aOrgPbMap = getTableMap(aOrgPbCap4formBean);
        log.info("aOrgPbMap===" + aOrgPbMap);
        Map aOrgTbMap = getTableMap(aOrgTbCap4formBean);
        log.info("aOrgTbMap===" + aOrgTbMap);
        //Map aOrgFbMap = getTableMap(aOrgFbCap4formBean);
        //log.info("aOrgFbMap===" + aOrgFbMap);

        dataMap.put("Z",pbMap); //综合采购
        dataMap.put("G",tbMap); //国内出差
        dataMap.put("C",fbMap); //财务
        dataMap.put("AZ",aOrgPbMap); //综合采购
        dataMap.put("AG",aOrgTbMap); //国内出差
        //dataMap.put("AC",aOrgFbMap); //财务

        return dataMap;
    }

    private Map getTableMap(FormBean cap4formBean){ //处理表单字段
        Map valMap = new HashMap();
        String tableName = CAP4FormKit.getTableNameByDisPlayName(cap4formBean, "", true);
        log.info("tableName===" + tableName);
        String zoblnrFiled = CAP4FormKit.getFieldTaleId(cap4formBean,"SAP外部单据号");
        log.info("zoblnrFiled===" + zoblnrFiled);
        String cgdhFiled = CAP4FormKit.getFieldTaleId(cap4formBean,"SAP预算单号");
        String itNameFiled = CAP4FormKit.getFieldTaleId(cap4formBean,"SAPIT负责人");
        String cwNameFiled = CAP4FormKit.getFieldTaleId(cap4formBean,"SAP财务负责人");
        String messageField = CAP4FormKit.getFieldTaleId(cap4formBean,"SAP返回结果");

        valMap.put("tableName",tableName);
        valMap.put("zoblnrFiled",zoblnrFiled);
        valMap.put("cgdhFiled",cgdhFiled);
        valMap.put("itNameFiled",itNameFiled);
        valMap.put("cwNameFiled",cwNameFiled);
        valMap.put("messageField",messageField);
        return valMap;
    }

}
