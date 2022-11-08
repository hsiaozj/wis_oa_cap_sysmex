package com.seeyon.apps.bjev.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.bjev.common.A8xDataRow;
import com.seeyon.apps.bjev.common.A8xFormDataWrapper;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;

@Controller
public class RembuController {
	private static Log log = LogFactory.getLog(RembuController.class);
	FormBean formBean = new FormBean();
	
	@RequestMapping
	public ModelAndView getRembuPage(HttpServletRequest request, HttpServletResponse response){
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
		log.info("tmpRow:"+tmpRow);
		
//		Map<String, String> fieldMap = formBean.getAllFieldDisplayMap();
//		log.info("字段名:"+fieldMap);
//		
//		//从表数据缓存
//		List<FormTableBean> subTableBean = formBean.getSubTableBean();
//		
//		List<Map> list = new ArrayList();
//		for(int i=0;i<subTableBean.size();i++){			//判断有多少个从表
//			List<Map<String, Object>> sonData = data.getChildDataList(i);
//			for(Map sonVal : sonData){
//				list.add(sonVal);
//			}
//		}
//		log.info("从表数据:"+list);
//		//拿到点击控件的那一行从表缓存
//		Map idMap=null;
//		for(Map li : list){
//			if(subId.equals(li.get("id")+"")){
//				idMap = li;
//			}
//		}
//		
//		log.info("控件行缓存:"+idMap);
				
//		//字段名与值的对应关系
//		Map s = new HashMap();
//		if(idMap!=null){
//			for(Object key : fieldMap.keySet()){
//				for(Object k : idMap.keySet()){
//					if(fieldMap.get(key).equals(k)){
//						s.put(key,idMap.get(k));
//					}
//				}
//			}
//		}
		
		String compCode = tmpRow.get("SAP公司代码")+"";
		String year = tmpRow.get("SAP会计年度")+"";
		String id = tmpRow.get("SAP外部单据号")+"";
				
		ModelAndView mav = new ModelAndView("budget/RembuInfo");
		
		log.info(compCode+"="+year+"="+id);
		mav.addObject("compCode", compCode);
		mav.addObject("year", year);
		mav.addObject("id", id);
		
		return mav;
	}
}
