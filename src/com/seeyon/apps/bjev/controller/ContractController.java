package com.seeyon.apps.bjev.controller;

import com.seeyon.apps.bjev.common.A8xDataRow;
import com.seeyon.apps.bjev.common.A8xFormDataWrapper;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ContractController {
    private static Log log = LogFactory.getLog(ContractController.class);

    @RequestMapping
    public ModelAndView getContractPage(HttpServletRequest request, HttpServletResponse response){
        String fieldId = request.getParameter("fieldId");
        String formID = request.getParameter("formId");
        String recordID = request.getParameter("formRecordId");
        String subName = request.getParameter("subName");
        String fieldVal = request.getParameter("fieldVal");
        String subId = request.getParameter("subRecordId");//行记录ID

        log.info(fieldId+"==="+formID+"==="+recordID+"==="+subName+"==="+fieldVal+"==="+subId);

        CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");

        FormBean formBean = cap4FormManager.getForm(Long.valueOf(formID), false);

        FormDataMasterBean cacheFormData = cap4FormManager.getSessioMasterDataBean(Long.valueOf(recordID));

        A8xFormDataWrapper data = new A8xFormDataWrapper(formBean, cacheFormData);
        A8xDataRow tmpRow = data.getMasterDataRow();
        log.info("主表数据tmpRow:"+tmpRow);    //{中文名：value}

        String hsmc = tmpRow.get("核算单位名称") == null? "" : tmpRow.get("核算单位名称")+"";
        String jylx = tmpRow.get("交易类型") == null? "" : tmpRow.get("交易类型")+"";
        String glmc = tmpRow.get("关联方清单") == null? "" : tmpRow.get("关联方清单")+"";


        Map<String, String> fieldMap = formBean.getAllFieldDisplayMap();
        log.info("字段名:"+fieldMap);

        //从表数据缓存
        List<FormTableBean> subTableBean = formBean.getSubTableBean();

        List<Map> list = new ArrayList();
        for(int i=0;i<subTableBean.size();i++){			//判断有多少个从表
            List<Map<String, Object>> sonData = data.getChildDataList(i);
            for(Map sonVal : sonData){
                list.add(sonVal);
            }
        }
        log.info("从表数据:"+list);
        //拿到点击控件的那一行从表缓存
        Map idMap=null;
        for(Map li : list){
            if(subId.equals(li.get("id")+"")){
                idMap = li;
            }
        }

        log.info("控件行缓存:"+idMap);

        //字段名与值的对应关系
        Map s = new HashMap();
        if(idMap!=null){
            for(Object key : fieldMap.keySet()){
                for(Object k : idMap.keySet()){
                    if(fieldMap.get(key).equals(k)){
                        s.put(key,idMap.get(k));
                    }
                }
            }
        }

        String gjahr = s.get("年度") == null? "" :s.get("年度")+"";
        String glht = s.get("我方合同编号") == null? "" :s.get("我方合同编号")+"";


        ModelAndView mav = new ModelAndView("contract/contractInfo");
        mav.addObject("hsmc",hsmc);
        mav.addObject("jylx",jylx);
        mav.addObject("glmc",glmc);
        mav.addObject("gjahr",gjahr);
        mav.addObject("glht",glht);

        return mav;
    }
}
