package com.seeyon.apps.bjev.form;

import com.seeyon.cap4.form.bean.ParamDefinition;
import com.seeyon.cap4.form.bean.fieldCtrl.FormFieldCustomCtrl;
import com.seeyon.cap4.form.util.Enums;

import java.util.List;

public class ContractForm extends FormFieldCustomCtrl {
    @Override
    public Enums.FieldType[] getFieldType() {

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
        return "{path:'apps_res/cap/customCtrlResources/contractResources/',jsUri:'js/formContractRuning.js',initMethod:'init',nameSpace:'field_" + this.getKey() + "'}";
    }

    @Override
    public void init() {
        this.setPluginId("contractPlugin");
        this.setIcon("cap-icon-e-invoice");
        ParamDefinition eivoiceDef = new ParamDefinition();
        eivoiceDef.setDialogUrl("apps_res/cap/customCtrlResources/contractResources/html/mapping.html");
        eivoiceDef.setDisplay("com.cap.ctrl.einvoice.paramtext");//如果要做国际化 这个地方只能存key
        eivoiceDef.setName("mapping");
        eivoiceDef.setParamType(Enums.ParamType.button);
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
        return "9214387654604875770";
    }

    @Override
    public List<String[]> getListShowDefaultVal(Integer arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getText() {
        // TODO Auto-generated method stub
        return "合同金额查询";
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
