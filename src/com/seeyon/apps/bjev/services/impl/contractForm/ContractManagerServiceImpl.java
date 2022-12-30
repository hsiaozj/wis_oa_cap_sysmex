package com.seeyon.apps.bjev.services.impl.contractForm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.bjev.services.webService.BaseServiceImpl;
import com.seeyon.apps.bjev.util.WsFlow;
import com.seeyon.apps.bjev.util.Xml2JsonUtil;
import com.seeyon.apps.bjev.util.XmlUtil;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ContractManagerServiceImpl extends BaseServiceImpl implements ContractManagerService{
    private static Log log = LogFactory.getLog(ContractManagerServiceImpl.class);
    @AjaxAccess
    @Override
    public FlipInfo findInfoData(FlipInfo fi, Map params) throws Exception {
        log.info("fi:"+fi.getData());
        log.info("params:"+params);
        Map contractMap = this.doSapSyncMethod(params);
        log.info("返回数据:"+ JSON.toJSONString(contractMap));

        List<Map> rstList = (List) contractMap.get("rstList");

        fi.setTotal(contractMap.size());
        fi.setSize(10);

        fi.setData(rstList);
        return fi;
    }

    @Override
    public Map<String, Object> doSapSyncMethod(Map<String, Object> map) throws Exception {
        String hsmc = (String) map.get("hsmc");
        String jylx = (String) map.get("jylx");
        String glmc = (String) map.get("glmc");
        String gjahr = (String) map.get("gjahr");
        String glht = (String) map.get("glht");

        Date date = new Date();
        String strDateFormat = "yyyyMMddHHmmss";

        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String dtsent =  localDateTime.format(DateTimeFormatter.ofPattern(strDateFormat));

        String msgId = System.currentTimeMillis()+"";
        String pomsgId = System.currentTimeMillis()+1+"";
        Map config = new HashMap();
        config.put("wsUrl", "http://120.133.56.147:8022/sapdisp/XISOAPAdapter/MessageServlet?senderParty=&senderService=BS_OA&receiverParty=&receiverService=&interface=SI_OA006_OA_REQ&interfaceNamespace=http://www.bjev.com.cn/oa");
        config.put("wsFunction", "ZRFC_FI_OA006");
        config.put("wsNameSpace", "urn:sap-com:document:sap:rfc:functions");
        config.put("wsUserName", WsFlow.getSth("wsUserName"));
        config.put("wsPassWord", WsFlow.getSth("wsPassWord"));

        String xml ="<ROOT>"+
                "<IS_BUSINFO>" +
                        "<DATA>" +
                            "<item>" +
                                "<ZHSMC>"+hsmc+"</ZHSMC>" +
                                "<ZGLMC>"+glmc+"</ZGLMC>" +
                                "<ZGLHT>"+glht+"</ZGLHT>" +
                                "<GJAHR>"+gjahr+"</GJAHR>" +
                                "<ZJYLX>"+jylx+"</ZJYLX>" +
                                "</item>" +
                                "</DATA>" +
                    "</IS_BUSINFO>" +
                    "<IS_SYSINFO>" +
                        "<SENDER>OA</SENDER>" +
                        "<RECEIVER>SAP</RECEIVER>" +
                        "<DTSEND>"+dtsent+"</DTSEND>" +
                        "<INTFID>EC002</INTFID>" +
                        "<MSGID>"+msgId+"</MSGID>" +
                        "<POMSGID>"+pomsgId+"</POMSGID>" +
                        "<LANGUAGE>ZH</LANGUAGE>" +
                        "<SERVICENAME>111</SERVICENAME>" +
                    "</IS_SYSINFO>"+
                "</ROOT>";

        log.info("config:"+config);
        OMElement isInput = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes("UTF-8"))).getDocumentElement();
        OMElement result = doSapMethod(isInput, config,"urn");
        String rst = result.toString();

        String status = XmlUtil.subStringBetween(rst, "<MSGTY>", "</MSGTY>");
        String msg = XmlUtil.subStringBetween(rst, "<MSGTX>", "</MSGTX>");
        if(status.equalsIgnoreCase("E")){
            List<Map> errList = new ArrayList();
            Map errMap = new HashMap();
            errMap.put("txt",msg);
            errMap.put("flag","E");

            errList.add(errMap);

            Map errRstMap = new HashMap();
            errRstMap.put("rstList",errList);
            return errRstMap;
        }else if(status.equalsIgnoreCase("S")) {
            JSONObject rstJson = Xml2JsonUtil.xml2JSON(rst);
            log.info("转换后数据:" + rstJson);

            Map rspJs = (Map) rstJson.get("ZRFC_FI_OA006.Response");
            List busJsArr = (List) rspJs.get("ES_BUSINFO");

            Map dataMap = (Map) busJsArr.get(0);
            List dataList = (List) dataMap.get("DATA");

            Map itemMap = (Map) dataList.get(0);
            List<Map> itemList = (List) itemMap.get("item");

            List<Map> allDataList = new ArrayList();

            for (Map detailMap : itemList) {
                Map allDataMap = new HashMap();

                String zglmc = this.checkListNotNull(detailMap, "ZGLMC");
                String zjyxe = this.checkListNotNull(detailMap, "ZJYXE");
                String zjylx = this.checkListNotNull(detailMap, "ZJYLX");

                String zgjahr = this.checkListNotNull(detailMap, "GJAHR");
                String zrzje = this.checkListNotNull(detailMap, "ZRZJE");

                String zhsmc = this.checkListNotNull(detailMap, "ZHSMC");
                String zhtljje = this.checkListNotNull(detailMap, "ZHTLJJE");

                String zkyed = this.checkListNotNull(detailMap, "ZKYED");
                String zhtfps = this.checkListNotNull(detailMap, "ZHTFPS");

                String zhtje = this.checkListNotNull(detailMap, "ZHTJE");
                String zhsdm = this.checkListNotNull(detailMap, "ZHSDM");

                String zhtfpz = this.checkListNotNull(detailMap, "ZHTFPZ");
                String zhtfpy = this.checkListNotNull(detailMap, "ZHTFPY");

                String zgldm = this.checkListNotNull(detailMap, "ZGLDM");
                String zglht = this.checkListNotNull(detailMap, "ZGLHT");

                String zhtyxd = this.checkListNotNull(detailMap, "ZHTYXD");
                String zhtyxq = this.checkListNotNull(detailMap, "ZHTYXQ");

                String zjymx = this.checkListNotNull(detailMap, "ZJYMX");

                allDataMap.put("ZGLMC", zglmc);
                allDataMap.put("ZJYXE", zjyxe);
                allDataMap.put("ZJYLX", zjylx);
                allDataMap.put("GJAHR", zgjahr);
                allDataMap.put("ZRZJE", zrzje);
                allDataMap.put("ZHSMC", zhsmc);
                allDataMap.put("ZHTYXD", zhtyxd);
                allDataMap.put("ZHTLJJE", zhtljje);
                allDataMap.put("ZKYED", zkyed);
                allDataMap.put("ZHTFPS", zhtfps);
                allDataMap.put("ZHTJE", zhtje);
                allDataMap.put("ZHSDM", zhsdm);
                allDataMap.put("ZHTFPZ", zhtfpz);
                allDataMap.put("ZHTFPY", zhtfpy);
                allDataMap.put("ZGLDM", zgldm);
                allDataMap.put("ZGLHT", zglht);
                allDataMap.put("ZHTYXQ", zhtyxq);
                allDataMap.put("ZJYMX", zjymx);
                allDataMap.put("txt", "查询成功");
                allDataMap.put("flag", "S");
                allDataList.add(allDataMap);
            }

            Map rstMap = new HashMap();
            rstMap.put("rstList", allDataList);
            return rstMap;
        }else{
            List<Map> errList = new ArrayList();
            Map errMap = new HashMap();
            errMap.put("txt","查询异常，请检查输入条件");
            errMap.put("flag","E");

            errList.add(errMap);

            Map errRstMap = new HashMap();
            errRstMap.put("rstList",errList);
            return errRstMap;
        }
    }

    private String checkListNotNull(Map dataMap,String str){
        if(dataMap.get(str)!=null && dataMap.get(str) instanceof List){
            List list = (List) dataMap.get(str);
            String rst = list.get(0)+"";
            return rst;
        }
        return "";
    }
}
