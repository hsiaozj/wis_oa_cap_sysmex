package com.seeyon.apps.bjev.services.impl.budget;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.bjev.services.webService.BaseServiceImpl;
import com.seeyon.apps.bjev.util.WsFlow;
import com.seeyon.apps.bjev.util.XmlUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.annotation.AjaxAccess;

public class BudgetManagerServiceImpl extends BaseServiceImpl implements BudgetManagerService{
	private static Log log = LogFactory.getLog(BudgetManagerServiceImpl.class);
	@AjaxAccess
	@Override
	public FlipInfo findInfoData(FlipInfo fi, Map params) throws Exception {
		log.info("fi:"+fi.getData());
		log.info("params:"+params);
		Map budMap = this.doSapSyncMethod(params);
		
		List<Map> list = new ArrayList();
		list.add(budMap);
		
		fi.setTotal(budMap.size());
		fi.setSize(10);
		
		fi.setData(list);
		return fi;
	}
	@Override
	public Map<String, Object> doSapSyncMethod(Map<String, Object> map) throws Exception {
//		String date = (String) map.get("date");
		String comp = (String) map.get("comp");
		String center = (String) map.get("center");
		String wbs = (String) map.get("wbs");
		String project = (String) map.get("project");
		
		Date d = new Date();
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
		String date = s.format(d);
		
		Map config = new HashMap();
		config.put("wsUrl", WsFlow.getSth("oa001"));
		config.put("wsFunction", "MT_OA001_REQ");
		config.put("wsNameSpace", "http://www.bjev.com.cn/oa");
		config.put("wsUserName", WsFlow.getSth("wsUserName"));
		config.put("wsPassWord", WsFlow.getSth("wsPassWord"));
		
		String xml = "<ROOT>"+
						"<HEADER>"+
					        "<SENDER>K2</SENDER>"+
					        "<RECEIVER>SAP</RECEIVER>"+
					        "<DTSEND>2020-05-26T10:16:16.112+08:00</DTSEND>"+
					        "<INTFID>OA001</INTFID>"+
					        "<MSGID></MSGID>"+
						"</HEADER>"+
					    "<WBS>"+wbs+"</WBS>"+
					    "<FUNDS_CENTER>"+center+"</FUNDS_CENTER>"+
					    "<COMPANYCODE>"+comp+"</COMPANYCODE>"+
					    "<SEARCH_DATE>"+date+"</SEARCH_DATE>"+
					    "<ZRSV01>"+project+"</ZRSV01>"+
					    "<ZRSV02>?</ZRSV02>"+
					    "<ZRSV03>?</ZRSV03>"+
					  "</ROOT>";
	    
		OMElement isInput = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes("UTF-8"))).getDocumentElement();
		OMElement result = doSapMethod(isInput, config,"OA");
		String rstStr = result.toString();
		
		String status = XmlUtil.subStringBetween(rstStr, "<ZSTATUS>", "</ZSTATUS>");
		
		Map budMap = new HashMap();
		budMap.put("AMOUNT", 0);

		if(status.equals("S")){
			String flag = XmlUtil.subStringBetween(rstStr, "<ZMESSAGE>", "</ZMESSAGE>");
			if(flag.equals("获取到基金中心预算额度")){
				//基金中心
				String money = XmlUtil.subStringBetween(rstStr, "<FUND_BUDGET>", "</FUND_BUDGET>");
				if(money.contains("-")){
					money="-"+money.replace("-", "");
				}
				BigDecimal big = new BigDecimal(money.trim());
				
				budMap.put("AMOUNT", big);
			}
			
			if(flag.equals("获取到WBS预算额度")){
				String money = XmlUtil.subStringBetween(rstStr, "<WBS_BUDGET>", "</WBS_BUDGET>");
				if(money.contains("-")){
					money="-"+money.replace("-", "");
				}

				BigDecimal big = new BigDecimal(money.trim());
				budMap.put("AMOUNT", big);
			}
		}else{
			String message = XmlUtil.subStringBetween(rstStr, "<ZMESSAGE>", "</ZMESSAGE>");
			budMap.put("AMOUNT", message);
		}
		
		return budMap;
	}
}
