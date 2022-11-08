package com.seeyon.apps.bjev.services.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import com.seeyon.apps.bjev.common.ClientResource;
import com.seeyon.apps.bjev.services.FormServices;
import com.seeyon.cap4.form.modules.engin.base.formData.CAP4FormDataDAO;
import com.seeyon.client.CTPRestClient;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.util.UUIDLong;

public class FormServicesImpl implements FormServices{
	private static final Logger log = Logger.getLogger(FormServicesImpl.class);
	
	private CAP4FormDataDAO cap4FormDataDAO = (CAP4FormDataDAO)AppContext.getBean("cap4FormDataDAO");
	
	@Override
	public void pushMessage(String message, String loginName, String registerCode) throws Exception {
		CTPRestClient client = ClientResource.getInstance().resouresClent();
		Map map = new HashMap();
		map.put("thirdpartyMessageId", UUIDLong.longUUID()+"");
		map.put("thirdpartyRegisterCode", registerCode);
		map.put("messageContent", message);
		map.put("thirdpartyReceiverId", UUIDLong.longUUID()+"");
		map.put("creation_date", DateFormatUtils.format(new Date(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss"));
		map.put("messageType", "4");
		map.put("downloadUrl", "");
		map.put("messageURL", "");
		map.put("messageH5URL", "");
		map.put("noneBindingSender", loginName);
		map.put("noneBindingReceiver", loginName);
		String result = client.post("thirdpartyMessage/receive/singleMessage", map,String.class);
		log.info("registerCode:"+registerCode+",loginName:"+loginName+",message:"+message+",result:"+result);
	}
	
	@Override
	public List<Map> getValueByMap(String[] returnFields, Map<String, Object> hm, String tableName, String fieldsLogic)
			throws Exception {
		return cap4FormDataDAO.getValueByMap(returnFields, hm, tableName, fieldsLogic);
	}
	
	@Override
	public List<Map> selectDataBySql(String sql) throws Exception {
		return cap4FormDataDAO.selectDataBySql(sql);
	}

}
