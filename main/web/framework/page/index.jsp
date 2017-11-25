<%@ page import="com.ucloud.paas.proxy.aaaa.entity.UserEntity" %>
<!DOCTYPE HTML>
<meta http-equiv="X-UA-Compatible" content="IE=8,chrome=1">
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    String path = request.getContextPath();
    String staticPath = request.getContextPath();
    UserEntity user = (UserEntity) session.getAttribute("globalUniqueUser");
%>
<html>
<head>
    <title>流程平台</title>
    <link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/bootstrap-3.3.4/dist/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/layui-v1.0.9/css/layui.css"/>
    <link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/layui-v1.0.9/css/modules/layer/default/layer.css"/>
    <link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/font-awesome-4.7.0/css/font-awesome.min.css"/>
    <link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/scrollbar/jquery.mCustomScrollbar.min.css"/>
    <link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/animate.css"/>
    <link rel="stylesheet" type="text/css" href="<%=staticPath%>/framework/css/index.css"/>
</head>
<body>
<div id="left-menu-tooltip" class="tooltip in right" role="tooltip"><div class="tooltip-arrow"></div><div id="left-menu-tooltip-text" class="tooltip-inner"></div></div>
<div id="header-container">
    <div class="pull-left">
        <a id="btn-home" class="pull-left uf-menu" href="index.jsp"><span class="fa fa-paper-plane-o fa-2x"></span><span>流程平台</span></a>
    </div>
    <div id="header-left" class="pull-left">
        <a id="business" class="pull-left uf-menu first" data-href="http://baidu.com"><span>业务中心</span></a>
    </div>
    <div id="header-right" class="pull-right">
        <a id="develop" class="pull-left uf-menu first" data-href="http://baidu.com"><span>开发中心</span></a>
        <a id="analysis" class="pull-left uf-menu first" data-href="http://baidu.com"><span>分析中心</span></a>
        <a id="btn-user" class="pull-left uf-menu"><span class="fa fa-user-circle-o fa-2x"></span><span><%=user.getTrueName()%></span></a>
        <a id="btn-power-off" class="pull-left uf-menu" href="<%=path%>"><span class="fa fa-power-off fa-lg"></span><span>退出</span></a>
    </div>
    <%--<div class="pull-right">--%>
        <%--<a class="pull-left uf-menu"><span class="fa fa-search fa-lg"></span><span>搜索</span></a>--%>
        <%--<a class="pull-left uf-menu"><span class="fa fa-bell-o fa-lg"></span><span>消息</span></a>--%>
        <%--<a class="pull-left uf-menu"><span>最近使用</span></a>--%>
        <%--<a class="pull-left uf-menu"><span>应用收藏</span></a>--%>
    <%--</div>--%>


</div>
<div id="left-menu-container">
    <a id="btn-expand-collapse" href="javascript:;"><span class="fa fa-angle-double-left"></span></a>
    <ul id="left-menu-nav" class="uf-nav">
        <li class="uf-nav-item">
            <a class="uf-nav-parent" href="javascript:void(0);"><span class="fa fa-caret-right menu-icon animated"></span><span class="menu-text">解决方案</span></a>
            <ul class="uf-nav">
                <li class="uf-nav-item"><a href="javascript:void(0);" onclick="alert('hello world')" href="javascript:void(0);"><span class="fa fa-gear menu-icon"></span><span class="menu-text">Alert</span></a></li>
                <li class="uf-nav-item"><a href="javascript:void(0);" onclick="msg()" href="javascript:void(0);"><span class="fa fa-gear menu-icon"></span><span class="menu-text">Msg</span></a></li>
                <li class="uf-nav-item"><a href="javascript:void(0);" onclick="showLoading()" href="javascript:void(0);"><span class="fa fa-gear menu-icon"></span><span class="menu-text">Loading</span></a></li>
                <li class="uf-nav-item"><a href="javascript:void(0);" onclick="confirmWindow()" href="javascript:void(0);"><span class="fa fa-gear menu-icon"></span><span class="menu-text">Confirm</span></a></li>
                <li class="uf-nav-item"><a href="javascript:void(0);" onclick="showModalWindow()"><span class="fa fa-gear menu-icon"></span><span class="menu-text">Open</span></a></li>
                <li class="uf-nav-item"><a href="javascript:void(0);" onclick="showFlowDesigner()"><span class="fa fa-gear menu-icon"></span><span class="menu-text">流程编辑器</span></a></li>
            </ul>
        </li>
        <div id="business-left-menu-container" class="left-menu-container">
            <li class="uf-nav-item"><a class="uf-menu" id="business-todo" href="javascript:void(0);" data-href="<%=path%>/base/frame/todo.jsp"><span class="fa fa-bookmark menu-icon"></span><span class="menu-text">待办工单</span></a></li>
            <li class="uf-nav-item"><a class="uf-menu" id="business-already" href="javascript:void(0);" data-href="<%=path%>/base/frame/already.jsp"><span class="fa fa-check-square menu-icon"></span><span class="menu-text">已办工单</span></a></li>
            <li class="uf-nav-item"><a class="uf-menu" id="business-launch" href="javascript:void(0);" data-href="<%=path%>/base/page/launchList.jsp"><span class="fa fa-edit menu-icon"></span><span class="menu-text">发起工单</span></a></li>
            <li class="uf-nav-item"><a class="uf-menu" id="business-query" href="javascript:void(0);" data-href="<%=path%>/base/page/todo.jsp"><span class="fa fa-list menu-icon"></span><span class="menu-text">工单查询</span></a></li>
            <li class="uf-nav-item"><a class="uf-menu" id="business-privilege" href="javascript:void(0);" data-href="/PMOS/page/pmos/processFrame.jsp"><span class="fa fa-gear menu-icon"></span><span class="menu-text">流程权限配置</span></a></li>
            <li class="uf-nav-item"><a class="uf-menu" id="business-test" href="javascript:void(0);" data-href="<%=path%>/test/start.do"><span class="fa fa-gear menu-icon"></span><span class="menu-text">测试发起工单</span></a></li>
        </div>
        <div id="develop-left-menu-container" class="left-menu-container">
            <li class="uf-nav-item"><a class="uf-menu" id="develop-workflow" href="javascript:void(0);" data-href="<%=path%>/workflow/forward.jsp?url=/workspace/com.primeton.bps.web.composer.bizflowmgr.bizProcessCustomFrame.flow&globalUniqueID=${globalUniqueID}"><span class="fa fa-bookmark menu-icon"></span><span class="menu-text">流程管理</span></a></li>
            <li class="uf-nav-item"><a class="uf-menu" id="develop-form" href="javascript:void(0);" data-href="<%=path%>/base/page/formManageSelect.jsp"><span class="fa fa-check-square menu-icon"></span><span class="menu-text">表单管理</span></a></li>
            <li class="uf-nav-item"><a class="uf-menu" id="develop-button" href="javascript:void(0);" data-href="<%=path%>/base/page/flowButtonList.jsp?tenantId="><span class="fa fa-edit menu-icon"></span><span class="menu-text">按钮管理</span></a></li>
            <li class="uf-nav-item"><a class="uf-menu" id="develop-publish" href="javascript:void(0);" data-href="<%=path%>/base/page/workOrderListDev.jsp"><span class="fa fa-list menu-icon"></span><span class="menu-text">发布工单</span></a></li>
            <li class="uf-nav-item"><a class="uf-menu" id="develop-process" href="javascript:void(0);" data-href="/PMOS/processController.do?method=initProcess"><span class="fa fa-gear menu-icon"></span><span class="menu-text">业务流程管理</span></a></li>
            <li class="uf-nav-item"><a class="uf-menu" id="develop-node" href="javascript:void(0);" data-href="/PMOS/nodeController.do?method=initNode"><span class="fa fa-gear menu-icon"></span><span class="menu-text">业务节点管理</span></a></li>
        </div>
    </ul>
    <span id="left-uf-nav-bar"></span>
</div>
<div id="content-container">
    <%--<iframe id="content-iframe" frameborder="no" src="<%=path%>/dashboard/dashboardShow.action?dashboardId=UUID-a2b17c86-8878-bd98-1eea-5c0c320cc65c&path=首页"></iframe>--%>
    <iframe id="content-iframe" frameborder="no" src=""></iframe>
        <div id="content-mask"></div>
</div>
</body>
<script type="text/javascript" src="<%=staticPath%>/component/jquery-1.12.3.min.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/bootstrap-3.3.4/dist/js/bootstrap.min.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/layui-v1.0.9/layui.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/layui-v1.0.9/lay/modules/layer.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/scrollbar/jquery.mCustomScrollbar.concat.min.js"></script>
<script type="text/javascript" src="<%=staticPath%>/framework/js/common.js"></script>
<script type="text/javascript" src="<%=staticPath%>/framework/js/index.js"></script>
<script>
    var _PATH = '<%=path%>';
    var _globalUniqueID = '<%=user.getAttribute1()%>';

    function showFlowDesigner(){
        showIModal({
            title:'',
            type: 2,
            maxmin:false,
            area: ['100%' , '100%'],
            closeBtn :1,
            content: _PATH + '/component/flowDesigner/flowDesigner.jsp'

        });
    }
</script>
</html>
