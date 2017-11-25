<!DOCTYPE HTML>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<% String radioType = request.getParameter("radioType"); %>
<jsp:include page="../basePage.jsp" />
<html>
<body>
<div id="__ztree_panel" class="ztree"></div>
</body>
<script>
$(document).ready(function(){
    layui.use(['layer'], function(){
        showILoading();
    });
    var radioType = '<%=radioType%>';
    if(radioType == 1){
        radioType = 'radio';
    } else {
        radioType = 'checkbox';
    }

    var __ztree_setting = {
        check: {
            /**复选框**/
            nocheckInherit: false,
            enable: true,
            chkStyle: radioType,
            chkboxType: {'Y': 'ps', 'N': 'ps'},
            radioType: "all"
        },
        async: {
            autoParam: ['id' , 'type'],
            contentType: 'application/x-www-form-urlencoded',
            enable: true,
//            dataFilter: __zTreeDataFilter,
            type: 'post',
            url: _PATH + '/queryProcActiTreeNodes.do?type=0&id=1'
        },
        view: {
//            addDiyDom: addDiyDom
        },
        data: {
            key: {
                name: 'label'
            },
            simpleData: {
                enable: true,
                idKey: "nodeId",
                pIdKey: "parentId",
                isParent: "parent",
                rootPId: ""
            }
        },
        callback: {
            beforeCheck: function (treeId, treeNode) {

            },
            onAsyncSuccess : function(event, treeId, treeNode, msg){

                if(treeNode){
                    treeNode.nocheck = false;
                    __current_tree.updateNode(treeNode);
                }
                closeILoading();

            }
        }
    }
    __current_tree = $.fn.zTree.init($('#__ztree_panel'), __ztree_setting, null);

});
function getSelected(){
    return $.fn.zTree.getZTreeObj('__ztree_panel').getCheckedNodes();
}
</script>
</html>
