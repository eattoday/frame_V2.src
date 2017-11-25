<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/base/basePageNew.jsp" %>
<head>
    <title>待办列表</title>
    <%--<meta http-equiv="content-type" content="application/vnd.ms-excel;charset=UTF-8"/>--%>
    <link rel="stylesheet" type="text/css" href="<%=path%>/component/jquery-outFetterTable/jquery.outFetterTable.css"/>
    <style>

    </style>
</head>
<body>
<div id="todoListContainer"></div>
<script type="text/javascript" src="<%=path%>/component/jquery-outFetterTable/jquery.outFetterTable.js"></script>
<script type="text/javascript">
    $(document).ready(function () {
        layui.use(['layer'], function () {
        })
    });

    var __settings = {
        renderTo: 'todoListContainer',
        loadURL: _PATH + '/workBaseController.do?method=queryTodo',
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
//            textAlign: 'center',
            columnClass: 'text-center',
            column: 'jobTitle',
            wrapFunction: function (record, __data_value) {
                if (record.processModelName == "turnToDispatch" || record.processModelName == "turnToDispatchDuban") {
                    var path = '<%=path%>'.replace("UFP_MANAGE", "UFP_TASK");
                    return '<a style="text-decoration:underline" target="_blank" href="' + path + '/pageBuild.do?method=build&fromPage=todo&type=waiting&buildMethod=build&' +
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
                            '&__returnUrl=/base/frame/todo.jsp&globalUniqueID=' + _globalUniqueID +
                            '">' + __data_value + '</a>';
                } else if (record.jobID == undefined) {
                    var path = '<%=path%>'.replace("UFP_MANAGE", "bpms");
                    return '<a style="text-decoration:underline" target="_blank" href="' + path + '/workflow/view/task-todo/redirect.html?taskId=' + record.taskInstID +
                            '&globalUniqueID=' + _globalUniqueID +
                            '">' + __data_value + '</a>';
                } else {
                    var path = '<%=path%>'.replace("UFP_TASK", "UFP_MANAGE");
                    return '<a style="text-decoration:underline" target="_blank" href="' + path + '/pageBuild.do?method=build&fromPage=todo&type=waiting&buildMethod=build&' +
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
                            '&__returnUrl=/base/frame/todo.jsp&globalUniqueID=' + _globalUniqueID +
                            '">' + __data_value + '</a>';
                }
                <%--return '<a style="text-decoration:underline" target="_blank" href="<%=path%>/pageBuild.do?method=build&fromPage=todo&type=waiting&buildMethod=build&' +--%>
                <%--'&processInstID=' + record.processInstID +--%>
                <%--'&processModelId=' + record.processModelId +--%>
                <%--'&processModelName=' + record.processModelName +--%>
                <%--'&activityInstID=' + record.activityInstID +--%>
                <%--'&activityDefID=' + record.activityDefID +--%>
                <%--'&taskInstID=' + record.taskInstID +--%>
                <%--'&activityInstName=' + encodeURIComponent(encodeURIComponent(record.activityInstName)) +--%>
                <%--'&jobTitle=' + encodeURIComponent(encodeURIComponent(record.jobTitle)) +--%>
                <%--'&jobCode=' + encodeURIComponent(encodeURIComponent(record.jobCode)) +--%>
                <%--'&jobID=' + record.jobID +--%>
                <%--'&appID=' + record.appID +--%>
                <%--'&shard=' + record.shard +--%>
                <%--'&businessId=' + record.businessId +--%>
                <%--'&rootProcessInstId=' + record.rootProcessInstId +--%>
                <%--'&createDate=' + record.createDate +--%>
                <%--'&taskWarning=' + record.taskWarning +--%>
                <%--'&strColumn4=' + record.strColumn4 +--%>
                <%--'&__returnUrl=/base/frame/todo.jsp' +--%>
                <%--'">' + __data_value + '</a>';--%>
            },
            highQuery: true,
            highQueryType: 'lk'

//        } , {
//            title : '专业',
//            column : 'strColumn1',
//            width : '20%',
//            textAlign:'center',
//            highQuery:true,
//            highQueryType:'enum',
//            enums:[{label:'数据网' , value:'数据网'}]
        }, {
            title: '当前环节',
            column: 'activityInstName',
            columnClass: 'text-center',
            width: '200px',
            highQuery: true,
            highQueryType: 'lk',
            wrapFunction: function (record, __data_value) {
                if (record.processModelName == "turnToDispatch" && __data_value == "上级反馈审核") {
                    return __data_value + '(' + record.strColumn7 + '/' + record.strColumn6 + ')';
                }
                return __data_value
            }
        }, {
            title: '到达时间',
            column: 'createDate',
            width: '150px',
            columnClass: 'text-center',
            highQuery: true,
            highQueryType: 'range'
        }
//            {
//            title: '要求完成时间',
//            column: 'datColumn1',
//            width: '150px',
//            columnClass: 'text-center',
//            highQuery: true,
//            highQueryType: 'range'
//        },
            ]
    }

    var outFetterTable = $.fn.outFetterTable.init({
        __settings: __settings
    });

    function refresh_out_fetter_data() {
//        outFetterTable.refresh();
        window.location.reload();
    }
    //    outFetterTable.load();


    var __process_bar_thread = null;

    var __process_bar_container = $('<div class="progress progress-striped active"></div>');
    var __process_bar = $('<div class="progress-bar" role="progressbar" aria-valuenow="1" aria-valuemin="0" aria-valuemax="100"></div>');
    __process_bar_container.append(__process_bar);

    var __header_container = $('<div style="padding:8px 8px 0 8px;width:100%;background-color: #fff"></div>');
    //    var __refresh_btn = $('<span onclick="__refresh_out_fetter_data()" class="btn btn-default float-left" style="line-height:30px;padding-top:0;padding-bottom:0;">刷新<img src="' + _PATH + '/base/_resources/refresh.png"/></span>');
    //    var __total_count_label = $('<span class="float-left" style="line-height:28px;margin-left:10px;margin-right:30px;">共<span id="__total_count_value" style="font-weight: bold">0</span>条</span>');
    //    var __quick_search_input = $('<div class="float-left" style="padding:0 8px;border-top-left-radius:5px;border-bottom-left-radius:5px;border-left:1px solid #d0d0d0;border-top:1px solid #d0d0d0;border-bottom:1px solid #d0d0d0;"><img src="' + _PATH + '/base/_resources/search.png"/><input id="__quick_search_input" placeholder="快速检索"/></div>');
    //    var __quick_search_btn = $('<div id="__quick_search_btn" class="float-left btn btn-default" onclick="__init_out_fetter_data()">搜索</div>');
    //    var __detail_search_btn = $('<span id="__detail_search_btn" class="btn btn-default float-right" style="line-height:30px;padding-top:0;padding-bottom:0;">高级查询</span>');

    //    __header_container.append(__refresh_btn).append(__total_count_label).append(__quick_search_input).append(__quick_search_btn).append(__detail_search_btn).append($('<div style="clear:both;"></div>')).append(__process_bar_container);
    __header_container.append($('<div style="clear:both;"></div>')).append(__process_bar_container);

    var __data_title_table = $('<table  class="out-fetter-header-table" style="margin-top: 5px;"></table>');
    var __data_title_tbody = $('<tbody></tbody>');
    var __data_title_tr = $('<tr></tr>');
    for (var x = 0; x < __settings.columns.length; x++) {
        var __data_title_td = $('<th></th>');
        var __data_title_column = __settings.columns[x];
        var __data_title_value = __data_title_column.title;
        __data_title_td.append(__data_title_value);
        __data_title_tr.append(__data_title_td);
        if (__data_title_column.width) {
            __data_title_td.width(__data_title_column.width);
        }
    }
    __data_title_tbody.append(__data_title_tr);
    __data_title_table.append(__data_title_tbody);
    __header_container.append(__data_title_table);


    var __data_index_value = 0;
    var __curr_data_page_value = 0;
    var __totalCount = 0;
    var __page_size = 10;
    var __foot_bar_container = '';
    var __go_top_img = $('<img class="float-right" style="cursor:pointer;" onclick="__go_top()" src="' + _PATH + '/base/_resources/go-top.png"/>');

    var __foot_blank_div = $('<div style="height:45px;"></div>');

    var dtGridPager = {pageSize: __page_size, highQueryParameters: {}};

    //    __init_out_fetter_data();

    var __goto_scroll_resp = null;

    $(window).scroll(function () {
        if (__goto_scroll_resp == null) {
            __goto_scroll_resp = setTimeout(function () {
                if ($(window).scrollTop() > 50) {
                    __header_container.css({position: 'fixed'});
                } else if ($(window).scrollTop() < 50) {
                    __header_container.css({position: ''});
                }
                if (($(window).scrollTop()) >= ($(document).height() - $(window).height()) && __data_index_value != __totalCount) {
                    __load_out_fetter_data(function () {
                        __goto_scroll_resp = null;
                    });
                } else {
                    __goto_scroll_resp = null;
                }
            }, 100)
        }
    });

    function __load_out_fetter_data(__callback, __query_mode) {
        dtGridPager.startRecord = __curr_data_page_value++ * __page_size;
        dtGridPager.fastQueryKeyWord = $('#__quick_search_input').val();
        dtGridPager.highQueryParameters.lk_jobCode = '';
        dtGridPager.highQueryParameters.lk_jobTitle = '';
        dtGridPager.highQueryParameters.ge_datColumn1 = '';
        dtGridPager.highQueryParameters.le_datColumn1 = '';
        dtGridPager.highQueryParameters.ge_createDate = '';
        dtGridPager.highQueryParameters.le_createDate = '';
        $.ajax({
            url: __settings.loadURL,
            data: {dtGridPager: JsonObjectToString(dtGridPager)},
            contentType: "application/x-www-form-urlencoded; charset=utf-8",
            method: 'POST',
            async: true,
            dataType: 'json',
            success: function (response) {
                if (response) {
                    var __dataList = response.exhibitDatas;
                    __totalCount = response.recordCount;
                    $('#__total_count_value').text(__totalCount);
                    var __data_table_container = $('<div style="padding:0 8px;"></div>');

                    for (var i = 0; i < __dataList.length; i++) {
                        var __data_table = $('<table class="out-fetter-table"></table>');
                        var __data_table_tbody = $('<tbody></tbody>');
                        var __data = __dataList[i];
                        var __data_tr = $('<tr></tr>');
                        for (var j = 0; j < __settings.columns.length; j++) {
                            var __data_td = $('<td></td>');
                            var __data_column = __settings.columns[j];
                            var __data_value = __data[__data_column.column];
                            if (__data_column.wrapFunction) {
                                __data_value = __data_column.wrapFunction(__data, __data_value);
                            }
                            if (__data_column.width) {
                                __data_td.width(__data_column.width);
                            }
                            if (__data_column.textAlign) {
                                __data_td.css({'text-align': __data_column.textAlign});
                            }
                            __data_td.append(__data_value);
                            __data_tr.append(__data_td);
                        }
                        __data_table_tbody.append(__data_tr);
//                        if (__settings.extendRow) {
//                            __data_table_tbody.append($(__settings.extendRow(__data)));
//                        }
                        __data_table.append(__data_table_tbody);
                        __data_table_container.append(__data_table);
                        if (__data_index_value++ % 2 == 1) {
                            __data_table.addClass('out-fetter-table-odd');
                        }
                    }
                    $('#' + __settings.renderTo).append(__data_table_container).append(__foot_blank_div);
                    if (__data_index_value == __totalCount) {
                        __foot_bar_container.text(__data_index_value + "条全部加载完成").append(__go_top_img);
                    } else {
                        $('#__loaded_count_value').text(__data_index_value);
                    }


                    if (__callback) {
                        __callback();
                    }
                }
            }
        })
    }

    function __init_out_fetter_data() {
        __show_process_bar();
        __foot_bar_container = $('<div class="out-fetter-footer" style="position:fixed;padding:8px 8px 0 8px;bottom:0;background-color:#fff;width:100%;"><a style="color:#333!important;" onclick="__load_out_fetter_data()">已加载<span id="__loaded_count_value">0</span>条，点击或滚动继续加载</a></div>');
        __foot_bar_container.append(__go_top_img);
        $('#__loaded_count_value').text(0);
        $('#' + __settings.renderTo).empty().append(__header_container).append(__foot_bar_container);
        __data_index_value = 0;
        __curr_data_page_value = 0;
        __totalCount = 0;
        __load_out_fetter_data(function () {
            __go_top();
            __hide_process_bar();
        });
    }

    function __refresh_out_fetter_data() {
        __init_out_fetter_data();
    }


    function __go_top() {
        $('html, body').animate({scrollTop: 0}, 100);
    }

    function __show_process_bar() {
        var __processWidth = 0;
        __process_bar.show();
        __process_bar_thread = setInterval(function () {
            __processWidth += Math.random() * (100 - __processWidth) * 0.1;
            __process_bar.animate({width: __processWidth + '%'}, 200);
        }, 200);
    }

    function __hide_process_bar() {
        clearInterval(__process_bar_thread);
        __process_bar.animate({width: '100%'}, 100, function () {
            __process_bar.fadeOut(100, function () {
                __process_bar.width(0);
            });
        });
    }
</script>
</body>
</html>
