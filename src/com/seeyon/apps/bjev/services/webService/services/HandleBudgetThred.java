package com.seeyon.apps.bjev.services.webService.services;

import com.seeyon.apps.bjev.services.FormServices;
import com.seeyon.apps.bjev.services.pojo.PbItem;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataDAO;
import com.seeyon.ctp.common.AppContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class HandleBudgetThred implements Callable<List<Map>> {
    private static final Logger log = Logger.getLogger(HandleBudgetThred.class);
    private List<PbItem> dataList; //处理业务数据
    private Map<String, Map<String, String>> dataMap; //业务表单数据
    private CAP4FormDataDAO cap4FormDataDAO = (CAP4FormDataDAO) AppContext.getBean("cap4FormDataDAO");
    private FormServices formServices = (FormServices) AppContext.getBean("pocFormServices");
    private Object lock = new Object();
    private String threadName = "";
    private String registerCode = "";

    public HandleBudgetThred(String threadName, List<PbItem> dataList, Map<String, Map<String, String>> dataMap, String registerCode) {
        this.dataList = dataList;
        this.dataMap = dataMap;
        this.threadName = threadName;
        this.registerCode = registerCode;
    }

    @Override
    public List<Map> call() {
        List<Map> resultList = new ArrayList();
        log.info("线程----" + threadName + "正在处理,处理数量为:" + dataList.size());
        synchronized (lock) {
            for (PbItem item : dataList) {
                Map procMap = new HashMap();
                try {
                    Map<String, Object> fields = new HashMap<>();
                    String zoblnr = item.getZoblnr(); //外部流水单号
                    String zstatus = item.getZstatus(); //回写状态
                    String zmessage = item.getZmessage(); //回写返回消息
                    String bukrs = item.getBukrs(); //公司代码

                    log.info("zoblnr========" + zoblnr);
                    log.info("zstatus========" + zstatus);
                    log.info("zmessage========" + zmessage);

                    if (StringUtils.isBlank(zoblnr)) { //采购申请申请单流水号
                        log.info("预算申请单流水号为空");
                        procMap.put("STATUS", "E");
                        procMap.put("MSG", "预算申请单流水号为空");
                        procMap.put("ZOBLNR", "");
                        resultList.add(procMap);
                        continue;
                    }

                    log.info("zoblnr=====" + zoblnr);
                    Map<String, String> typeMap = getTypeMap(zoblnr, bukrs);
                    log.info("typeMap=====" + typeMap);

                    String zoblnrFiled = typeMap.get("zoblnrFiled");
                    String cgdhFiled = typeMap.get("cgdhFiled");
                    String itNameFiled = typeMap.get("itNameFiled");
                    String cwNameOneFiled = typeMap.get("cwNameFiled");
                    String messageField = typeMap.get("messageField");
                    String tableName = typeMap.get("tableName");

                    //查询表单记录id
                    Map valMap = new HashMap();
                    valMap.put(zoblnrFiled, zoblnr);

                    // 查询底表中是否有这个人
                    log.info("valMap===" + valMap);
                    Long formDefId = 0L;
                    String itNameValue = "";
                    String cwNameValue = "";
                    List<Map> tableList = this.getValueByMap(null, valMap, tableName, "and");
                    log.info("tableList===" + tableList);

                    if (tableList != null && tableList.size() > 0) {
                        Map tableModel = tableList.get(0);
                        formDefId = Long.valueOf(tableModel.get("id") + "");
                        itNameValue = tableModel.get(itNameFiled) + "";
                        cwNameValue = tableModel.get(cwNameOneFiled) + "";
                    } else {
                        log.info("在OA中查询不到这条预算申请单记录,zoblnr为:" + zoblnr);
                        procMap.put("STATUS", "E");
                        procMap.put("MSG", "在OA中查询不到这条预算申请单记录,流水单号为:" + zoblnr);
                        procMap.put("ZOBLNR", zoblnr);
                        resultList.add(procMap);
                        continue;
                    }

                    log.info("itNameValue====" + itNameValue);
                    log.info("cwNameValue====" + cwNameValue);
                    if (StringUtils.isNotBlank(zstatus) && zstatus.equals("S")) {
                        int indexLen = zmessage.indexOf("|");
                        String startStr = zmessage.substring(0, indexLen);
                        String endStr = zmessage.substring(indexLen + 1, zmessage.length());
                        fields.put(cgdhFiled, startStr);
                        fields.put(messageField, endStr);
                        log.info("fields===" + fields);
                        boolean b = cap4FormDataDAO.updateData(formDefId, tableName, fields);
                        if (!b) {
                            log.info("更新业务底表错误,表单id为" + formDefId + "请求参数为" + fields);
                            procMap.put("STATUS", "E");
                            procMap.put("MSG", "更新预算申请单底表错误,流水单号为:" + zoblnr);
                            procMap.put("ZOBLNR", zoblnr);
                            resultList.add(procMap);
                        } else {
                            procMap.put("STATUS", "S");
                            procMap.put("MSG", "预算申请单回写OA表单成功");
                            procMap.put("ZOBLNR", zoblnr);
                            resultList.add(procMap);
                        }
                    } else { //传输失败,发送消息给对应的人
                        fields.put(messageField, zmessage);  //更新回写表单
                        log.info("fields===" + fields);
                        boolean b = cap4FormDataDAO.updateData(formDefId, tableName, fields);
                        if (!b) {
                            log.info("预算申请单推送返回错误,更新表单不成功,表单id为" + formDefId + "请求参数为" + fields);
                            procMap.put("STATUS", "E");
                            procMap.put("MSG", "预算申请单推送返回错误,更新OA表单不成功,流水单号为:" + zoblnr);
                            resultList.add(procMap);
                        } else {
                            formServices.pushMessage(zmessage, itNameValue, registerCode);
                            formServices.pushMessage(zmessage, cwNameValue, registerCode);
                            procMap.put("STATUS", "S");
                            procMap.put("MSG", "预算申请单回写返回错误消息成功,流水单号为:" + zoblnr);
                            procMap.put("ZOBLNR", zoblnr);
                            resultList.add(procMap);
                        }
                    }
                } catch (Exception e) {
                    log.info("预算申请单处理异常,消息为:" + e.getMessage());
                    procMap.put("STATUS", "E");
                    procMap.put("MSG-", e.getMessage());
                    resultList.add(procMap);
                }
            }
        }
        return resultList;
    }

    private Map<String, String> getTypeMap(String zoblnr, String bukrs) {
        Map<String, String> map = new HashMap<>();
        if (zoblnr.contains("Z")) { //采购预算申请单
            if (bukrs.equals("1050")) {
                map = dataMap.get("Z");
            } else {
                map = dataMap.get("AZ");
            }
        } else if (zoblnr.contains("C")) { //财务
            if (bukrs.equals("1050")) {
                map = dataMap.get("C");
            } else {
                map = dataMap.get("AC");
            }
        } else { //国内出差
            if (bukrs.equals("1050")) {
                map = dataMap.get("G");
            } else {
                map = dataMap.get("AG");
            }
        }
        return map;
    }

    private List<Map> getValueByMap(String[] returnFields, Map<String, Object> hm, String tableName, String fieldsLogic)
            throws Exception {
        return cap4FormDataDAO.getValueByMap(returnFields, hm, tableName, fieldsLogic);
    }

}
