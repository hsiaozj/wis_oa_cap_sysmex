package com.seeyon.apps.bjev.form;

import java.util.List;

import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCustomCtrl;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.form.util.Enums.FieldType;
import com.seeyon.cap4.form.util.Enums.ParamType;

public class BudgetForm extends FormFieldCustomCtrl{
	@Override
	public FieldType[] getFieldType() {
		
		return new Enums.FieldType[]{Enums.FieldType.DECIMAL};
	}

	@Override
	public String getFieldLength() {
		return "25";
	}

	@Override
	public String getMBInjectionInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPCInjectionInfo() {
		// TODO Auto-generated method stub
		return "{path:'apps_res/cap/customCtrlResources/budgetResources/',jsUri:'js/formBudgetRuning.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
	}

	@Override
	public void init() {
		this.setPluginId("budgetPlugin");
		this.setIcon("cap-icon-e-invoice");
		ParamDefinition eivoiceDef = new ParamDefinition();
		eivoiceDef.setDialogUrl("apps_res/cap/customCtrlResources/budgetResources/html/mapping.html");
        eivoiceDef.setDisplay("com.cap.ctrl.einvoice.paramtext");//如果要做国际化 这个地方只能存key
        eivoiceDef.setName("mapping");
        eivoiceDef.setParamType(ParamType.button);
        addDefinition(eivoiceDef);
	}

	@Override
	public boolean canBathUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getDefaultVal(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return "5407987654604875770";
	}

	@Override
	public List<String[]> getListShowDefaultVal(Integer arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return "可用预算查询";
	}
	
    @Override
    public boolean canUse() {
    	
    	return true;
    }
    
    @Override
    public boolean canUse(Enums.FormType formType) {
    	
    	return true;
    }
}
