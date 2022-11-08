var einvoiceFieldInfo = [];
var formFieldInfo = [];

//设置模版的分隔符
_.templateSettings = {
    evaluate: /\{%([\s\S]+?)%\}/g,
    interpolate: /\{\{([\s\S]+?)\}\}/g,
    escape: /\{\{-([\s\S]+?)\}\}/g
};
var initObj;
$(document).ready(function(){
    initObj = initParam();
    getEinvoiceFieldInfo();
    $("#einvoice").change(function () {
        var fieldType = $(this).find("option:selected").attr("fieldtype");
        getFormFields(fieldType);
    });


    $("#systemMappingArea").on('click', '.cap-icon-zengjia', function(event) {
        var render = _.template($("#template-einvoice_relation").html());
        var html1 = render({ sourveValue: "",targetValue:"", fieldList: einvoiceFieldInfo, formFieldList:formFieldInfo});
        var htmDom = $(html1.trim());
        $(this).parent().after(htmDom);
    });

    $("#systemMappingArea").on('click', '.cap-icon-jianshao', function(event) {
        if($(".biz_groupguanlian").length>1){
            $(this).parent().remove();
        }else{
            alert("已经是最后一条，不能再删了！");
            var currentRow = $(this).parent();
            currentRow.find("#einvoice").val("");
            currentRow.find("#formfield").val("");
            currentRow.find("#formfield").empty();
            currentRow.find("#formfield").append("<option></option>");
        }
    });

});


function getEinvoiceFieldInfo() {
    var params = initParam();
    var formId = params.formBaseInfo.formBaseInfo.formInfo.id;
    $.ajax({ url: "/seeyon/rest/cap4/formSapRembu/getRembuFieldInfo?formid=" + formId+ "&fieldType=" + ""+"&currentField="+params.currentfield.name,
        async:true,
        success: function(data){
            var fields = data.data.RembuMap;
            var formFields = data.data.formFieldMap;
            for (var key in fields){
                var einvoiceInfo = {};
                einvoiceInfo.fieldname = key;
                einvoiceInfo.fieldvalue = fields[key];
                einvoiceFieldInfo = einvoiceFieldInfo.concat(einvoiceInfo);
            }
            for (var key in formFields) {
                var field = {};
                field.fieldname = formFields[key].name;
                field.display = formFields[key].display;
                field.ownertablename = formFields[key].ownerTableName;
                formFieldInfo = formFieldInfo.concat(field);
            }
            //如果之前已经设置，重新打开设置窗口的时候要显示出之前已经设置的映射
            if(initObj.currentfield&&initObj.currentfield.customParam&&$.parseJSON(initObj.currentfield.customParam).mapping&&$.parseJSON(initObj.currentfield.customParam).mapping.length>0){
                var oldMapping = $.parseJSON(initObj.currentfield.customParam).mapping;
                var systemMappingArea = $("#systemMappingArea");
                systemMappingArea.empty();
                for(var i=0;i<oldMapping.length;i++){
                    var it = oldMapping[i];
                    var source = it.source;
                    var target = it.target;
                    var render = _.template($("#template-einvoice_relation").html());
                    var html1 = render({ sourveValue: source,targetValue:target, fieldList: einvoiceFieldInfo, formFieldList:formFieldInfo});
                    systemMappingArea.append($(html1.trim()));
                }
            }else{
                var options = "";
                for (var key in fields){
                    options += "<option fieldtype='" + key +"'>" + fields[key] + "</option>";
                }
                $("#einvoice").append(options);
            }
        }
    });
}

function getFormFields(fieldType){
    var params = initParam();
    var formId = params.formBaseInfo.formBaseInfo.formInfo.id;
    $.ajax({ url: "/seeyon/rest/cap4/formSapRembu/getSapFields?formid=" + formId + "&fieldType=" + fieldType+"&currentField="+params.currentfield.name,
        async:true,
        success: function(data){
            var fields = data.data;
            var options = "<option></option>";
            formFieldInfo=[];
            for (var key in fields){
                options += "<option fieldname='"+fields[key].name+"' ownertablename='" + fields[key].ownerTableName + "'>" + fields[key].display + "</option>";
                var field = {};
                field.fieldname = fields[key].name;
                field.display = fields[key].display;
                field.ownertablename = fields[key].ownerTableName;
                formFieldInfo = formFieldInfo.concat(field);
            }
            $("#formfield").empty();
            $("#formfield").append(options);
        }
    });
}

function initParam(){
    var obj= window.parentDialogObj && (window.parentDialogObj["ctrlDialog"]);//获取窗口对象
    if(obj && obj.getTransParams){
        //然后通过V5方法获取弹窗传递过来的参数
        return obj.getTransParams();
    }
}


//确定按钮调用方法，返回需要的json数据
function OK(){ 
    var mapping = [];
    var settings = $("#systemMappingArea").find(".biz_groupguanlian");
    var checkTag = true;
    for(var i=0;i<settings.length;i++){
        var setting = $(settings[i]);
        var temp = {};
        var einvoicefield = setting.find(".leftField").find("option:selected").attr("fieldtype");
        var fieldname = setting.find(".rightField").find("option:selected").attr("fieldname");
        if(einvoicefield===undefined||fieldname===undefined){
            top.$.alert("选项不能为空！");
            checkTag = false;
            break;
        }
        temp.source = einvoicefield;
        temp.target = fieldname;
        mapping.push(temp);
    }
    
    if(checkTag){
        var hasSame = false;
        var sameFieldName = "";
        //校验所选是否满足规则
        //先校验右侧同一个数据域是否出现多次
        for(var j=0;j<mapping.length;j++){
            var current1 = mapping[j];
            for(var k=0;k<mapping.length;k++){
                var current2 = mapping[k];
                if(current1.source!=current2.source && current1.target===current2.target){
                    hasSame = true;
                    sameFieldName = current1.target;
                    break;
                }
            }
            if(hasSame){
                break;
            }
        }
        //再校验左右是否类型匹配
        if(hasSame){
            checkTag = false;
            var sameField = getFieldInfoByCurrentField(initObj.currentfield.name,sameFieldName);
            top.$.alert(sameField.display+"[右侧数据域]出现多次!");
        }else{
            var errorMsg = "";
            var formId = initObj.formBaseInfo.formBaseInfo.formInfo.id;
            $.ajax({ url: "/seeyon/rest/cap4/formSapRembu/checkSapMapping",
                async:false,
                type: 'POST',
                dataType:'json',
                contentType : 'application/json;charset=UTF-8',
                data: JSON.stringify({"formId":formId,"datas":mapping}),
                success: function(data){
                    if(data.data.result=="true"){
                        checkTag = true;
                    }else{
                        checkTag = false;
                        errorMsg = data.data.errorMsg;
                    }
                }
            });
            if(!checkTag){
                top.$.alert(errorMsg);
            }
        }
    }
    return  {valid:checkTag,data:mapping};
}

/**
 * 通过当前自定义控件和另外一个字段的名称获取另外一个字段的定义信息
 * @param currentFname
 * @param fieldName
 * @returns {*}
 */
function getFieldInfoByCurrentField(currentFname,fieldName){
    var masterFields = initObj.formBaseInfo.formBaseInfo.tableInfo.front_formmain.fieldInfo;
    for(var i=0;i<masterFields.length;i++){
        if(masterFields[i].name===currentFname){
            for(var j=0;j<masterFields.length;j++){
                if(masterFields[j].name===fieldName){
                    return masterFields[j];
                }
            }
            break;
        }
    }
    var subTables = initObj.formBaseInfo.formBaseInfo.tableInfo.formsons;
    if(subTables!=undefined){
        for(var i=0;i<subTables.length;i++){
            var subTableFields = subTables[i].fieldInfo;
            for(var j=0;j<subTableFields.length;j++){
                if(subTableFields[j].name===currentFname){
                    for(var k=0;k<subTableFields.length;k++){
                        if(subTableFields[k].name===fieldName){
                            return subTableFields[k];
                        }
                    }
                }
            }

        }
    }
}