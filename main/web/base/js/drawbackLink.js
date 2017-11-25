//function drawbackLink(__link_dialog_body){
//    var data = {};
//    for(var pro in _winParams){
//        data[pro] = _winParams[pro];
//    }
//    $.ajax({
//        url : "workBaseController.do?method=drawbackWorkItem" ,
//        type : 'POST',
//        async : true,
//        dataType : "json",
//        data : data,
//        success:function(response){
//            if(response.success){
//                alert('撤回成功');
//                __exitFromFrame();
//            } else {
//                alert('撤回失败');
//            }
//
//        },
//        error:function(XMLHttpRequest, textStatus, errorThrown){
//            alert('撤回失败:' + errorThrown);
//        }
//    });
//    return false;
//}

//@ sourceURL=drawbackLink.js
var isaj = true;
function drawbackLink(__link_dialog_body) {
    var data = {};
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
    var processingObjectId = window.__$__processingObjectId;
    if(processingObjectId){
        data.processingObjectID = window.__$__processingObjectId;
        data.processingObjectTable = window.__$__processingObjectTable;
    }else{
        data.processingObjectID = parent.__$__processingObjectId;
        data.processingObjectTable = parent.__$__processingObjectTable;
    }
    __show_metar_loading();
    if (isaj) {
        isaj = false;
        //setTimeout(function () {
        //    window.opener = null;
        //    window.open('', '_self');
        //    window.close();
        //}, 1500);
        $.ajax({
            url: "workBaseController.do?method=backActivity",
            type: 'POST',
            async: true,
            dataType: "json",
            data: data,
            success: function (response) {
                __hide_metar_loading();
                if (response.success) {
                    iMsg("已经取回到我的待办", 6);
                    setTimeout(function () {
                        window.opener = null;
                        window.open('', '_self');
                        window.close();
                    }, 1500);
                } else {
                    iMsg('取回失败');
                    __hide_metar_loading();
                    setTimeout(function () {
                        window.opener = null;
                        window.open('', '_self');
                        window.close();
                    }, 1500);
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                iMsg('取回失败:' + errorThrown);
                __hide_metar_loading();
                setTimeout(function () {
                    window.opener = null;
                    window.open('', '_self');
                    window.close();
                }, 1500);
            }
        });
    }
    return false;
}
