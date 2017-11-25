<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePageNew.jsp" %>
<head>
    <title>已办列表</title>
    <%--<meta http-equiv="content-type" content="application/vnd.ms-excel;charset=UTF-8"/>--%>
    <link rel="stylesheet" type="text/css" href="<%=path%>/component/jquery-outFetterTable/jquery.outFetterTable.css"/>
    <style>

    </style>
</head>
<body>
<div id="todoListContainer"></div>
<script type="text/javascript" src="<%=path%>/component/jquery-outFetterTable/jquery.outFetterTable.js"></script>
<script type="text/javascript">

    var __settings = {
        renderTo: 'todoListContainer',
        loadURL: _PATH + '/workBaseController.do?method=getReady',
        extendRow: function (record) {
            return '<tr><td colspan="2" style="padding-top:0px;">' + record.jobTitle + '</a></td><td colspan="2" style="padding-top:0px;text-align:right">' + record.activityInstName + '</td></tr>';
        },
        columns: [{
            title: '工单类型',
            column: '',
            width: '120px',
            columnClass: 'text-center',
            wrapFunction: function (record, __data_value) {
                if (record.processModelName == "turnToDispatch") {
                    return '任务派发工单';
                } else {
                    return '流程管理工单';
                }
            }
//            highQuery: true,
//            highQueryType: 'range'
        },{
            title: '工单编号',
//            textAlign:"center",
            column: 'jobCode',
            columnClass: 'text-center',
            width: '20%',
            highQuery: true,
            highQueryType: 'lk'
        }, {
            title: '工单主题',
            column: 'jobTitle',
            wrapFunction: function (record, __data_value) {
                if (record.processModelName == "turnToDispatch" || record.processModelName == "turnToDispatchDuban") {
                    var path = '<%=path%>'.replace("UFP_MANAGE", "UFP_TASK");
                    return '<a style="text-decoration:underline" target="_blank" href="' + path + '/pageBuild.do?method=build&fromPage=already&type=already&buildMethod=build&' +
                            '&processInstID=' + record.processInstID +
                            '&processModelId=' + record.processModelId +
                            '&processModelName=' + record.processModelName +
                            '&activityInstID=' + record.activityInstID +
                            '&activityDefID=' + record.activityDefID +
                            '&taskInstID=' + record.taskInstID +
                            '&activityInstName=' + encodeURIComponent(encodeURIComponent(record.activityInstName)) +
                            '&jobTitle=' + encodeURIComponent(encodeURIComponent(record.jobTitle)) +
                            '&jobCode=' + encodeURIComponent(encodeURIComponent(record.jobCode)) +
                            '&jobID=' + record.jobID +
                            '&appID=' + record.appID +
                            '&shard=' + record.shard +
                            '&businessId=' + record.businessId +
                            '&rootProcessInstId=' + record.rootProcessInstId +
                            '&createDate=' + record.createDate +
                            '&taskWarning=' + record.taskWarning +
                            '&strColumn4=' + record.strColumn4 +
                            '&__returnUrl=/base/frame/already.jsp&globalUniqueID=' + _globalUniqueID +
                            '">' + __data_value + '</a>';
                } else if (record.jobID == undefined) {
                    var path = '<%=path%>'.replace("UFP_MANAGE", "bpms");
                    return '<a style="text-decoration:underline" target="_blank" href="' + path + '/workflow/view/task-done/redirect.html?taskId=' + record.taskInstID +'&editable=false'+
                            '&globalUniqueID=' + _globalUniqueID +
                            '">' + __data_value + '</a>';
                } else {
                    var path = '<%=path%>'.replace("UFP_TASK", "UFP_MANAGE");
                    return '<a style="text-decoration:underline" target="_blank" href="' + path + '/pageBuild.do?method=build&fromPage=already&type=already&buildMethod=build&' +
                            '&processInstID=' + record.processInstID +
                            '&processModelId=' + record.processModelId +
                            '&processModelName=' + record.processModelName +
                            '&activityInstID=' + record.activityInstID +
                            '&activityDefID=' + record.activityDefID +
                            '&taskInstID=' + record.taskInstID +
                            '&activityInstName=' + encodeURIComponent(encodeURIComponent(record.activityInstName)) +
                            '&jobTitle=' + encodeURIComponent(encodeURIComponent(record.jobTitle)) +
                            '&jobCode=' + encodeURIComponent(encodeURIComponent(record.jobCode)) +
                            '&jobID=' + record.jobID +
                            '&appID=' + record.appID +
                            '&shard=' + record.shard +
                            '&businessId=' + record.businessId +
                            '&rootProcessInstId=' + record.rootProcessInstId +
                            '&createDate=' + record.createDate +
                            '&taskWarning=' + record.taskWarning +
                            '&strColumn4=' + record.strColumn4 +
                            '&__returnUrl=/base/frame/already.jsp&globalUniqueID=' + _globalUniqueID +
                            '">' + __data_value + '</a>';
                }
            },
            highQuery: true,
            highQueryType: 'lk'
//        } , {
//            title : '专业',
//            column : 'strColumn1',
//            width : '20%',
//            textAlign:'center',
//            highQuery:true,
//            highQueryType:'lk'
        }, {
            title: '到达时间',
            column: 'createDate',
            width: '150px',
//            textAlign:'center',
            highQuery: true,
            highQueryType: 'range'},
            {
            title: '操作时间',
            column: 'completionDate',
            width: '150px',
//            textAlign:'center',
            highQuery: true,
            highQueryType: 'range'
//        } , {
//            title : '要求完成时间',
//            column : 'datColumn1',
//            width: '150px',
//            textAlign:'center',
//            highQuery:true,
//            highQueryType:'range'
        }],
        extQueryColumns: [{
            title: '工单主题',
            column: 'jobTitle',
            highQuery: true,
            highQueryType: 'lk',
            queryIndex: 1

        }, {
            title: '工单环节',
            column: 'activityInstName',
            highQuery: true,
            highQueryType: 'lk'

        }]
    }

    var outFetterTable = $.fn.outFetterTable.init({
        __settings: __settings
    });
</script>
</body>
</html>
