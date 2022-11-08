package com.seeyon.apps.bjev.services.webService.services;

import com.seeyon.apps.bjev.util.WsFlow;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.transport.http.HttpTransportProperties.Authenticator;
import java.io.ByteArrayInputStream;
import java.util.Iterator;

public class HandleOtherBudgetThred implements Runnable {

    private String xmlString = "";
    private static final Logger log = Logger.getLogger(HandleOtherBudgetThred.class);

    public HandleOtherBudgetThred(String xmlString) {
        this.xmlString = xmlString;
    }

    @Override
    public void run() {
        log.info("handleOtherBudgetThred====xmlString==" + xmlString);
        String url = WsFlow.getSth("k2url");
        String methodName = WsFlow.getSth("k2methodName");
        String nameSpace = WsFlow.getSth("k2nameSpace");
        String wsUserName = "";
        String wsPassWord = "";

        try {
            String requestXml = "<ROOT><reqtXML>" + xmlString + "</reqtXML></ROOT>";
            OMElement inEle = new StAXOMBuilder(new ByteArrayInputStream(requestXml.getBytes("UTF-8"))).getDocumentElement();
            log.info("inEle==" + inEle);

            ServiceClient serviceClient = new ServiceClient();
            EndpointReference targetEPR = new EndpointReference(url);
            Options options = serviceClient.getOptions();
            options.setExceptionToBeThrownOnSOAPFault(false);
            options.setTimeOutInMilliSeconds(300000L);
            options.setTo(targetEPR);
            options.setProperty(org.apache.axis2.Constants.Configuration.DISABLE_SOAP_ACTION, true);

            log.info("url===========" + url);
            Authenticator basicAuth = new Authenticator();
            basicAuth.setUsername(wsUserName);
            basicAuth.setPassword(wsPassWord);
            options.setProperty(HTTPConstants.AUTHENTICATE, basicAuth);
            options.setAction(nameSpace + ":" + methodName);
            String function = methodName;
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace(nameSpace, "web");
            OMElement method = fac.createOMElement(function, omNs);

            @SuppressWarnings("unchecked")
            Iterator<OMElement> iter = inEle.getChildElements();
            while(iter.hasNext()) {
                OMElement omElement = iter.next();
                method.addChild(omElement);
            }
            log.info("method===========" + method);
            method.build();
            log.info("发送K2的URL:" + url);
            log.info("发送K2的数据:" + method);
            serviceClient.sendRobust(method);

        } catch (Exception e) {
            log.info("请求K2接口异常,返回消息为:" + e.getMessage());
        }
    }
}
