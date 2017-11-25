//@ sourceURL=ArchiveLink.js
function ArchiveLink(__link_dialog_body) {
    __show_metar_loading();
    var data = {};
//    if(__$__processingObjectId == 0){
//        alert('未设置当前审核对象ID：__$__processingObjectId');
//        return false;
//    } else if(__$__processingObjectTable == 0){
//        alert('未设置当前审核对象Table：__$__processingObjectTable');
//        return false;
//    }
    data.processingType = "ARCHIVE";
    data.operTypeEnumId = '40050439';
    data.processingReason = "";
    data.processingResultOpinion = "";
    var processingObjectId = window.__$__processingObjectId;
    if(processingObjectId){
        data.processingObjectID = window.__$__processingObjectId;
        data.processingObjectTable = window.__$__processingObjectTable;
    }else{
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
    $.ajax({
        url: "workBaseController.do?method=generalProcess",
        type: 'POST',
        async: true,
        dataType: "json",
        data: data,
        success: function (response) {
            if (response.success) {
                iMsg('归档成功', 6);
                parent.closeWindow();
            } else {
                __hide_metar_loading();
                iMsg(response.success);
            }

        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            __hide_metar_loading();
            iMsg('error' + errorThrown);
        }
    });
    return false;
}
