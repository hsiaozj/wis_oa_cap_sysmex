package com.seeyon.apps.bjev.services.webService;

import java.util.Iterator;
import java.util.Map;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties.Authenticator;
import org.apache.log4j.Logger;

public abstract class SendDataServiceImpl implements SendDataService{
	private static final Logger log = Logger.getLogger(SendDataServiceImpl.class);

	public OMElement doSapMethod(OMElement inEle, Map<String, Object> wsConfigInfos,String value) throws Exception {
		String url = (String)wsConfigInfos.get("wsUrl");
		String methodName = (String)wsConfigInfos.get("wsFunction");
		String nameSpace = (String)wsConfigInfos.get("wsNameSpace");
		String wsUserName = (String)wsConfigInfos.get("wsUserName");
		String wsPassWord = (String)wsConfigInfos.get("wsPassWord");
		
		ServiceClient serviceClient = new ServiceClient();
		EndpointReference targetEPR = new EndpointReference(url);
		Options options = serviceClient.getOptions();
		options.setExceptionToBeThrownOnSOAPFault(false);
		options.setTimeOutInMilliSeconds(300000L);
		options.setTo(targetEPR);
		
		Authenticator basicAuth = new Authenticator();
		basicAuth.setUsername(wsUserName);
		basicAuth.setPassword(wsPassWord);
		options.setProperty(HTTPConstants.AUTHENTICATE, basicAuth);
		options.setAction(nameSpace + ":" + methodName);
		String function = methodName;
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace(nameSpace, value);
		OMElement method = fac.createOMElement(function, omNs);

		@SuppressWarnings("unchecked")
		Iterator<OMElement> iter = inEle.getChildElements();
		while(iter.hasNext()) {
			OMElement omElement = iter.next();
			method.addChild(omElement);
		}

		method.build();
		log.info("请求SAP的URL:" + url);
		log.info("请求SAP数据:" + method);
		OMElement result = serviceClient.sendReceive(method);
		log.info("SAP返回数据:" + result);
		return result;
	}

	public OMElement getOMElement(String eleStr) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace("", "");
		OMElement ele = fac.createOMElement(eleStr, omNs);
		return ele;
	}

	public String getStrVal(Object obj) {
		return obj != null ? obj.toString() : "";
	}

	@Override
	public abstract Map<String, Object> doSapSyncMethod(Map<String, Object> map) throws Exception;

}
