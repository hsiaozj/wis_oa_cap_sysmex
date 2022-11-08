(function () {
    var privated = 'field_5407987654604875770';
    var selfA8xHrm = {};

    selfA8xHrm.dynamicLoading = {
        css: function (path) {
            if (!path || path.length === 0) {
                throw new Error('argument "path" is required !');
            }
            var head = document.getElementsByTagName('head')[0];
            var link = document.createElement('link');
            link.href = path;
            link.rel = 'stylesheet';
            link.type = 'text/css';
            head.appendChild(link);
        },
        js: function (path) {
            if (!path || path.length === 0) {
                throw new Error('argument "path" is required !');
            }
            var head = document.getElementsByTagName('head')[0];
            var script = document.createElement('script');
            script.src = path;
            script.type = 'text/javascript';
            head.appendChild(script);
        }
    }

    selfA8xHrm.init = function (adaptation) {
        selfA8xHrm.dynamicLoading.css(adaptation.url_prefix + 'css/formQueryBtn.css');
        var messageObj = adaptation.adaptation.childrenGetData(adaptation.privateId);
        selfA8xHrm.appendChildDom(messageObj, adaptation.privateId, adaptation)
        selfA8xHrm.initData(messageObj.value, adaptation.privateId, messageObj.auth);

        // 监听是否数据刷新
        adaptation.adaptation.ObserverEvent.listen('Event' + adaptation.privateId, function () {
            messageObj = adaptation.adaptation.childrenGetData(adaptation.privateId);
            selfA8xHrm.appendChildDom(messageObj, adaptation.privateId, adaptation);
            selfA8xHrm.initData(messageObj.value, adaptation.privateId, messageObj.auth), messageObj.auth;
            //监听自定义控件展示内容是否改变
            document.querySelector('#custom' + adaptation.privateId).addEventListener('change', function () {
                selfA8xHrm.customCtrlsChangeListener(adaptation.privateId, messageObj, adaptation);
            })
        });

        //监听自定义控件展示内容是否改变
        //由于需求更改为禁用input,所以暂时注释掉该监听
        // document.querySelector('#custom'+adaptation.privateId).addEventListener('change', function () {
        //     selfA8xHrm.customCtrlsChangeListener(adaptation.privateId,messageObj,adaptation);
        // })
    };


    //自定义控件内容改变监听器方法
    selfA8xHrm.customCtrlsChangeListener = function (id, messageObj, adaptation) {
        if (!$("#custom" + id).val()) {
            messageObj.value = $("#custom" + id).val() + "|";
        } else {
            var arr = messageObj.value.split("|");
            messageObj.value = $("#custom" + id).val() + "|" + arr[1];
        }
        messageObj.showValue = $("#custom" + adaptation.privateId).val();
        adaptation.adaptation.childrenSetData(messageObj, id);
    }


    //回填表单mapping字段
    selfA8xHrm.backfillFormControlData = function (payload, adaptation) {
        adaptation.adaptation.backfillFormControlData(payload, adaptation.privateId);
    }

    //初始化自定义控件数据
    selfA8xHrm.initData = function (data, id, auth) {
        if (data && data != "***") {
            var arr = data.split("|");
            if (auth === 'browse') {
                document.querySelector("#input" + id).innerHTML = "<div>" + arr[0] + "<div>";
            } else if (auth === "edit") {
                document.querySelector("#custom" + id).value = arr[0];
            }
        }
    }

    selfA8xHrm.appendChildDom = function (messageObj, privateId, adaptation) {
        switch ('button') {
            case 'button':
                selfA8xHrm.customButton(messageObj, privateId, adaptation);
                break;
        }
    };
    
    selfA8xHrm.customButton = function (messageObj, privateId, adaptation) {
        if (messageObj.auth === 'hide') {
        	var dom_hide = '<section class="cap4-text is-one"><div class="cap4-text__left" id="text_left_'
				+ privateId
				+ '"></div><div class="cap4-text__right">'
				+ '<div class="cap4-text__browse">***</div>'
				+ '</div></section>';
            document.querySelector('#' + privateId).innerHTML = dom_hide;
            return;
        }
    
        var paddingrigth = 0;
        if (messageObj.auth === 'edit') {
            paddingrigth = '40px';
        } else if (messageObj.auth === 'browse') {
            paddingrigth = '16px';
        }
        var domStructure = '<section id="section_' + privateId
		+ '" class="cap4-text is-one"><div id="text_left_' + privateId
		+ '"></div><div ><div id="cap4-text__' + privateId
		+ '" class="cap4-text__cnt">' + '<div id="input' + privateId
		+ '" style="padding-right:' + paddingrigth
		+ '"><input readonly="true"   type="text" id="custom'
		+ privateId + '"/></input></div>'
		+ '<div class="cap4-date__picker"><i id="search' + privateId
		+ '" class="icon CAP cap-icon-sousuo ' + privateId
		+ '"></i>&nbsp;</div></div></div></section>';
        
        var creditDom = document.querySelector('#' + privateId);
        if (creditDom == null || creditDom == undefined) {
            return;
        }
        creditDom.innerHTML = domStructure;

        //设置必填样式
        if (messageObj.isNotNull == "1" && !messageObj.value) {
            document.getElementById("section_" + privateId).setAttribute("class", "cap4-text is-one is-must");
        }
        
        // 点击弹出新窗体
        document.querySelector('.' + privateId).addEventListener('click', function() {
        	console.log(messageObj);
			// 预提交数据
        	adaptation.adaptation.callTakeFormSave({ type : 'save', mainbodyArgs : { needCheckRule : '0', needDataUnique : '0', needSn : '0' }, isPrev : true, callback : function() {}, successFn : function() {
        		var content = messageObj.formdata.content;
            	var fieldId=messageObj.id;
            	var formId=content.contentTemplateId;
            	var formRecordId=content.contentDataId;
            	var subRecordId=messageObj.recordId;
            	
            	//明细表的名称
            	var subName = messageObj.formdata.formdata.display;
				 current_dialog = $.dialog({
		                url:'/seeyon/signinbudget/signinbudget.do?method=getBudgetPage&fieldId='+fieldId+'&formId='+formId+'&formRecordId='+formRecordId+"&subRecordId="+subRecordId+"&subName="+encodeURIComponent(subName),
		                title: "可用预算信息",
		                width: 1000,
		                height: 500,
		                targetWindow: getCtpTop(),
		                buttons: [{
		                    text: $.i18n('common.button.ok.label'),
		                    id: "sure",
		                    isEmphasize: true,
		                    handler: function () {
		                        var result = current_dialog.getReturnValue();
		                        var content = messageObj.formdata.content;
		                        $.ajax({
		                            type: 'get',
		                            url: _ctxPath + "/rest/cap4/formSapBudget/parseAndFillBackSap",
		                            dataType: 'JSON',
		                            contentType: 'application/json;charset=UTF-8',
		                            data: {
		                                'name': result.key,
		                                'fieldId':fieldId,
		                                'formId': content.contentTemplateId,
		                                'fieldName': messageObj.id,
		                                'masterId': content.contentDataId,
		                                'subId': messageObj.recordId,
		                                'data':JSON.stringify(result)
		                            },
		                            success: function (data) {
		                                //回填mapping数据
		                                var backfill = {};
		                                //将自定义控件值设置到adaptation中
		                                messageObj.value = result.code;
		                                messageObj.showValue = result.code;
		                                adaptation.adaptation.childrenSetData(messageObj, adaptation.privateId);

		                                backfill.tableName = adaptation.formMessage.tableName;
		                                backfill.tableCategory = adaptation.formMessage.tableCategory;
		                                backfill.updateData = data.data;
		                                backfill.updateRecordId = messageObj.recordId;
		                                selfA8xHrm.backfillFormControlData(backfill, adaptation);
		                                
		                                console.log("backfill"+backfill.tableName+"="+backfill.tableCategory+"="+backfill.updateData);
		                                console.log("messageObj"+messageObj.value+"="+messageObj.showValue);
		                    
		                                current_dialog.close();
		                            },
		                            error: function (data) {
		                                $.error(JSON.parse(data.responseText).message);
		                            }
		                        });
		                    }
		                }, {
		                    text: $.i18n('common.button.cancel.label'),
		                    id: "exit",
		                    handler: function () {
		                        current_dialog.close();
		                    }
		                }]
		            });
			}, errorFn : function() {
				top.$.error("错误");
			} }, privateId);
		});
		
       
        if (messageObj.auth === 'browse') {
            var d1 = document.getElementById("search" + privateId);
            d1.parentNode.removeChild(d1);
            var d2 = document.getElementById("cap4-text__" + privateId);
            d2.setAttribute("class", "cap4-text__browse");
            var d3 = document.getElementById("custom" + privateId);
            d3.parentNode.removeChild(d3);
        }
    }
    window[privated] = selfA8xHrm;
})();