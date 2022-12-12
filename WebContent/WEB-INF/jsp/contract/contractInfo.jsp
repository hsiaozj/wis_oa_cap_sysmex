<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/common/common.jsp" %>

<html>
<head>
	<title>可用预算信息</title>
	<style type="text/css">
	        input::-webkit-input-placeholder { color: #d4d4d4; }
	        input:-ms-input-placeholder { color: #d4d4d4; }
	        #key{ border-radius: 14px 0 0 14px; }
	        #key:focus{ border: 1px solid #ccc; }
	</style>
	<script type="text/javascript" src="${path}/ajax.do?managerName=contractManagerService"></script>
	<script text="text/javascript">
        var table;
        $(function () {
            table = $("#signInPlaceTable").ajaxgrid({				
                colModel: [{
                    display: 'id',
                    name: 'id',
                    width: '2%',
                    sortable: false,
                    align: 'center',
                    type: 'checkbox'
                }, {
                	display: '合同号',
                    name: 'ZGLHT',
                    width: '10%',
                    sortable: true,
                    align: 'center'
                }, {
					display: '关联方名称',
					name: 'ZGLMC',
					width: '10%',
					sortable: true,
					align: 'center'
				}, {
					display: '核算单位名称',
					name: 'ZHSMC',
					width: '10%',
					sortable: true,
					align: 'center'
				}, {
					display: '年度',
					name: 'GJAHR',
					width: '10%',
					sortable: true,
					align: 'center'
				},{
					display: '交易类型',
					name: 'ZJYLX',
					width: '10%',
					sortable: true,
					align: 'center'
				}, {
					display: '合同总额',
					name: 'ZHTJE',
					width: '10%',
					sortable: true,
					align: 'center'
				},{
					display: '年度预算额度',
					name: 'ZJYXE',
					width: '10%',
					sortable: true,
					align: 'center'
				},  {
					display: '实际入账金额',
					name: 'ZRZJE',
					width: '10%',
					sortable: true,
					align: 'center'
				}, {
					display: '合同累计执行金额',
					name: 'ZHTLJJE',
					width: '10%',
					sortable: true,
					align: 'center'
				}, {
					display: '新签合同可用额度',
					name: 'ZKYED',
					width: '10%',
					sortable: true,
					align: 'center'
				}, {
					display: '当年合同分配额剩余可用',
					name: 'ZHTFPS',
					width: '10%',
					sortable: true,
					align: 'center'
				}, {
					display: '合同开始日期',
					name: 'ZHTYXQ',
					width: '10%',
					sortable: true,
					align: 'center'
				}, {
					display: '合同结束日期',
					name: 'ZHTYXD',
					width: '10%',
					sortable: true,
					align: 'center'
				}, {
					display: '当年合同分配额已占用',
					name: 'ZHTFPY',
					width: '10%',
					sortable: true,
					align: 'center'
				},{
					display: '当年合同分配额总额',
					name: 'ZHTFPZ',
					width: '10%',
					sortable: true,
					align: 'center'
				}, {
					display: '状态信息',
					name: 'txt',
					width: '10%',
					sortable: true,
					align: 'center'
				}],
                usepager: true,
                resizable:false,
                closeError : true,
                rpMaxSize :20,
                showToggleBtn:false,
				height:718,
                managerMethod: "findInfoData",
                managerName: "contractManagerService"
            });
            
            var queryParams={"hsmc":'${hsmc}',"jylx":'${jylx}',"glmc":'${glmc}',"gjahr":'${gjahr}',"glht":'${glht}'};
			$("#signInPlaceTable").ajaxgridLoad(queryParams);

			document.getElementById("ht").value='${glht}';
        });       

        //确定按钮回调，将选择的数据返回
        function OK() {
            var arr = table.grid.getSelectRows();
            if (arr.length > 1) {
                $.error("请选择一条数据");
                return;
            }
            if (arr.length == 0) {
                $.error("请选择一条数据");
                return;
            }
            console.log({data: arr[0]});
            return {data: arr[0]};
        }

		function ss(){
			var htVal = $('#ht').val();
			var queryParams={"hsmc":'${hsmc}',"jylx":'${jylx}',"glmc":'${glmc}',"gjahr":'${gjahr}',"glht":htVal};
			console.log("查询值:"+'${hsmc}'+"=="+'${jylx}'+"=="+'${glmc}'+"=="+'${gjahr}'+"=="+htVal);
			$("#signInPlaceTable").ajaxgridLoad(queryParams);
		}

    </script>
</head>
<body>
	<sapn>合同号</sapn>
	<input type="text" value="" id="ht"/>
	<a href="javascript:void(0)" class="common_button common_button_emphasize radius3px" onclick="ss()">确定</a>

	<div id='layout' class="comp" comp="type:'layout'">
    <div class="layout_center" id="center" style="overflow:hidden;">
        <table class="flexme3" style="display: none" id="signInPlaceTable"></table>
    </div>
</div>
</body>
</html>