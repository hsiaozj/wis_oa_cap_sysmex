package com.seeyon.apps.bjev.services.impl.rembu;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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

public class RembuManagerServiceImpl extends BaseServiceImpl implements RembuManagerService{
	private static Log log = LogFactory.getLog(RembuManagerServiceImpl.class);
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
		String compCode = (String) map.get("compCode");
		String year = (String) map.get("year");
		String id = (String) map.get("id");
		
		
		Map config = new HashMap();
		config.put("wsUrl", WsFlow.getSth("k2004"));
		config.put("wsFunction", "MT_K2004");
		config.put("wsNameSpace", "http://www.bjev.com.cn/k2");
		config.put("wsUserName", WsFlow.getSth("wsUserName"));
		config.put("wsPassWord", WsFlow.getSth("wsPassWord"));
		
		String xml = "<ROOT>"+
						"<HEADER>"+
					        "<SENDER>K2</SENDER>"+
					        "<RECEIVER>SAP</RECEIVER>"+
					        "<DTSEND>2020-05-26T10:16:16.112+08:00</DTSEND>"+
					        "<INTFID>OA002</INTFID>"+
						"</HEADER>"+
						"<ITEM>"+
				            "<BUKRS>"+compCode+"</BUKRS>"+
				            "<GJAHR>"+year+"</GJAHR>"+
				            "<ZOBLNR>"+id+"</ZOBLNR>"+
				         "</ITEM>"+
					  "</ROOT>";
	    
		OMElement isInput = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes("UTF-8"))).getDocumentElement();
		OMElement result = doSapMethod(isInput, config,"k2");
		String rstStr = result.getChildren().hasNext() ? result.getChildren().next().toString() : null;
		
		Map budMap = new HashMap();
		if(rstStr != null && !"".equals(rstStr)){
			String rstStrXml = XmlUtil.subStringBetween(rstStr, "<MSGTY>", "</MSGTY>");
			if(rstStrXml.equals("S") ){
				String money = XmlUtil.subStringBetween(rstStr, "<MSGTX>", "</MSGTX>");
				
				if(money.contains("-")){
					money="-"+money.replace("-", "");
				}
				
				BigDecimal big = new BigDecimal(money.trim());
				budMap.put("AMOUNT", big);
			}else{
				String budget = XmlUtil.subStringBetween(rstStr, "<MSGTX>", "</MSGTX>");
				budMap.put("AMOUNT", budget);
			}
		}
		return budMap;
	}
}
