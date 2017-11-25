function signFormPeerReview(__link_dialog_body , __batchFlag){
    __resizeLinkDialog(__link_dialog_body , 500 , 235);
//    $('#' + __link_dialog_body).html(__link_dialog_body);
//    $('#' + __link_dialog_body).load(_PATH + '/base/page/signPeerReviewForm.jsp?__signFormAction=Y&__link_dialog_body=' + __link_dialog_body);

    var iframe = $('<iframe id="' + __link_dialog_body
    + '_iframe" frameborder="0" style="width:100%;height:160px;" src="'
    +_PATH + '/base/page/signPeerReviewForm.jsp?__signFormAction=Y&__link_dialog_body=' + __link_dialog_body +'"></iframe>');

    $('#' + __link_dialog_body).append(iframe);
    var __dispatchEditorLink_btn = $('<div class="modal-footer"></div>');
    var __dispatchEditorLink_btn_submit = $('<span class="btn btn-success">确定</span>');
    __dispatchEditorLink_btn_submit.click(function(){
        document.getElementById(__link_dialog_body+"_iframe").contentWindow.submitBtn();
    })
    __dispatchEditorLink_btn.append(__dispatchEditorLink_btn_submit);
    $('#' + __link_dialog_body).parent().append(__dispatchEditorLink_btn);

}