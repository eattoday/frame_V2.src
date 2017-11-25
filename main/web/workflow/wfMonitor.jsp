<%@ page import="java.util.List" %>
<%@ page import="com.metarnet.core.common.model.MonitorNode" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String path = request.getContextPath();
    String layout = request.getParameter("layout");
    response.getWriter().println("<!DOCTYPE HTML>");
%>
<html>
<meta http-equiv="X-UA-Compatible" content="IE=8">
<meta http-equiv="content-type" content="text/html;charset=utf-8"/>
<link rel="stylesheet" type="text/css"
      href="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/bootstrap/css/bootstrap.min.css"/>
<head>

    <style type="text/css">
        .nodeContainer {
            position: absolute;
            text-align: center;
        }

        .node {
            padding: 0 5px;
            margin-bottom: 3px;
            border-radius: 8px;
            width: 220px;
            height: 100px;
            text-align: left;
            font-size: 12px;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .node div {
            margin: 5px 0;
        }

        .glyphicon {
            margin-right: 5px;
        }

        .user_card_container {
            padding: 10px;
            float: left;
            position: relative;
        }

        .user_card_container:hover {
            background-color: #f5f5f5;
        }

        #transfer-btn {
            position: fixed;
            z-index: 10;
            font-size: 20px;
            right: 10px;
            top: 60px;
            border-radius: 8px;
        }

        #loading-icon {
            position: fixed;
            z-index: 10;
            font-size: 200px;
            top: 50%;
            left: 50%;
            margin-top: -100px;
            margin-left: -100px;
            display: none;
        }

        .btn-regect {
            background-color: #a0a0a0
        }
    </style>
</head>
<body>
<div id="loading-icon"><span class="glyphicon glyphicon-transfer"></span></div>

<%
    if ("1".equals(layout)) {
        response.getWriter().println("<a onclick='transfer(0)' title='切换至竖版' id=\"transfer-btn\" class=\"btn btn-primary\"><span class=\"glyphicon glyphicon-resize-vertical\"></span></a>");
    } else {
        response.getWriter().println("<a onclick='transfer(1)' title='切换至横版' id=\"transfer-btn\" class=\"btn btn-primary\"><span class=\"glyphicon glyphicon-resize-horizontal\"></span></a>");
    }
%>

<script type="text/javascript" src="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/jquery/jquery.min.js"></script>
<script type="text/javascript"
        src="<%=path%>/component/jquery.dtGrid.v1.1.9/dependents/bootstrap/js/bootstrap.min.js"></script>
<script type="text/javascript" src="<%=path%>/component/jsPlumb-1.4.1/jquery.jsPlumb-1.4.1-all-min.js"></script>
<script type="text/javascript" src="<%=path%>/component/layui-v1.0.9/layui.js"></script>
<script type="text/javascript" src="<%=path%>/framework/js/common.js"></script>
<%--<script type="text/javascript" src="http://10.249.6.35/commdo/js/showUserCard.js"></script>--%>
<script type="text/javascript" src="<%=path%>/base/_js/require.js"></script>

<script type="text/javascript">
    $(document).ready(function () {
        layui.use(['layer'], function () {
        });
    });
    var _PATH = '<%=path%>';
    var layout = '<%=layout%>';

    var direction = layout == 1 ? -1 : 1;

    function transfer(layout) {

        $('#loading-icon').show();

        var currUrl = location.href;

        var anchorIndex;

        if ((anchorIndex = currUrl.indexOf('&layout=')) > -1) {
            currUrl = currUrl.substring(0, anchorIndex);
        }
        location.href = currUrl + '&layout=' + layout;

    }

    jsPlumb.ready(function () {

        var firstInstance = jsPlumb.getInstance();
        firstInstance.importDefaults({
            PaintStyle: {strokeStyle: "#333"},
            EndpointStyle: {fillStyle: "#333"},
            Connector: "Flowchart",
            Endpoint: "Blank",
            ConnectionOverlays: [
                ["Arrow", {
                    location: 1,
                    direction: direction,
                    length: 8,
                    width: 10,
                    foldback: 1
                }]
            ]
        });

        <%
            response.getWriter().println("<script type='text/javascript'>var connectLib = new Array();</script>");

            List<MonitorNode> list = (List<MonitorNode>) request.getAttribute("list");

            int maxPostionTop = 0;
            int maxPostionLeft = 0;

            String preNodeId = "";

            for(int i = 0 ; i < list.size() ; i++){
                MonitorNode node = list.get(i);
                String stateStyle = "";
                if("TODO".equals(node.getState())){
                    stateStyle = "btn-primary";
                } else if("DANGER".equals(node.getState())){
                    stateStyle = "btn-danger";
                } else if("REJECT".equals(node.getState())){
                    stateStyle = "btn-default btn-regect";
                } else {
                    stateStyle = "btn-default";
                }



                int positionY = 0;
                int positionX = 0;

                if("1".equals(layout)){
                    //横版
                    positionY = node.getPositionX()*70 + 60;
                    positionX = node.getPositionY()*280 + 20;
                } else {
                    //默认竖版
                    positionY = node.getPositionY()*160 + 60;
                    positionX = node.getPositionX()*140 + 20;
                }

                if(positionY > maxPostionTop){
                    maxPostionTop = positionY;
                }
                if(positionX > maxPostionLeft){
                    maxPostionLeft = positionX;
                }

                response.getWriter().println("<div class='nodeContainer' id='" + node.getId() + "' style='top:" + positionY + "px;left:" + positionX + "px' >" +
                 "<div class='node btn "+ stateStyle +"'>" + node.getLabel() + "</div>" +
                  "<div>" + node.getOrgName() + "</div>" +
                   "</div>");
                List<MonitorNode> preNodes = node.getPreNodes();

                if(preNodes.size() == 0){
//                    response.getWriter().println("<script type='text/javascript'>connectLib.push({source:'" + preNodeId + "', target:'" + node.getId() + "' , anchors:[ 'BottomCenter', 'TopCenter' ]});</script>");
                } else {
                    for(int j = 0 ; j < preNodes.size() ; j++){

                        MonitorNode preNode = preNodes.get(j);

                        if("1".equals(layout)){
                            //横版
                            response.getWriter().println("<script type='text/javascript'>connectLib.push({source:'" + preNode.getId() + "', target:'" + node.getId() + "' , anchors:[ 'Right', 'Left' ]});</script>");
                        } else {
                            //默认竖版
                            response.getWriter().println("<script type='text/javascript'>connectLib.push({source:'" + preNode.getId() + "', target:'" + node.getId() + "' , anchors:[ 'BottomCenter', 'TopCenter' ]});</script>");
                        }


                    }
                }

                preNodeId = node.getId();
            }

        %>

        for (var x = 0; x < connectLib.length; x++) {
//            alert(connectLib[x]['source'] + ',' + connectLib[x]['target']);
            firstInstance.connect(connectLib[x]);
        }

        document.body.style.height = '<%=maxPostionTop + 130%>px';
        document.body.style.width = '<%=maxPostionLeft + 290%>px';

//        __bindCard($('.user-card'));
        closeILoading();
    });

    function __show_person_info(action) {
        var person_usernames = $(action).attr('usernames');

        if ($('#' + action.id + 'modal')[0]) {
//            $('#' + action.id+'modal').modal('show');
            $('#' + action.id + 'modal').modal({backdrop: 'static', show: true});
            return;
        }


        var modal = $('<div class="modal fade" id="' + action.id + 'modal" tabindex="-1" role="dialog" aria-labelledby="' + action.id + 'label" aria-hidden="true"></div>');
        var modal_dialog = $('<div class="modal-dialog" style="width:100%;padding-top:30px;padding-left:20px;padding-right:20px;"></div>');
        var modal_content = $('<div class="modal-content"></div>');
        var modal_header = $('<div class="modal-header"></div>');
        var modal_close = $('<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>');
        var modal_title = $('<h4 class="modal-title" id="' + action.id + 'label">当前工单处理人</h4>');
        var modal_body = $('<div class="modal-body"></div>');
        var modal_footer = $('<div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">关闭</button></div>');

        modal_header.append(modal_close).append(modal_title);
        modal_content.append(modal_header).append(modal_body).append(modal_footer);
        modal_dialog.append(modal_content);
        modal.append(modal_dialog);
//        modal.modal('show');
        modal.modal({backdrop: 'static', show: true});
//        __open_metar_window(action.id , '当前处理人' , 960 , 460 , function(window_body){
        $.ajax({
            url: _PATH + '/commWorkFlowMonitorController.do?method=getUserEntityByUserNames&userNames=' + person_usernames,
            method: 'POST',
            async: true,
            dataType: 'json',
            success: function (users) {
                if (users) {

                    for (var i = 0; i < users.length; i++) {
                        var user = users[i];

                        var user_card_container = $('<div class="user_card_container"></div>');
                        var user_card = $('<div id="__' + user.userId + '_container" style="float:left;border:1px solid #bbb;width:400px;height:245px;padding-right:0;background-color:#fff;"></div>');
                        var __window_body = $('<div style="margin-top: 10px;margin-left:15px;padding-left:15px;"></div>');
                        var __window_top = $('<div style="margin-top: 15px;margin-left:15px;"><img src="' + _PATH + '/base/_resources/logo2.png" alt="" style="height:70px;width:90px;float:left;margin-top:12px;margin-right:12px;"></div>');
                        var __nameAndOrg = $('<div style="float:left;width:280px;"></div>');
                        var _window_tureName = $('<div style="margin-top: 10px; margin-left: 10px; font-size: 16px;"></div>');
                        var _window_fullOrgName = $('<div style="margin-top: 12px; margin-left: 10px;height:70px;"></div>');
                        var _window_mail = $('<div></div>');
                        var _window_telephone = $('<div></div>');
                        var _window_mobilephone = $('<div style=""></div>');
                        //中间横条
                        var __window_middle = $('<div style="margin-top: 10px;width:100%;height:28px;background-image: url(' + _PATH + '/base/_resources/card.png)" ></div>');
//                        var __msg_icon = $('<img src="' + _PATH + '/base/_resources/msg_icon.png" title="跟TA聊天" height="25" style="cursor:pointer;float:left;margin-left:30px;margin-top:1px;">');
//                        __msg_icon.hover(function () {
//                            $(this).css({'margin-top': '2px'});
//                        }, function () {
//                            $(this).css({'margin-top': '1px'});
//                        });

//                        bindChat(__msg_icon, user.userName);

//                        __window_middle.append(__msg_icon);

                        _window_tureName.text(user.trueName);
                        _window_fullOrgName.text(user.orgName);
                        if (user.email == undefined) {
                            _window_mail.text('邮箱: ');
                        } else {
                            _window_mail.text('邮箱: ' + user.email);
                        }
                        _window_telephone.text('办公号码：' + user.telephone);
                        _window_mobilephone.text('移动电话：' + user.mobilePhone);

                        __window_body.append(_window_mail);
                        __window_body.append(_window_telephone);
                        __window_body.append(_window_mobilephone);

                        __nameAndOrg.append(_window_tureName);
                        __nameAndOrg.append(_window_fullOrgName);

                        __window_top.append(__nameAndOrg);
                        __window_top.append($('<div style="clear:both"></div>'));
                        user_card.append(__window_top);   //上面部分
                        user_card.append(__window_middle);    //中间部分
                        user_card.append(__window_body);    //下面部分

                        user_card_container.append(user_card);
                        modal_body.append(user_card_container);

                    }

                    modal_body.append('<div style="clear:both"></div>');

                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
//                    __hide_metar_loading();
            }
        })
//        });

    }

    function bindChat(__msg_icon, userName) {
        __msg_icon.click(function () {
            {
                top.open('/msg_platform/sceneChat.do?method=singleChat&globalUniqueID=${globalUniqueID}&otherUser=' + userName
                        , 'groupwindow', 'height=600, width=852, top=0, left=0, toolbar=no, menubar=no, scrollbars=no, resizable=no, location=no, status=no');
            }
        })
    }
    $(".logModeloperDesc").hover(function () {
        layer.tips($(this).html(), ".logModeloperDesc", {tips: [2, '#0088CC'], area: ['500px', 'auto']});
    });
</script>
</body>
</html>