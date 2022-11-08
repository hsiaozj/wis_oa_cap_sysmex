package com.seeyon.apps.bjev.services.webService;

import com.seeyon.apps.bjev.services.pojo.PbInput;
import com.seeyon.apps.bjev.services.pojo.WbackOutput;
import com.seeyon.apps.bjev.services.webService.services.impl.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.bjev.apps.util.XmlUtil;
import com.seeyon.apps.bjev.services.WsBaseService;
import com.seeyon.apps.bjev.services.pojo.Mdata;
import com.seeyon.apps.bjev.services.pojo.MdataOutput;
import com.seeyon.apps.bjev.services.pojo.WisBaseOutput;
import com.seeyon.apps.bjev.util.LogUtil;
import com.seeyon.ctp.common.AppContext;


public class WebServicesImpl implements WebServices{
	private static Log log = LogFactory.getLog(WebServicesImpl.class);
	
	@Override
	public MdataOutput syncMdata(Mdata input) {
		WsBaseService mdataService = (WsBaseService)AppContext.getBean("mdataService");
		MdataOutput mdataOutput = (MdataOutput)addLogsAndDoWork(input, mdataService);
		return mdataOutput;
	}

	@Override
	public WbackOutput procBudgetBusiness(PbInput input) {
		WsBaseService budgetService = new BudgetBusinesServiceImpl();
		WbackOutput wbOutput = (WbackOutput)addLogsAndDoWork(input, budgetService);
		return wbOutput;
	}

	private WisBaseOutput addLogsAndDoWork(Object input, WsBaseService wsBaseService) {
		WisBaseOutput rst;
		try {
			String xml = XmlUtil.getXml(input);
			log.info("SAP请求OA数据:"+xml);
			rst = wsBaseService.doWsWork(input);
			log.info("OA返回SAP数据:"+XmlUtil.getXml(rst));
		} catch (Exception e) {
			rst = new WisBaseOutput();
			rst.setResult("E");
			rst.setMessage(e.getMessage());
			log.info("SAP请求OA异常:"+LogUtil.getTrace(e));
			e.printStackTrace();
		}
		return rst;
	}
}
