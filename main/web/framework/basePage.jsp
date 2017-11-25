<!DOCTYPE HTML>
<meta http-equiv="X-UA-Compatible" content="IE=EDGE,chrome=1">
<meta name="renderer" content="webkit|ie-comp|ie-stand">
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%
    String path = request.getContextPath();
    String staticPath = request.getContextPath();
%>
<title></title>
<link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/bootstrap-3.3.4/dist/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/layui-v1.0.9/css/layui.css"/>
<link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/font-awesome-4.7.0/css/font-awesome.min.css"/>
<link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/scrollbar/jquery.mCustomScrollbar.min.css"/>
<link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/animate.css"/>
<link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/ztree/css/zTreeStyle1.css"/>
<link rel="stylesheet" type="text/css" href="<%=staticPath%>/component/jquery.dtGrid.v1.1.9/jquery.dtGrid.min.css"/>
<link rel="stylesheet" type="text/css" href="<%=staticPath%>/base/_css/metarnet.css"/>

<!--[if lte IE 9]>
<script type="text/javascript" src="<%=staticPath %>/component/adapter-ie8/html5shiv.js"></script>
<script type="text/javascript" src="<%=staticPath %>/component/adapter-ie8/respond.js"></script>
<![endif]-->
<script type="text/javascript" src="<%=staticPath%>/component/jquery-1.12.3.min.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/bootstrap-3.3.4/dist/js/bootstrap.min.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/scrollbar/jquery.mCustomScrollbar.concat.min.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/layui-v1.0.9/layui.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/ztree/js/jquery.ztree.core-3.5.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/ztree/js/jquery.ztree.excheck-3.5.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/jquery.dtGrid.v1.1.9/jquery.dtGrid.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/jquery.dtGrid.v1.1.9/i18n/zh-cn.js"></script>
<script type="text/javascript" src="<%=staticPath%>/framework/js/common.js"></script>
<script type="text/javascript" src="<%=staticPath%>/base/_js/require.js"></script>
<script type="text/javascript" src="<%=staticPath%>/component/jquery-uploadify/jquery.uploadify.min.js"></script>

<script>
    var _PATH = '<%=path%>';
    var _STATIC_PATH = '<%=staticPath%>';
    var _globalUniqueID = "${globalUniqueID}";
    $(document).ready(function(){
        layui.use(['layer'], function(){});
    });
</script>