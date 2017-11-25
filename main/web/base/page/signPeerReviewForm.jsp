<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ include file="/base/basePageNew.jsp" %>
<%
    String __signFormAction = request.getParameter("__signFormAction");
    __signFormAction = (__signFormAction == null || "".equals(__signFormAction)) ? "Y" : __signFormAction;
%>
<html>
<head>
    <title></title>
    <style>
        #__signFormPeerReview_container {
            z-index: 1049;
        }
    </style>
</head>
<body>
<form id="__sign_form_peer_<%=__signFormAction%>">
    <table class="table table-editfdbktask">
        <tbody>
        <tr>
            <th><b class="Required" style="margin-left: 0px">*</b>审核说明</th>
            <td colspan="5"><textarea id="approvalOpinion_peer_<%=__signFormAction%>" class="form-control"></textarea>
            </td>
        </tr>
        <tr>
            <th><b class="Required" style="margin-left: 0px">*</b>下一步操作人</th>
            <td colspan="2">
                <%--<div class="input-group">--%>
                <%--<input class="form-control" type="text"  id="participantTrueName_signPeer" readonly>--%>
                <%--<input type="hidden" id="participantID_signPeer">--%>
                <%--<span id="__participant_signPeer_tree" class="input-group-addon glyphicon glyphicon-user"></span>--%>
                <%--</div>--%>
                <div style="position: relative;">
                    <input id="participantID_signPeer" name="participantID_signPeer" type="hidden">
                    <input id="participantTrueName_signPeer" name="participantTrueName_signPeer" readonly="readonly"
                           type="text" class="form-control">
                    <%--<span id="__person_tree_to_send${dispatchType}" class="input-group-addon glyphicon glyphicon-th"></span>--%>
                    <a id="__participant_signPeer_tree" class="seldatabtn"><i class="fa fa-user"></i></a>
                </div>
            </td>
        </tr>
        </tbody>
    </table>
    <%--<div class="__dialog_panel_btns">--%>
    <%--<span id="__submit_sign_form_peer_<%=__signFormAction%>" class="btn btn-danger">提交</span>--%>
    <%--<span id="__reset_sign_form_peer_<%=__signFormAction%>" class="btn btn-default">重置</span>--%>
    <%--</div>--%>
</form>
<script>
    $(document).ready(function () {
        layui.use(['layer'], function () {
        });
    });
    <%--alert('<%=request.getParameter("__link_dialog_body")%>');--%>
    <%--__resizeLinkDialog('<%=request.getParameter("__link_dialog_body")%>' , 500 , 400);--%>
    <%--$('#__submit_sign_form_peer_<%=__signFormAction%>').click(function () {--%>
    function submitBtn() {
        var data = {};
        data.operTypeEnumId = '40050227';
        var req_url = _PATH + '/workBaseController.do?method=saveAudit';
//        if (__$__processingObjectId == 0) {
//            iMsg('未设置当前审核对象ID：__$__processingObjectId');
//            return;
//        } else if (__$__processingObjectTable == 0) {
//            iMsg('未设置当前审核对象Table：__$__processingObjectTable');
//            return;
//        }
//        data.processingObjectID = __$__processingObjectId;
//        data.processingObjectTable = __$__processingObjectTable;
//            data.processingObjectID = "";
//            data.processingObjectTable = "";
        var processingObjectId = window.__$__processingObjectId;
        if (processingObjectId) {
            data.processingObjectID = window.__$__processingObjectId;
            data.processingObjectTable = window.__$__processingObjectTable;
        } else {
            data.processingObjectID = parent.__$__processingObjectId;
            data.processingObjectTable = parent.__$__processingObjectTable;
        }
        var winParams = window._winParams;
        if (winParams) {
            for (var pro in winParams) {
                data[pro] = winParams[pro];
            }
        } else {
            winParams = parent._winParams;
            if (winParams) {
                for (var pro in winParams) {
                    data[pro] = winParams[pro];
                }
            }
        }
//        data.TASKLIST = TASKLIST;
        data.operDesc = $('#approvalOpinion_peer_<%=__signFormAction%>').val();
        if (data.operDesc == '') {
            $('#approvalOpinion_peer_<%=__signFormAction%>').addClass('__notnull');
            $('#approvalOpinion_peer_<%=__signFormAction%>').focus();
            return;
        }
        data.participantTrueName = $("#participantTrueName_signPeer").val();
        if (data.participantTrueName == '') {
            $('#participantTrueName_signPeer').addClass('__notnull');
            return;
        }
        data.participantID = $("#participantID_signPeer").val();
        data.participantTrueName = $("#participantTrueName_signPeer").val();
        data.processingStatus = '<%=__signFormAction%>';
        data.attribute1 = 'peer';

        if ($("input[name='report']").length > 0) {
            if ($("input[name='report']")[0].checked) {
                data.report = $("input[name='report']")[0].value;
            } else {
                data.report = $("input[name='report']")[1].value;
            }
        }
        __show_metar_loading();
        data.levelType = 'peer';
        $.ajax({
            url: req_url,
            type: 'POST',
            async: true,
            dataType: "json",
            data: data,
            success: function (response) {
                if (response.success) {
//                    __exitFromFrame();
                    iMsg('审核成功。', 6);
                    parent.closeWindow();
                } else {
                    __hide_metar_loading();
                    if (response.msg) {
                        iMsg(response.msg);
                    } else {
                        iMsg('审核失败，请重试。');
                    }
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
//            iMsg('error' + errorThrown);
                __hide_metar_loading
                iMsg('审核失败，请重试。');
            }
        })
    }

    $('#__reset_sign_form_peer_<%=__signFormAction%>').click(function () {
        document.getElementById('__sign_form_peer_<%=__signFormAction%>').reset();
        $('#approvalOpinion_peer_<%=__signFormAction%>').focus();
    });
    $('#__participant_signPeer_tree').click(function () {
        $('#__participantSignPeerTree_container').css("z-index", 1051);
        __open_tree("participantSignPeerTree", 9, '人员树', function (selectedNodes) {
            if (selectedNodes == "") {
                selectedNodes = [{userName: '', label: ''}];
            }
            document.getElementById("participantID_signPeer").value = selectedNodes[selectedNodes.length - 1].userName;
            document.getElementById("participantTrueName_signPeer").value = selectedNodes[selectedNodes.length - 1].label;
        }, '', '', 'radio');
        $('#__participantSignPeerTree_container').css("z-index", 1051);
    });
    $('#participantTrueName_signPeer').click(function () {
        $('#__participantSignPeerTree_container').css("z-index", 1051);
        __open_tree("participantSignPeerTree", 9, '人员树', function (selectedNodes) {
            if (selectedNodes == "") {
                selectedNodes = [{userName: '', label: ''}];
            }
            document.getElementById("participantID_signPeer").value = selectedNodes[selectedNodes.length - 1].userName;
            document.getElementById("participantTrueName_signPeer").value = selectedNodes[selectedNodes.length - 1].label;
        }, '', '', 'radio');
        $('#__participantSignPeerTree_container').css("z-index", 1051);
    });
    $('#__participantSignPeerTree_container').css("z-index", 1051);
</script>
</body>
</html>
