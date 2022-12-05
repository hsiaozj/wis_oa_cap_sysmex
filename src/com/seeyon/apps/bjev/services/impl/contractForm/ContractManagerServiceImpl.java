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
        config.put("wsUrl", "http://120.133.56.147:8022/sapdev/XISOAPAdapter/MessageServlet?senderParty=&senderService=BS_OA&receiverParty=&receiverService=&interface=SI_OA006_OA_REQ&interfaceNamespace=http://www.bjev.com.cn/oa");
        config.put("wsFunction", "ZRFC_FI_EC002");
        config.put("wsNameSpace", "urn:sap-com:document:sap:rfc:functions");
        config.put("wsUserName", "oa2po");
        config.put("wsPassWord", "Aa123456");

        String xml = "<IS_BUSINFO>" +
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
                    "</IS_SYSINFO>";

        OMElement isInput = new StAXOMBuilder(new ByteArrayInputStream(xml.getBytes("UTF-8"))).getDocumentElement();
        OMElement result = doSapMethod(isInput, config,"urn");
        String rst = result.toString();

        String status = XmlUtil.subStringBetween(rst, "<ZSTATUS>", "</ZSTATUS>");
        String msg = XmlUtil.subStringBetween(rst, "<ZSTATUSTXT>", "</ZSTATUSTXT>");
        if(status.equalsIgnoreCase("E")){
            List<Map> errList = new ArrayList();
            Map errMap = new HashMap();
            errMap.put("txt",msg);

            errList.add(errMap);

            Map errRstMap = new HashMap();
            errRstMap.put("rstList",errList);
            return errRstMap;
        }
        JSONObject rstJson= Xml2JsonUtil.xml2JSON(rst);


        Map rspJs = (Map) rstJson.get("ZRFC_FI_EC002.Response");
        List busJsArr = (List) rspJs.get("ES_BUSINFO");

        Map dataMap = (Map) busJsArr.get(0);
        List dataList = (List) dataMap.get("DATA");

        Map itemMap = (Map) dataList.get(0);
        List<Map> itemList = (List) itemMap.get("item");

        List<Map> allDataList = new ArrayList();

        for(Map detailMap : itemList){
            Map allDataMap = new HashMap();

            List zglmcList = (List) detailMap.get("ZGLMC");
            String zglmc = (String) zglmcList.get(0);

            List zjyxeList = (List) detailMap.get("ZJYXE");
            String zjyxe = (String) zjyxeList.get(0);

            List zjylxList = (List) detailMap.get("ZJYLX");
            String zjylx = (String) zjylxList.get(0);

            List gjahrList = (List) detailMap.get("GJAHR");
            String zgjahr = (String) gjahrList.get(0);

            List zjymxList = (List) detailMap.get("ZJYMX");
            String zjymx = (String) zjymxList.get(0);

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

            List ztxtList = (List) detailMap.get("ZSTATUSTXT");
            String txt = (String) ztxtList.get(0);

            allDataMap.put("txt",txt);
            allDataMap.put("zglmc",zglmc);
            allDataMap.put("zjyxe",zjyxe);
            allDataMap.put("zjylx",zjylx);
            allDataMap.put("gjahr",zgjahr);
            allDataMap.put("zjymx",zjymx);
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

        Map rstMap = new HashMap();
        rstMap.put("rstList",allDataList);
        return rstMap;
    }

}
