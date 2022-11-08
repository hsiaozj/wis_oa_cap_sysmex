package com.seeyon.apps.bjev.services.webService;

import com.seeyon.apps.bjev.services.pojo.Mdata;
import com.seeyon.apps.bjev.services.pojo.PbInput;
import com.seeyon.apps.bjev.services.pojo.WbackOutput;
import com.seeyon.apps.bjev.services.pojo.WisBaseOutput;

public interface WebServices {

	public WisBaseOutput syncMdata(Mdata input);

	public WbackOutput procBudgetBusiness(PbInput input);
	
}
