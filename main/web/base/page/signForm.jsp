<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" >
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ include file="/base/basePageNew.jsp" %>

<%
    String __signFormAction = request.getParameter("__signFormAction");
    __signFormAction = (__signFormAction == null || "".equals(__signFormAction)) ? "Y" : __signFormAction;
    String __batchFlag = request.getParameter("__batchFlag");
    String up = request.getParameter("up");
%>
<html>
<body>
<form id="__sign_form_<%=__signFormAction%>">
    <table class="table table-editfdbktask">
        <tbody>
        <tr>
            <th><b class="Required">*</b>审核意见</th>
            <td colspan="5"><textarea id="approvalOpinion_<%=__signFormAction%>" rows="5" class="form-control"></textarea></td>
        </tr>
        <%
            if (up.equals("Y")) {
        %>
        <tr>
            <th>是否上报</th>
            <td style="text-align: center;border: none">
                <span class="radio">
                    <label>
                        <input name="report" type="radio" value="Y">是
                    </label>
                </span>
            </td>
            <td style="text-align: center;border: none">
                <span class="radio">
                    <label>
                        <input name="report" type="radio" value="N" checked>否
                    </label>
                </span>
            </td>
        </tr>
        <% }
        %>

        </tbody>
    </table>
    <%--<div class="__dialog_panel_btns">--%>
        <%--<span id="__submit_sign_form_<%=__signFormAction%>" class="btn btn-success">提交</span>--%>
        <%--<span id="__reset_sign_form_<%=__signFormAction%>" class="btn btn-default">重置</span>--%>
    <%--</div>--%>
</form>
<script>
    $(document).ready(function () {
        layui.use(['layer'], function () {
        });
    });
    <%--iMsg('<%=request.getParameter("__link_dialog_body")%>');--%>
    <%--__resizeLinkDialog('<%=request.getParameter("__link_dialog_body")%>' , 500 , 400);--%>
    <%--$('#__submit_sign_form_<%=__signFormAction%>').click(function () {--%>
    function submitBtn() {
        var data = {};
        data.operTypeEnumId = '40050227';
        var req_url =_PATH+ '/workBaseController.do?method=generalProcess';
        if ('<%=__batchFlag%>' == 'Y') {
//            req_url = 'commFeedbackController.do?method=approval';
            var processInstID = '';
            var __selectedFeedbacks = __getCheckedFeedbacks();
            if (__selectedFeedbacks.length == 0) {
                iMsg('请选择需要操作的数据');
                return;
            }
            for (var i = 0; i < __selectedFeedbacks.length; i++) {
                processInstID += __selectedFeedbacks[i]['processInstID'];
                if (i < __selectedFeedbacks.length - 1) {
                    processInstID += ',';
                }
            }
            data.processInstID = processInstID;
        } else {
//            if (__$__processingObjectId == 0) {
//                iMsg('未设置当前审核对象ID：__$__processingObjectId');
//                return;
//            } else if (__$__processingObjectTable == 0) {
//                iMsg('未设置当前审核对象Table：__$__processingObjectTable');
//                return;
//            }
//            data.processingObjectID = __$__processingObjectId;
//            data.processingObjectTable = __$__processingObjectTable;
            var processingObjectId = window.__$__processingObjectId;
            if(processingObjectId){
                data.processingObjectID = window.__$__processingObjectId;
                data.processingObjectTable = window.__$__processingObjectTable;
            }else{
                data.processingObjectID = parent.__$__processingObjectId;
                data.processingObjectTable = parent.__$__processingObjectTable;
            }
//            data.processingObjectID = "";
//            data.processingObjectTable = "";
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
//            data.TASKLIST = TASKLIST;
        }
        data.operDesc = $('#approvalOpinion_<%=__signFormAction%>').val();
        if (data.operDesc == '') {
            $('#approvalOpinion_<%=__signFormAction%>').addClass('__notnull');
            $('#approvalOpinion_<%=__signFormAction%>').focus();
            return;
        }
        data.processingStatus = '<%=__signFormAction%>';

        if ($("input[name='report']").length > 0) {
            if ($("input[name='report']")[0].checked) {
                data.report = $("input[name='report']")[0].value;
            } else {
                data.report = $("input[name='report']")[1].value;
            }
        }
        __show_metar_loading();
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

    $('#__reset_sign_form_<%=__signFormAction%>').click(function () {
        document.getElementById('__sign_form_<%=__signFormAction%>').reset();
        $('#approvalOpinion_<%=__signFormAction%>').focus();
    });

    if ('<%=__signFormAction%>' == 'Y') {
        $('#approvalOpinion_Y').val('通过');
    }
</script>
</body>
</html>
