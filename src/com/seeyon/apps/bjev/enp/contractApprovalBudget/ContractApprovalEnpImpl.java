package com.seeyon.apps.bjev.enp.contractApprovalBudget;

import com.seeyon.apps.bjev.enp.BusinessEnpService;
import com.seeyon.apps.bjev.util.CAP4FormKit;
import com.seeyon.apps.bjev.util.SnowFlakeUtil;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContractApprovalEnpImpl implements BusinessEnpService {
    private CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");

    @Override
    public Map<String, Object> preProcessMethod(Map<String, Object> paramMap) throws Exception {
        String templateCode = (String) paramMap.get("code");
        SnowFlakeUtil snowFlakeUtil = new SnowFlakeUtil();

        FormBean cap4formBean = CAP4FormKit.getFormBean(cap4FormManager, templateCode);
        String tableName = CAP4FormKit.getTableNameByDisPlayName(cap4formBean, "核算单位名称", true);
        String formsonName = CAP4FormKit.getTableNameByDisPlayName(cap4formBean, "货币类别", false);//从表表名
        List<Map> formainList = (List<Map>) paramMap.get(tableName);
        List<Map> formsonList = (List<Map>) paramMap.get(formsonName);

        String zhsmcField = CAP4FormKit.getFieldTaleId(cap4formBean, "核算单位名称");
        String zglmcField = CAP4FormKit.getFieldTaleId(cap4formBean, "关联方清单");
        String zhtkField = CAP4FormKit.getFieldTaleId(cap4formBean, "合同生效日期");
        String zhtjField = CAP4FormKit.getFieldTaleId(cap4formBean, "合同结束日期");
        String zjylxField = CAP4FormKit.getFieldTaleId(cap4formBean, "交易类型");

        if (formainList != null && formainList.size() > 0) {
            Map data = formainList.get(0);
            String zhsmc = data.get(zhsmcField) == null ? "" : data.get(zhsmcField) + "";
            String zglmc = data.get(zglmcField) == null ? "" : data.get(zglmcField) + "";
            String zhtk = data.get(zhtkField) == null ? "" : data.get(zhtkField) + "";
            String zhtj = data.get(zhtjField) == null ? "" : data.get(zhtjField) + "";
            String zjylx = data.get(zjylxField) == null ? "" : data.get(zjylxField) + "";

            for(Map dataMap:formsonList){
                dataMap.put("field9999", zhsmc);
                dataMap.put("field9998", zglmc);
                dataMap.put("field9997", zhtk);
                dataMap.put("field9996",zhtj);
                dataMap.put("field9995", zjylx);
            }
        }

        Map<String, Object> dataMap=new HashMap<>();
        List<Map> info=new ArrayList<>();
        dataMap.put("field0999", "OA");
        //接收方
        dataMap.put("field0998", "SAP");
        long id = snowFlakeUtil.nextId();
        dataMap.put("field0996", String.valueOf(id));
        dataMap.put("field0995", "ZH");
        info.add(dataMap);
        paramMap.put("formmain_9999",info);

        return paramMap;
    }

    @Override
    public Map<String, Object> finishedProcessMethod(Map<String, Object> paramMap) throws Exception {
        return paramMap;
    }
}
