package com.seeyon.apps.bjev.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Xml2JsonUtil {
    /**
     * 转换一个xml格式的字符串到json格式
     *
     * @param xml
     *            xml格式的字符串
     * @return 成功返回json 格式的字符串;失败反回null
     */
    @SuppressWarnings("unchecked")
    public static JSONObject xml2JSON(String xml) {
        JSONObject obj = new JSONObject();
        try {
            InputStream is = new ByteArrayInputStream(xml.getBytes("utf-8"));
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build(is);
            Element root = doc.getRootElement();
            obj.put(root.getName(), iterateElement(root));
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 转换一个xml格式的字符串到json格式
     *
     * @param file
     *            java.io.File实例是一个有效的xml文件
     * @return 成功反回json 格式的字符串;失败反回null
     */
    @SuppressWarnings("unchecked")
    public static JSONObject xml2JSON(File file) {
        JSONObject obj = new JSONObject();
        try {
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build(file);
            Element root = doc.getRootElement();
            obj.put(root.getName(), iterateElement(root));
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 一个迭代方法
     *
     * @param element
     *            : org.jdom.Element
     * @return java.util.Map 实例
     */
    @SuppressWarnings("unchecked")
    private static Map  iterateElement(Element element) {
        List jiedian = element.getChildren();
        Element et = null;
        Map obj = new HashMap();
        List list = null;
        for (int i = 0; i < jiedian.size(); i++) {
            list = new LinkedList();
            et = (Element) jiedian.get(i);
            if (et.getTextTrim().equals("")) {
                if (et.getChildren().size() == 0)
                    continue;
                if (obj.containsKey(et.getName())) {
                    list = (List) obj.get(et.getName());
                }
                list.add(iterateElement(et));
                obj.put(et.getName(), list);
            } else {
                if (obj.containsKey(et.getName())) {
                    list = (List) obj.get(et.getName());
                }
                list.add(et.getTextTrim());
                obj.put(et.getName(), list);
            }
        }
        return obj;
    }

    // 测试
    public static void main(String[] args) throws AxisFault, UnsupportedEncodingException, XMLStreamException {
        String xml = "<ROOT>"+
                "<IS_BUSINFO>" +
                "<DATA>" +
                "<item>" +
                "<ZHSMC>"+""+"</ZHSMC>" +
                "<ZGLMC>"+""+"</ZGLMC>" +
                "<ZGLHT>"+""+"</ZGLHT>" +
                "<GJAHR>"+"2022"+"</GJAHR>" +
                "<ZJYLX>"+""+"</ZJYLX>" +
                "</item>" +
                "</DATA>" +
                "</IS_BUSINFO>" +
                "<IS_SYSINFO>" +
                "<SENDER>OA</SENDER>" +
                "<RECEIVER>SAP</RECEIVER>" +
                "<DTSEND>"+"20221201144920125"+"</DTSEND>" +
                "<INTFID>EC002</INTFID>" +
                "<MSGID>"+System.currentTimeMillis()+""+"</MSGID>" +
                "<POMSGID>"+System.currentTimeMillis()+""+1+"</POMSGID>" +
                "<LANGUAGE>ZH</LANGUAGE>" +
                "<SERVICENAME>111</SERVICENAME>" +
                "</IS_SYSINFO>"+
                "</ROOT>";

        OMElement isInput = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes("UTF-8"))).getDocumentElement();
        String methodName = "ZRFC_FI_OA006";
        String nameSpace = "urn:sap-com:document:sap:rfc:functions";
        String wsUserName = "oa2po";
        String wsPassWord = "Aa123456";

        ServiceClient serviceClient = new ServiceClient();
        EndpointReference targetEPR = new EndpointReference("http://120.133.56.147:8022/sapdev/XISOAPAdapter/MessageServlet?senderParty=&senderService=BS_OA&receiverParty=&receiverService=&interface=SI_OA006_OA_REQ&interfaceNamespace=http://www.bjev.com.cn/oa");
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
        OMNamespace omNs = fac.createOMNamespace(nameSpace, "urn");
        OMElement method = fac.createOMElement(function, omNs);

        @SuppressWarnings("unchecked")
        Iterator<OMElement> iter = isInput.getChildElements();
        while(iter.hasNext()) {
            OMElement omElement = iter.next();
            method.addChild(omElement);
        }

        method.build();
        OMElement result = serviceClient.sendReceive(method);
        String rst = result.toString();
        System.out.println("SAP返回数据:" + rst);

        String status = XmlUtil.subStringBetween(rst, "<MSGTY>", "</MSGTY>");
        String msg = XmlUtil.subStringBetween(rst, "<MSGTX>", "</MSGTX>");
        if(status.equalsIgnoreCase("E")){
            List<Map> errList = new ArrayList();
            Map errMap = new HashMap();
            errMap.put("txt",msg);

            errList.add(errMap);

            Map errRstMap = new HashMap();
            errRstMap.put("rstList",errList);
            System.out.println("进入");
        }

        JSONObject rstJson= xml2JSON(rst);
        System.out.println(rstJson);

        Map rspJs = (Map) rstJson.get("ZRFC_FI_OA006.Response");
        List busJsArr = (List) rspJs.get("ES_BUSINFO");
        System.out.println(busJsArr);

        Map dataMap = (Map) busJsArr.get(0);
        List dataList = (List) dataMap.get("DATA");
        System.out.println(dataList);

        Map itemMap = (Map) dataList.get(0);
        List<Map> itemList = (List) itemMap.get("item");
        System.out.println(itemList);

        List<Map> allDataList = new ArrayList();

        for(Map detailMap : itemList){
            Map allDataMap = new HashMap();

            List zglmcList = (List) detailMap.get("ZGLMC");
            String zglmc = (String) zglmcList.get(0);

            List zjyxeList = (List) detailMap.get("ZJYXE");
            String zjyxe = (String) zjyxeList.get(0);

            List zjylxList = (List) detailMap.get("ZJYLX");
            String zjylx = (String) zjylxList.get(0);

//            List gjahrList = (List) detailMap.get("GJAHR");
//            String gjahr = (String) gjahrList.get(0);
            System.out.println("***********************");
            if(detailMap.get("GJAHR")!=null && detailMap.get("GJAHR") instanceof List){
                List zjymxList = (List) detailMap.get("GJAHR");
                String zjymx = (String) zjymxList.get(0);
                System.out.println("====================================gjahr");
            }

//            List zjymxList = (List) detailMap.get("ZJYMX");
//            String zjymx = (String) zjymxList.get(0);

            if(dataMap.get("ZJYMX")!=null&&dataMap.get("ZJYMX") instanceof List){
                List zjymxList = (List) detailMap.get("ZJYMX");
                String zjymx = (String) zjymxList.get(0);
            }

            List zglgxList = (List) detailMap.get("ZGLGX");
            String zglgx = (String) zglgxList.get(0);

            List zrzjeList = (List) detailMap.get("ZRZJE");
            String zrzje = (String) zrzjeList.get(0);

            List zhsmcList = (List) detailMap.get("ZHSMC");
            String zhsmc = (String) zhsmcList.get(0);

            List zhtyxdList = (List) detailMap.get("ZHTYXD");
            String zhtyxd = (String) zhtyxdList.get(0);

            List zhtljjeList = (List) detailMap.get("ZHTLJJE");
            String zhtljje = (String) zhtljjeList.get(0);

            List zkyedList = (List) detailMap.get("ZKYED");
            String zkyed = (String) zkyedList.get(0);

            List zhtfpsList = (List) detailMap.get("ZHTFPS");
            String zhtfps = (String) zhtfpsList.get(0);

            List zhtztList = (List) detailMap.get("ZHTZT");
            String zhtzt = (String) zhtztList.get(0);

            List zhtjeList = (List) detailMap.get("ZHTJE");
            String zhtje = (String) zhtjeList.get(0);

            List zhsdmList = (List) detailMap.get("ZHSDM");
            String zhsdm = (String) zhsdmList.get(0);

            List zhtfpzList = (List) detailMap.get("ZHTFPZ");
            String zhtfpz = (String) zhtfpzList.get(0);

            List bglmc2List = (List) detailMap.get("BGLMC2");
            String bglmc2 = (String) bglmc2List.get(0);

            List zhtfpyList = (List) detailMap.get("ZHTFPY");
            String zhtfpy = (String) zhtfpyList.get(0);

            List zgldmList = (List) detailMap.get("ZGLDM");
            String zgldm = (String) zgldmList.get(0);

            List zglhtList = (List) detailMap.get("ZGLHT");
            String zglht = (String) zglhtList.get(0);

            List zhtwyList = (List) detailMap.get("ZHTWY");
            String zhtwy = (String) zhtwyList.get(0);

            List zhtyxqList = (List) detailMap.get("ZHTYXQ");
            String zhtyxq = (String) zhtyxqList.get(0);

            allDataMap.put("zglmc",zglmc);
            allDataMap.put("zjyxe",zjyxe);
            allDataMap.put("zjylx",zjylx);
            //allDataMap.put("gjahr",gjahr);
            //allDataMap.put("zjymx",zjymx);
            allDataMap.put("zglgx",zglgx);
            allDataMap.put("zrzje",zrzje);
            allDataMap.put("zhsmc",zhsmc);
            allDataMap.put("zhtyxd",zhtyxd);
            allDataMap.put("zhtljje",zhtljje);
            allDataMap.put("zkyed",zkyed);
            allDataMap.put("zhtfps",zhtfps);
            allDataMap.put("zhtzt",zhtzt);
            allDataMap.put("zhtje",zhtje);
            allDataMap.put("zhsdm",zhsdm);
            allDataMap.put("zhtfpz",zhtfpz);
            allDataMap.put("bglmc2",bglmc2);
            allDataMap.put("zhtfpy",zhtfpy);
            allDataMap.put("zgldm",zgldm);
            allDataMap.put("zglht",zglht);
            allDataMap.put("zhtwy",zhtwy);
            allDataMap.put("zhtyxq",zhtyxq);

            allDataList.add(allDataMap);
        }
    }

    public static String getJsonVal(JSONArray jsonarray){
        try {
            if(jsonarray != null) {
                String val = jsonarray.getString(0);
                return val;
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
