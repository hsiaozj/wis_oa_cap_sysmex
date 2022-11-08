package com.seeyon.apps.bjev.services.webService.services;

import com.seeyon.apps.bjev.services.pojo.Mdata;
import com.seeyon.apps.bjev.services.pojo.MdataOutput;

public interface MdataService {
	
	//webService接口处理
	public MdataOutput syncMdata(Mdata input) throws Exception;
}
