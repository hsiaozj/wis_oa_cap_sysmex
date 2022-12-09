package com.seeyon.apps.bjev.services.impl.contractApproval;

import com.seeyon.apps.bjev.util.LogUtil;
import com.seeyon.apps.bjev.util.XmlUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ContractApprovalServiceImpl implements ContractApprovalService {
    private static final Logger log = Logger.getLogger(ContractApprovalServiceImpl.class);

    @Override
    public Map<String, Object> doSapSyncMethod(Map<String, Object> map) throws Exception {
        Map<String, Object> returnResult = new HashMap();
        try {
            String xml = (String)map.get("xml");
            xml =xml.replace("&", "&amp;");

            StringBuilder stringBuilder=new StringBuilder(xml);
            OMElement isInput = new StAXOMBuilder(new ByteArrayInputStream(stringBuilder.toString().getBytes("UTF-8"))).getDocumentElement();
            OMElement result = doSapMethod(isInput, map,"urn");

            String rstStr = XmlUtil.subStringBetween(result.toString(), "<ES_SYSINFO>", "</ES_SYSINFO>");
            log.info("解析结果："+rstStr);

            if(StringUtils.isNotBlank(rstStr)&&!rstStr.contains("无法截取目标字符串")){
                String infostatus = XmlUtil.subStringBetween(rstStr, "<MSGTY>", "</MSGTY>");
                if(!infostatus.contains("无法截取目标字符串")&&"S".equalsIgnoreCase(infostatus)){
                    String status = XmlUtil.subStringBetween(result.toString(), "<ZSTATUS>", "</ZSTATUS>");
                    String message = XmlUtil.subStringBetween(result.toString(), "<ZSTATUSTXT>", "</ZSTATUSTXT>");
                    if(!status.contains("无法截取目标字符串")){
                        returnResult.put("status", status);
                        returnResult.put("message", message);
                        return returnResult;
                    }
                }
            }
            returnResult.put("status", "E");
            returnResult.put("message", "合同审批SAP创建失败,返回信息为："+result.toString());
        }catch (Exception ex){
            ex.printStackTrace();
            log.info("合同审批出现异常"+ex.getMessage());

            String message = "合同审批不成功，系统推送数据时出现异常:";
            String errInfo = LogUtil.getTrace(ex);
            if(org.apache.commons.lang.StringUtils.isNotBlank(errInfo)) {
                if(errInfo.length() > 450) {
                    message += ","+errInfo.substring(0, 450);
                }else {
                    message += ","+errInfo;
                }
            }
            returnResult.put("status", "E");
            returnResult.put("message", message);
        }
        return returnResult;
    }

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

        HttpTransportProperties.Authenticator basicAuth = new HttpTransportProperties.Authenticator();
        basicAuth.setUsername(wsUserName);
        basicAuth.setPassword(wsPassWord);
        options.setProperty(HTTPConstants.AUTHENTICATE, basicAuth);
        options.setAction(nameSpace + ":" + methodName);
        String function = methodName;
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(nameSpace, value);
        OMElement method = fac.createOMElement(function, omNs);

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
}
