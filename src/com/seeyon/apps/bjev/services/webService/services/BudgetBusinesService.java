package com.seeyon.apps.bjev.services.webService.services;

import com.seeyon.apps.bjev.services.pojo.PbInput;
import com.seeyon.apps.bjev.services.pojo.WisBaseOutput;

public interface BudgetBusinesService { //预算业务处理

    public WisBaseOutput procBackBudgetData(PbInput input) throws Exception;

}
