package com.seeyon.apps.bjev.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;

@Controller
public class BudgetController {
	private static Log log = LogFactory.getLog(BudgetController.class);
	FormBean formBean = new FormBean();
	private EnumManager enumManagerNew = (EnumManager)AppContext.getBean("enumManagerNew");
	@RequestMapping
	public ModelAndView getBudgetPage(HttpServletRequest request, HttpServletResponse response){
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
		
		String wbs=null;
		
		String center = null;
		
		String project = null;
		
		String yu =null;
		String yusuan="";
		//判断是否在主表
		if(tmpRow.get("预算内容")!=null){
			yu = tmpRow.get("预算内容")+"";
			
			CtpEnumItem enumItem = enumManagerNew.getEnumItem(Long.valueOf(yu));
			if(enumItem != null) {
				yusuan = enumItem.getShowvalue();
			}
		}
				
				
		//判断是否在主表
		if(tmpRow.get("SAPWBS编码")!=null){
			wbs = (String) tmpRow.get("SAPWBS编码");
		}
		//是否在从表
		if(s!=null){
			if(s.get("SAPWBS编码")!=null){
				wbs = (String) s.get("SAPWBS编码");
			}
		}
		
		//判断是否在主表
		if(tmpRow.get("SAP成本中心")!=null){
			center = (String) tmpRow.get("SAP成本中心");
		}
		//是否在从表
		if(s!=null){
			if(s.get("SAP成本中心") !=null){
				center = (String) s.get("SAP成本中心");
			}
		}
		
		if(tmpRow.get("SAP承诺项目")!=null){
			project = (String) tmpRow.get("SAP承诺项目");
		}
		
		if(s!=null){
			if(s.get("SAP承诺项目") !=null){
				project = (String) s.get("SAP承诺项目");
			}
		}
		
		//主表取
		String comp = (String) tmpRow.get("SAP公司代码");

//		String da = (String) tmpRow.get("SAP申请日期");
//		String time="";
//		if(da!=null && da.length()>0){
//			Date date = new Date(da);
//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//			time = dateFormat.format(date);
//		}
		
		if(yusuan.equals("项目类")){
			center="";
		}else if(yusuan.equals("资产类")){
			center="";
		}
		
		ModelAndView mav = new ModelAndView("budget/budgetInfo");
//		log.info(wbs+"="+comp+"="+center+"="+date);
		mav.addObject("wbs", wbs);
		mav.addObject("comp", comp);
		mav.addObject("center", center);
//		mav.addObject("date", time);
		mav.addObject("project", project);
		
		return mav;
	}
}
