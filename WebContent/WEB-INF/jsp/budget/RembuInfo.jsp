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
	<script type="text/javascript" src="${path}/ajax.do?managerName=rembuManagerService"></script>
	<script text="text/javascript">
        var table;
        $(function () {
            table = $("#signInPlaceTable").ajaxgrid({				
                colModel: [{
                    display: 'id',
                    name: 'id',
                    width: '5%',
                    sortable: false,
                    align: 'center',
                    type: 'checkbox'
                }, {
                	display: '预算金额',
                    name: 'AMOUNT',
                    width: '95%',
                    sortable: true,
                    align: 'center'
                }],
                usepager: true,
                resizable:false,
                closeError : true,
                rpMaxSize :20,
                showToggleBtn:false,
                parentId: $('.layout_center').eq(0).attr('id'),
                managerMethod: "findInfoData",
                managerName: "rembuManagerService"
            });
            
            var queryParams={"compCode":'${compCode}',"year":'${year}',"id":'${id}'};
			$("#signInPlaceTable").ajaxgridLoad(queryParams);
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

    </script>
</head>
<body>
	<div id='layout' class="comp" comp="type:'layout'">
    <div class="layout_center" id="center" style="overflow:hidden;">
        <table class="flexme3" style="display: none" id="signInPlaceTable"></table>
    </div>
</div>
</body>
</html>