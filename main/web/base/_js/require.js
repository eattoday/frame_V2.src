//@ sourceURL=require.js
function require(__linkName, callback) {
    $.ajax({
        url: __linkName,
        type: 'POST',
        async: true,
        success: function (response) {
            $('body').append('<script>' + response + '</script>');
            if (callback) {
                callback();
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {

        }
    })
}

/*---初始化上传附件组件---*/
/**
 *
 * @param __file_uploader       type=file的input
 * @param __flowingObjectId     附件关联的对象ID
 * @param __flowingObjectTable  附件关联的对象表名
 * @param __type                模式，edit:编辑模式，可以上传附件     detail（默认）:只读模式，只显示附件列表
 * @param __callback            回调函数
 * @param iscounter             附件列表是否添加计数，Y（默认）：计数     N：不计数
 * @param bordered              是否显示边框,N（默认）：不显示        Y：显示
 * @private
 */
function __init_attachment_function(__file_uploader, __flowingObjectId, __flowingObjectTable, __type, __callback, iscounter, bordered) {
    var __file_uploader_file_list_container = $('<div class="__metar_attachment_list" style=""></div>');
    if (iscounter == 'N') {
        __file_uploader_file_list_container = $('<div class="__metar_attachment_list_nocounter"></div>');
    }
    if (bordered == 'Y') {
        __file_uploader_file_list_container.addClass('__metar_attachment_list_bordered');
    }
    if (__type != 'edit') {
        __file_uploader_file_list_container.css({'padding': '0px'});
    }
    var __file_list_table = $('<table style="width:100%;"></table>');
    var __file_list_tbody = $('<tbody></tbody>');
    var __file_list_title = $('<tr></tr>');
    __file_list_title.append('<th>文件名称</th><th>文件大小</th>');
    if (__type == 'edit') {
        __file_list_title.append('<th>操作</th>');
    }
    __file_uploader_file_list_container.append(__file_list_table.append(__file_list_tbody.append(__file_list_title)));
    $('#' + __file_uploader).parent().append(__file_uploader_file_list_container);
    $.ajax({
        url: _PATH + '/attachment.do?method=query',
        type: 'POST',
        async: true,
        dataType: 'json',
        data: {
            flowingFlag: 'Y',
            jsonData: '[{flowingObjectId:"' + __flowingObjectId + '" , flowingObjectTable:"' + __flowingObjectTable + '" , flowingObjectShardingId:"1"}]'
        },
        success: function (response) {
            if (response) {
                var __attachmentList = response.attachmentList;
                if (__attachmentList.length > 0) {
                    for (var i = 0; i < __attachmentList.length; i++) {
                        var __attachment_data = __attachmentList[i];
                        var __attachmentName = __attachment_data.attachmentName;
                        var __attachmentSize = __attachment_data.attachmentSize;
                        if (__attachmentSize == null) {
                            __attachmentSize = '0KB';
                        } else {
                            var __size = window.parseFloat(__attachmentSize / 1024).toFixed(2);
                            if (__size == 0) {
                                __attachmentSize = __attachmentSize + 'B';
                            } else if (__size < 1024) {
                                __attachmentSize = __size + 'KB';
                            } else {
                                __size = window.parseFloat(__size / 1024).toFixed(2);
                                __attachmentSize = __size + 'MB';
                            }
                        }
                        var jsonArr = encodeURIComponent('[{attachmentId:' + __attachment_data.attachmentId + '}]');
                        var __file_list_tr = $('<tr id="__attachment_' + __attachment_data.attachmentId + '"><td><i class="fa fa-file-text" style="margin-right:10px;"></i>' +
                        '<a href="' + _PATH + '/attachment.do?method=download&flowingFlag=Y&jsonArr=' + jsonArr + '">' + __attachmentName + '</a>'
                        + '</td><td>' + __attachmentSize + '</td></tr>');
                        if (__type == 'edit') {
                            __file_list_tr.append('<td><a href="javascript:void(0)" onclick="__metar_delete_file(' + __attachment_data.attachmentId + ' , \'' + __attachmentName + '\', \'' + __attachment_data.flowingObjectId + '\' , \'' + __attachment_data.flowingObjectTable + '\')"><i class="fa fa-trash-o"></i></a></td>');
                        }
                        __file_list_tbody.append(__file_list_tr);

                    }
                } else if (__type != 'edit') {
                    __file_uploader_file_list_container.remove();
                }
                if (__callback) {
                    eval(__callback + '()');
                }

            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {

        }
    });
    if (__type == 'edit') {
        __file_uploader_file_list_container.css({'margin-top': '5px', 'margin-bottom': '10px'});
        $('#' + __file_uploader).uploadify({
            'swf': _PATH + '/base/_resources/uploadify.swf',
            'cancelImg': _PATH + '/base/_resources/uploadify-cancel.png',
            'uploader': _PATH + '/attachment.do?method=upload&globalUniqueID=' + _globalUniqueID + '&flowingFlag=Y&flowingObjectId=' + __flowingObjectId + '&flowingObjectTable=' + __flowingObjectTable + '&flowingObjectShardingId=1&attachmentTypeEnumId=1&attachmentFormatEnumId=1&shardingId=1&activityInstanceId=1&taskInstanceId=1&',
            'buttonText': '<span class=""><i class="fa fa-cloud-upload" style="margin-right:10px"></i>上传文件</span>',
            'type': 'get',
            'auto': true,
            'fileObjName': 'uploadFiles',
            'multi': true,
            'simUploadLimit': 10,
            'successTimeout': 1800,
            'width': 100,
            'onSelect': function (event, ID, fileObj) {
//                    alert('上传中...');
            },
            'onFallback': function () {
                iMsg("您未安装FLASH控件，无法上传图片！请安装FLASH控件后再试。");
            },
            'onUploadStart': function (fileObj) {
                if (fileObj.size > 12582912) {
                    iMsg("您上传的文件大于12M,请重新上传！");
                    $('#' + __file_uploader).uploadify('stop');
                    $('#' + __file_uploader).uploadify('cancel', '*');
                    return;
                }
            },
            'onUploadSuccess': function (file, data, response) {
                var returnData = eval('(' + data + ')');
                if (returnData.success) {
                    var __attachment_data = eval('(' + returnData.data + ')');
                    var __attachmentName = __attachment_data.attachmentName;
                    var __attachmentSize = __attachment_data.attachmentSize;
                    if (__attachmentSize == null) {
                        __attachmentSize = '0KB';
                    } else {
                        var __size = window.parseFloat(__attachmentSize / 1024).toFixed(2);
                        if (__size == 0) {
                            __attachmentSize = __attachmentSize + 'B';
                        } else if (__size < 1024) {
                            __attachmentSize = __size + 'KB';
                        } else {
                            __size = window.parseFloat(__size / 1024).toFixed(2);
                            __attachmentSize = __size + 'MB';
                        }
                    }
                    //__file_uploader_file_list_container.append($('<li id="__attachment_' + __attachment_data.attachmentId + '">' +
                    //'<a href="' + _PATH + '/attachment.do?method=download&flowingFlag=Y&jsonArr=[{attachmentId:' + __attachment_data.attachmentId + '}]">' + __attachmentName + '</a>' +
                    //'('+__attachmentSize+')&nbsp;'+
                    //'<a onclick="__metar_delete_file(' + __attachment_data.attachmentId + ' , \'' + __attachmentName + '\')">删除</a>' +
                    //'</li>'));
                    var jsonArr = encodeURIComponent('[{attachmentId:' + __attachment_data.attachmentId + '}]');
                    var __file_list_tr = $('<tr id="__attachment_' + __attachment_data.attachmentId + '"><td><i class="fa fa-file-text" style="margin-right:10px;"></i>' +
                    '<a href="' + _PATH + '/attachment.do?method=download&flowingFlag=Y&jsonArr=' + jsonArr + '">' + __attachmentName + '</a>'
                    + '</td><td>' + __attachmentSize + '</td></tr>');
                    if (__type == 'edit') {
                        __file_list_tr.append('<td><a href="javascript:void(0)" onclick="__metar_delete_file(' + __attachment_data.attachmentId + ' , \'' + __attachmentName + '\', \'' + __attachment_data.flowingObjectId + '\' , \'' + __attachment_data.flowingObjectTable + '\')"><i class="fa fa-trash-o"></i></a></td>');
                    }
                    __file_list_tbody.append(__file_list_tr);
                    if (__callback) {
                        eval(__callback + '()');
                    }
                } else {
                    iMsg('上传失败，请重新上传。');
                }
            }
        })
    } else {
        $('#' + __file_uploader).hide();
    }

}
//function __init_attachment_function(__file_uploader, __flowingObjectId, __flowingObjectTable, __type, __callback, iscounter) {
//    var __file_uploader_file_list_container = $('<ul class="__metar_attachment_list"></ul>');
//    if (iscounter == 'N') {
//        __file_uploader_file_list_container = $('<ul class="__metar_attachment_list_nocounter"></ul>');
//    }
//    $('#' + __file_uploader).parent().append(__file_uploader_file_list_container);
//    $.ajax({
//        url: _PATH+'/attachment.do?method=query',
//        type: 'POST',
//        async: true,
//        dataType: 'json',
//        data: {
//            flowingFlag: 'Y',
//            jsonData: '[{flowingObjectId:"' + __flowingObjectId + '" , flowingObjectTable:"' + __flowingObjectTable + '" , flowingObjectShardingId:"1"}]'
//        },
//        success: function (response) {
//            if (response) {
//                var __attachmentList = response.attachmentList;
//                for (var i = 0; i < __attachmentList.length; i++) {
//                    var __attachment_data = __attachmentList[i];
//                    var __attachmentName = __attachment_data.attachmentName;
//                    var __attachmentSize = __attachment_data.attachmentSize;
//                    if(__attachmentSize == null){
//                        __attachmentSize = '0KB';
//                    } else {
//                        var __size = window.parseFloat(__attachmentSize/1024).toFixed(2);
//                        if (__size == 0) {
//                            __attachmentSize = __attachmentSize + 'B';
//                        } else if(__size < 1024){
//                            __attachmentSize = __size + 'KB';
//                        } else {
//                            __size = window.parseFloat(__size/1024).toFixed(2);
//                            __attachmentSize = __size + 'MB';
//                        }
//                    }
//                    if (__type == 'edit') {
//                        __file_uploader_file_list_container.append($('<li id="__attachment_' + __attachment_data.attachmentId + '">' +
//                            '<a href="' + _PATH + '/attachment.do?method=download&flowingFlag=Y&jsonArr=[{attachmentId:' + __attachment_data.attachmentId + '}]">' + __attachmentName + '</a>' +
//                        '('+__attachmentSize+')&nbsp;'+
//                            '<a onclick="__metar_delete_file(' + __attachment_data.attachmentId + ' , \'' + __attachmentName + '\')">删除</a>' +
//                            '</li>'));
//                    } else {
//                        __file_uploader_file_list_container.append($('<li id="__attachment_' + __attachment_data.attachmentId + '">' +
//                        //__attachmentSize+
//                        '<a href="' + _PATH + '/attachment.do?method=download&flowingFlag=Y&jsonArr=[{attachmentId:' + __attachment_data.attachmentId + '}]">' + __attachmentName + '</a>' +
//                        '('+__attachmentSize+')&nbsp;'+
//                            '</li>'));
//                    }
//
//                }
//                if (__callback) {
//                    eval(__callback + '()');
//                }
//
//            }
//        },
//        error: function (XMLHttpRequest, textStatus, errorThrown) {
//
//        }
//    });
//    if (__type == 'edit') {
//        $('#' + __file_uploader).uploadify({
//            'swf': _PATH + '/base/_resources/uploadify.swf',
//            'cancelImg': _PATH + '/base/_resources/uploadify-cancel.png',
//            'uploader': _PATH + '/attachment.do?method=upload&globalUniqueID=' + _globalUniqueID + '&flowingFlag=Y&flowingObjectId=' + __flowingObjectId + '&flowingObjectTable=' + __flowingObjectTable + '&flowingObjectShardingId=1&attachmentTypeEnumId=1&attachmentFormatEnumId=1&shardingId=1&activityInstanceId=1&taskInstanceId=1&',
//            'buttonText': '<span class=""><i class="fa fa-cloud-upload" style="margin-right:10px"></i>上传文件</span>',
//            'type': 'get',
//            'auto': true,
//            'fileObjName': 'uploadFiles',
//            'multi': true,
//            'simUploadLimit': 10,
//            'successTimeout': 1800,
//            'width':100,
//            'onSelect': function (event, ID, fileObj) {
////                    alert('上传中...');
//            },
//            'onFallback': function () {
//                alert("您未安装FLASH控件，无法上传图片！请安装FLASH控件后再试。");
//            },
//            'onUploadStart': function (fileObj) {
//                if (fileObj.size > 12582912) {
//                    alert("您上传的文件大于12M,请重新上传！");
//                    $('#' + __file_uploader).uploadify('stop');
//                    $('#' + __file_uploader).uploadify('cancel', '*');
//                    return;
//                }
//            },
//            'onUploadSuccess': function (file, data, response) {
//                var returnData = eval('(' + data + ')');
//                if (returnData.success) {
//                    debugger;
//                    var __attachment_data = eval('(' + returnData.data + ')');
//                    var __attachmentName = __attachment_data.attachmentName;
//                    var __attachmentSize = __attachment_data.attachmentSize;
//                    if(__attachmentSize == null){
//                        __attachmentSize = '0KB';
//                    } else {
//                        var __size = window.parseFloat(__attachmentSize/1024).toFixed(2);
//                        if (__size == 0) {
//                            __attachmentSize = __attachmentSize + 'B';
//                        } else if(__size < 1024){
//                            __attachmentSize = __size + 'KB';
//                        } else {
//                            __size = window.parseFloat(__size/1024).toFixed(2);
//                            __attachmentSize = __size + 'MB';
//                        }
//                    }
//                    __file_uploader_file_list_container.append($('<li id="__attachment_' + __attachment_data.attachmentId + '">' +
//                        '<a href="' + _PATH + '/attachment.do?method=download&flowingFlag=Y&jsonArr=[{attachmentId:' + __attachment_data.attachmentId + '}]">' + __attachmentName + '</a>' +
//                        '('+__attachmentSize+')&nbsp;'+
//                        '<a onclick="__metar_delete_file(' + __attachment_data.attachmentId + ' , \'' + __attachmentName + '\')">删除</a>' +
//                        '</li>'));
//                    if (__callback) {
//                        eval(__callback + '()');
//                    }
//                } else {
//                    alert('上传失败，请重新上传。');
//                }
//            }
//        })
//    } else {
//        $('#' + __file_uploader).hide();
//    }
//
//}
/*---删除附件---*/
function __metar_delete_file(__attachmentId, __attachmentName, _flowingObjectId, _flowingObjectTable) {
    if (confirm('确认要删除附件' + __attachmentName + '?')) {
        $.ajax({
            url: _PATH + '/attachment.do?method=delete',
            type: 'POST',
            async: true,
            dataType: 'json',
            data: {
                flowingFlag: 'Y',
                jsonArr: '[{attachmentId:' + __attachmentId + ', flowingObjectId:\'' + _flowingObjectId + '\' , flowingObjectTable:\'' + _flowingObjectTable + '\'}]'
            },
            success: function (response) {
                if (response.success) {
                    $('#__attachment_' + __attachmentId).remove();
                } else {
                    iMsg('删除附件失败，请重试');
                }
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                iMsg('删除附件失败，请重试');
            }
        })
    }

}

/*
 遍历所有class为__metar_enum的select控件，然后加载下拉数据
 */
function __loadValues4Select() {
    var selects = $('.__metar_enum');
    for (var i = 0; i < selects.length; i++) {
        var select = selects[i];
        var __select_id = $(select).attr('id');
        var __enumCode = $(select).attr('enumCode');
        var __emptyMsg = $(select).attr('emptyMsg');
        if (__enumCode) {
            if (__emptyMsg) {
                __loadEnumValuesByType(__select_id, __enumCode, __emptyMsg);
            } else {
                __loadEnumValuesByType(__select_id, __enumCode);
            }
        }
    }
}

/*
 加载枚举下拉数据
 */
function __loadEnumValuesByType(__select_id, __enumCode, __emptyMsg) {
    $.ajax({
        url: _PATH + '/commEnumController.do?method=getEnumByType',
        type: 'POST',
        async: true,
        dataType: 'json',
        data: {enumItemCode: __enumCode, orgId: '', status: 1},
        success: function (response) {
            if (response) {
                var __select_value = $('#' + __select_id).attr('enumValue');
                if (__emptyMsg) {
                    document.getElementById(__select_id).options.add(new Option(__emptyMsg, -1));
                }
                for (var i = 0; i < response.length; i++) {
                    var emunEntity = response[i];
                    if (emunEntity.enumValueId == __select_value) {
                        document.getElementById(__select_id).options.add(new Option(emunEntity.enumValueName, emunEntity.enumValueId));
                    } else {
                        document.getElementById(__select_id).options.add(new Option(emunEntity.enumValueName, emunEntity.enumValueId));
                    }
                }
                $('#' + __select_id).val(__select_value);
//                __init_query_select($('#' + __select_id));
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {

        }
    })
}

/*---返回上一步---*/
function __exitFromFrame() {
    location.href = _PATH + _winParams.__returnUrl;
}

/*---弹出窗口---*/
function __open_metar_window(__window, __title, __width, __height, isParent, __callback) {
//    __open_metar_window_parent(__window , __title , __width , __height , __callback);
//    return;
    try {
        if ("N" == isParent) {
            __open_metar_window_parent(__window, __title, __width, __height, __callback);
        } else {
            if (parent.__open_metar_window_parent) {
                parent.__open_metar_window_parent(__window, __title, __width, __height, __callback);
            } else {
                __open_metar_window_parent(__window, __title, __width, __height, __callback);
            }
        }
    } catch (e) {
        __open_metar_window_parent(__window, __title, __width, __height, __callback);
    }

}
function __open_metar_window_parent(__window, __title, __width, __height, __callback) {
    var __window_window = $('#__' + __window + '_container');
    if (!document.getElementById('__' + __window + '_container')) {
        __window_window = $('<div id="__' + __window + '_container" class="__link_dialog_container modal"></div>');
        var __window_header = $('<div class="modal-header">' + __title + '</div>');
        var __window_header_close_btn = $('<div class="close">×</div>');
        __window_header_close_btn.click(function () {
            $(__window_window).modal('hide');
        });
        __window_header.append(__window_header_close_btn);
        var __window_body = $('<div class="modal-body"></div>');
        __window_window.append(__window_header);
        __window_window.append(__window_body);
        if (__width) {
            __window_window.css('width', __width);
            __window_window.css('margin-left', -__width / 2);
        }
        if (__height) {
            __window_window.css('height', __height);
            __window_body.css('height', __height - 115);
            __window_window.css('margin-top', -__height / 2);
        }
        __window_window.modal({backdrop: 'static', show: true});
        $('body').append(__window_window);
        __callback(__window_body);
        drag(__window_window.get(0), __window_header.get(0));
    }
    __window_window.modal({backdrop: 'static', show: true});
    return __window_window;
}

/*---改变所在iframe的大小---*/
function __resize_up_iframe() {
//    alert(document.body.scrollHeight + ',' + document.documentElement.clientHeight);
    $(window.parent.document).find('#' + _parent_iframe).css('height', document.body.scrollHeight + 20);
}

/*
 显示等待提示
 */
function __show_metar_loading() {
//    $('#__progress_bar').stop().css('width' , '1px');
////    alert(123);
//    setTimeout(function(){
//        $('#__progress_bar').stop().animate({width:'100%'} , 5000) ;
//    } , 100)
//    if ($('#__metar_progress_box').length > 0) {
//        $('#__metar_progress_box').stop().show();
//    } else {
//        var __metar_progress_box = $('<div id="__metar_progress_box" class="__metar_progress_box"></div>');
//        __metar_progress_box.append('<img src="' + _PATH + '/base/_resources/loading.gif"/>');
//        $('body').append(__metar_progress_box);
//    }
//     showILoading();
}

/*
 隐藏等待提示
 */
function __hide_metar_loading() {
    //$('#__metar_progress_box').stop().hide();
    closeILoading();
}

/*
 弹出树
 @param  __open_btn_id       弹出树的触发元素ID
 @param  __tree_type         弹出树的类型（1、组织树 2、人员树   3、派发树）
 @param  __tree_title        弹出树的标题
 @param  __callback          选择数据后，回调函数
 @param  __root_org_id       根节点组织ID
 @param  __root_as_company   是否将根节点组织ID所属公司作为根节点(0、是，获取根节点组织ID所属公司，作为根节点    1、否)，默认为0
 @param  __select_type       选择类型（checkbox、多选（默认）    radio、单选）
 @param  __option_type       选择类型（1、组织树和人员树互斥    2、组织树，人员树，常用组织和常用人员树可以同时选择）（默认）
 @param  isParent            是否在父窗体中弹出窗口 Y是 N或者null 否(默认)
 */
function __open_tree(__open_btn_id, __tree_type, __tree_title, __callback, __root_org_id, __root_as_company, __select_type, __option_type,isParent) {
    __open_tree_customParam(__open_btn_id, __tree_type, __tree_title, __callback, __select_type, __option_type, {
        type: __tree_type,
        rootOrgId: __root_org_id,
        rootAsCompany: __root_as_company
    },isParent);
}

/**
 *
 * @param __settings{
 *     open_btn_id: 弹出树的触发元素ID
 *     tree_type:   弹出树的类型（1、组织树 2、人员树   3、派发树  4、专业处室组织树   5、专业处室人员树）
 *     tree_title:  弹出树的标题
 *     callback:    选择数据后，回调函数
 *     select_type: 选择类型（checkbox、多选（默认）    radio、单选）
 *     root_org_id: 根节点组织ID
 *     root_as_company:是否将根节点组织ID所属公司作为根节点(0、是，获取根节点组织ID所属公司，作为根节点    1、否)，默认为0
 * }
 *
 * @private
 */
function __open_tree_config(__settings) {
    __open_tree_customParam(__settings.open_btn_id, __settings.tree_type, __settings.tree_title, __settings.callback, __settings.select_type, {
        type: __settings.tree_type,
        rootOrgId: __settings.root_org_id,
        rootAsCompany: __settings.root_as_company
    });
}

//不确定的查询数据参数，放入一个对象
function __open_tree_customParam(__open_btn_id, __tree_type, __tree_title, __callback, __select_type, __option_type, __dataParameter, isParent) {
    if (__select_type != 'radio') {
        __select_type = 'checkbox';
    }
    if (__option_type != '1') {
        __option_type = '2';
    }
    if (!__dataParameter.rootOrgId) {
        __dataParameter.rootOrgId = '';
    }
    if (!__dataParameter.rootAsCompany) {
        __dataParameter.rootAsCompany = 0;
    }
    __dataParameter.type = __tree_type;

    var __dialog_height = document.body.scrollHeight > 450 ? 450 : document.body.scrollHeight;
    __dialog_height = document.documentElement.clientHeight > 450 ? 450 : document.documentElement.clientHeight;
    try {
        if (parent.__open_tree) {
            __dialog_height = parent.document.body.scrollHeight > 450 ? 450 : parent.document.body.scrollHeight;
            __dialog_height = parent.document.documentElement.clientHeight > 450 ? 450 : parent.document.documentElement.clientHeight;
        }
    } catch (e) {
        //alert(e);
    }

    __open_metar_window(__open_btn_id, __tree_title, 600, __dialog_height, isParent, function (__window) {
            var __ztree_panel_0 = $('<ul id="' + __open_btn_id + '_tree" class="ztree"></ul>');
            var __btns_div = $('<div class="__dialog_panel_btns"></div>');
            if (__tree_type == 1 || __tree_type == 4) {
                var __ztree_0 = __init_metar_org_common_tree_panel(__ztree_panel_0, __select_type, __option_type, __dataParameter, __dialog_height);
                //var __ztree_0 = __init_metar_org_tree_panel(__window, __ztree_panel_0, __select_type, __dataParameter, __dialog_height, __dialog_height);
                var __btn_submit = $('<span class="btn btn-info">确定</span>');
                __btn_submit.click(function () {
                    var __ztree_Orgs = __ztree_0[0].getCheckedNodes();
                    var __return_checked_nodes = new Array();
                    if (__ztree_0.length > 1) {
                        for (var i = 1; i < __ztree_0.length; i++) {
                            var selectedOrgs = __ztree_0[i].getCheckedNodes();
                            if (selectedOrgs.length > 0) {
                                __ztree_Orgs = __ztree_Orgs.concat(selectedOrgs);
                            }
                        }
                    }

                    if (__ztree_Orgs.length == 0) {
                        iMsg('请选择');
                    } else {
                        __window.parent().modal('hide');
                        for (var i = 0; i < __ztree_Orgs.length; i++) {
                            var __return_org = __ztree_Orgs[i];
                            if (!__return_org.isParent) {
                                __return_checked_nodes.push(__return_org);
                            } else if (!__return_org.children) {
                                __return_org.isParent = false;
                                __return_checked_nodes.push(__return_org);
                            } else if (__return_org.children.length == 0) {
                                __return_org.isParent = false;
                                __return_checked_nodes.push(__return_org);
                            }
                        }
                        __callback(filterArray(__return_checked_nodes));
                    }
                });
                var __btn_clear = $('<span class="btn btn-default">清空</span>');
                __btn_clear.click(function () {
                    for (var j = 0; j < __ztree_0.length; j++) {
                        var __ztree_person = __ztree_0[j];
                        __ztree_person.checkAllNodes(false);
                    }
                    //__window.parent().modal('hide');
                    __callback('');
                });
                var __btn_close = $('<span class="btn btn-default">关闭</span>');
                __btn_close.click(function () {
                    __window.parent().modal('hide');
                });
                __btns_div.append(__btn_submit);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_clear);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_close);
                __window.parent().append(__btns_div);
            } else if (__tree_type == 2 || __tree_type == 5) {
                var __ztree_0 = __init_metar_person_common_tree_panel(__ztree_panel_0, __select_type, __option_type, __dataParameter, __dialog_height);
                //var __ztree_0 = __init_metar_person_tree_panel(__window, __ztree_panel_0, __select_type, __dataParameter, null, __dialog_height);
                var __btn_submit = $('<span class="btn btn-info">确定</span>');
                __btn_submit.click(function () {
                    var __ztree_Persons = __ztree_0[0];
                    var __ztree_PersonsCommon;
                    var __selectedPersons = __ztree_Persons.getCheckedNodes();
                    var __selectedSearchPersons = __ztree_Persons.getSelectedPersons();

                    if (__ztree_0.length > 1) {
                        for (var i = 1; i < __ztree_0.length; i++) {
                            var selectedPersons = __ztree_0[i].getCheckedNodes();
                            if (selectedPersons.length > 0) {
                                __selectedPersons = __selectedPersons.concat(selectedPersons);
                            }
                        }
                    }
                    if (__selectedSearchPersons.length > 0) {
                        __selectedPersons = __selectedPersons.concat(__selectedSearchPersons);
                    }
                    if (__selectedPersons.length == 0) {
                        iMsg('请选择人员');
                        return;
                    } else {
                        var __return_checked_nodes = new Array();
                        for (var i = 0; i < __selectedPersons.length; i++) {
                            var __return_person = __selectedPersons[i];
                            if (!__return_person.isParent) {
                                __return_checked_nodes.push(__return_person);
                            }
                        }
                        __callback(filterArray(__return_checked_nodes));
                    }
                    __window.parent().modal('hide');
                });
                var __btn_clear = $('<span class="btn btn-default">清空</span>');
                __btn_clear.click(function () {
                    for (var j = 0; j < __ztree_0.length; j++) {
                        var __ztree_person = __ztree_0[j];
                        __ztree_person.checkAllNodes(false);
                    }
                    //var __ztree_Persons = __ztree_0[0];
                    //var __selectedPersons = __ztree_Persons.getCheckedNodes(true);
                    //for (var j = 0; j < __selectedPersons.length; j++) {
                    //    __selectedPersons[j].checked=false;
                    //}
                    __init_ztree_panel(__ztree_panel_0, __select_type, __dataParameter);
                    //__window.parent().modal('hide');
                    __callback('');
                });
                var __btn_close = $('<span class="btn btn-default">关闭</span>');
                __btn_close.click(function () {
                    __window.parent().modal('hide');
                });
                __btns_div.append(__btn_submit);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_clear);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_close);
                __window.parent().append(__btns_div);
            } else if (__tree_type == 3) {
                var __ztree_array = __init_metar_dispatch_tree_panel(__ztree_panel_0, __select_type, __option_type, __dataParameter, __dialog_height);
                var __btn_submit = $('<span class="btn btn-info">确定</span>');
                __btn_submit.click(function () {
                    var __ztree_org = __ztree_array[0];

                    var __selectedOrgs = __ztree_org.getCheckedNodes();
                    var __org_null = true;
                    var __select_person_null = true;
                    var __search_person_null = true;
                    var __return_persons = new Array();
                    if (__selectedOrgs.length > 0) {
                        __org_null = false;
                    }
                    if (__ztree_array.length > 1) {
                        for (var j = 1; j < __ztree_array.length; j++) {
                            var __ztree_person = __ztree_array[j];
                            var __selectedPersons = __ztree_person.getCheckedNodes();
                            var __selectedSearchPersons = __ztree_person.getSelectedPersons();
                            if (__ztree_person.search) {
                                if (__selectedSearchPersons.length > 0) {
                                    __select_person_null = false;
                                    __return_persons = __return_persons.concat(__selectedSearchPersons);
                                }
                            } else {
                                if (__selectedPersons.length > 0) {
                                    __search_person_null = false;
                                    __return_persons = __return_persons.concat(__selectedPersons);
                                }
                            }
                        }
                    }
                    if (__org_null && __select_person_null && __search_person_null) {
                        iMsg('请选择组织或者人员');
                        return;
                    } else {
                        var __return_nodes = __selectedOrgs.concat(__return_persons);
                        var __return_checked_nodes = new Array();
                        for (var i = 0; i < __return_nodes.length; i++) {
                            var __return_node = __return_nodes[i];
                            if (!__return_node.isParent) {
                                __return_checked_nodes.push(__return_node);
                            }
                        }
                        __callback(filterArray(__return_checked_nodes));
                    }
                    __window.parent().modal('hide');
                });
                var __btn_clear = $('<span class="btn btn-default">清空</span>');
                __btn_clear.click(function () {

                    for (var j = 0; j < __ztree_array.length; j++) {
                        var __ztree_person = __ztree_array[j];
                        __ztree_person.checkAllNodes(false);
                    }

                    //__window.parent().modal('hide');
                    __callback('');
                });
                var __btn_close = $('<span class="btn btn-default">关闭</span>');
                __btn_close.click(function () {
                    __window.parent().modal('hide');
                });
                __btns_div.append(__btn_submit);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_clear);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_close);
                __window.parent().append(__btns_div);
            } else if (__tree_type == 9) {
                var __ztree_array = __init_metar_person9_tree_panel(__ztree_panel_0, __select_type, __option_type, __dataParameter, __dialog_height);
                var __btn_submit = $('<span class="btn btn-info">确定</span>');
                __btn_submit.click(function () {
                    var __ztree_Persons = __ztree_array;
                    var __selectedSearchPersons = __ztree_Persons.getSelectedPersons();

                    if (__selectedSearchPersons.length == 0) {
                        iMsg('请选择人员');
                        return;
                    } else {
                        var __return_checked_nodes = new Array();
                        for (var i = 0; i < __selectedSearchPersons.length; i++) {
                            var __return_person = __selectedSearchPersons[i];
                            __return_checked_nodes.push(__return_person);
                        }
                        __callback(filterArray(__return_checked_nodes));
                    }
                    __window.parent().modal('hide');
                });
                var __btn_clear = $('<span class="btn btn-default">清空</span>');
                __btn_clear.click(function () {
                    for (var j = 0; j < __ztree_0.length; j++) {
                        var __ztree_person = __ztree_0[j];
                        __ztree_person.checkAllNodes(false);
                    }
                    //__window.parent().modal('hide');
                    __callback('');
                });
                var __btn_close = $('<span class="btn btn-default">关闭</span>');
                __btn_close.click(function () {
                    __window.parent().modal('hide');
                });
                __btns_div.append(__btn_submit);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_clear);
                __btns_div.append('&nbsp;');
                __btns_div.append(__btn_close);
                __window.parent().append(__btns_div);
            }

            function __init_metar_org_tree_panel(__window_container, __ztree_panel, __select_type, __dataParameter, __height, __dialog_height) {
//                __dataParameter.type = 1;
                if (__height) {
                    __ztree_panel.css('height', __height - 100);
                }
                //__ztree_panel.css('overflow', 'auto');
                __window_container.append(__ztree_panel);
                var __ztree_0 = __init_ztree_panel(__ztree_panel, __select_type, __dataParameter);
                return __ztree_0;
            }

            function __init_metar_org_common_tree_panel(__ztree_org_panel, __select_type, __option_type, ___dataParameter, __dialog_height) {
                var __ztree_array = new Array();
                var __ztree_org_container_panel = $('<div></div>');
                __ztree_org_container_panel.css('height', __dialog_height - 130);
                __ztree_org_container_panel.css('overflow', 'auto');
                var __ztree_pserson_container_panel;
                var __ztree_common_person_container_panel;
                var __ztree_common_org_container_panel;
                var __ztree_tab_panel = $('<ul class="nav nav-tabs"></ul>');
                __ztree_tab_panel.css('margin-top', -10);
                var __ztree_tab_btn_0 = $('<li role="presentation" class="active"><a>组织树</a></li>');
                var __ztree_tab_btn_1 = $('<li role="presentation"><a>常用群组</a></li>');
                __ztree_tab_panel.append(__ztree_tab_btn_0);
                __ztree_tab_panel.append(__ztree_tab_btn_1);
                __ztree_tab_btn_0.click(function () {
                    __active_tab($(this));
                    __active_tree_panel(__ztree_org_container_panel);
                    if (__option_type == '1') {
                        for (var j = 0; j < __ztree_array.length; j++) {
                            var __ztree_person = __ztree_array[j];
                            __ztree_person.checkAllNodes(false);
                        }
                    }
                    //__window.parent().modal('hide');
                    __callback('');
                });
                __ztree_tab_btn_1.click(function () {
                    //if (!__ztree_common_person_container_panel) {
                    $("#__ztree_common_person_container_panel").remove();
                    __ztree_common_person_container_panel = $('<div id="__ztree_common_person_container_panel"></div>');
                    __ztree_common_person_container_panel = $('<div></div>');
                    var __ztree_pserson_panel = $('<ul id="' + __open_btn_id + '_tree_tab_2" class="ztree"></ul>');
                    __ztree_common_person_container_panel.append(__ztree_pserson_panel);
                    __window.append(__ztree_common_person_container_panel);
                    ___dataParameter.type = "1";
                    var __ztree_2 = __init_metar_common_tree_panel(__ztree_common_person_container_panel, __ztree_pserson_panel, __select_type, ___dataParameter, __dialog_height - 155, __dialog_height - 16);
                    for (var j = 0; j < __ztree_array.length; j++) {
                        var __ztree_person = __ztree_array[j];
                        if (__ztree_person.checkAllOrgNodes)
                            __ztree_person.checkAllOrgNodes(false);
                    }
                    __ztree_array.push(__ztree_2);
                    //}
                    __active_tab($(this));
                    __active_tree_panel(__ztree_common_person_container_panel);
                    if (__option_type == '1') {
                        for (var j = 0; j < __ztree_array.length; j++) {
                            var __ztree_person = __ztree_array[j];
                            __ztree_person.checkAllNodes(false);
                        }
                    }
                    return __ztree_common_person_container_panel;
                });
                __window.append(__ztree_tab_panel);
                function __active_tab(__ztree_tab_btn) {
                    __ztree_tab_panel.find('li.active').removeClass("active");
                    __ztree_tab_btn.addClass("active");
                }

                function __active_tree_panel(__tree_panel) {
                    var __tree_panels = __window.children();
                    for (var x = 1; x < __tree_panels.length; x++) {
                        $(__tree_panels[x]).hide();
                    }
                    $(__tree_panel).show();
                }

                __ztree_org_container_panel.append(__ztree_org_panel);
                __window.append(__ztree_org_container_panel);
                __dataParameter.type = 1;
                var __ztree_0 = __init_metar_org_tree_panel(__ztree_org_container_panel, __ztree_org_panel, __select_type, ___dataParameter, null, null);
                __ztree_array.push(__ztree_0);
                return __ztree_array;
            }

            function __init_metar_person_common_tree_panel(__ztree_org_panel, __select_type, __option_type, ___dataParameter, __dialog_height) {
                var __ztree_array = new Array();
                var __ztree_org_container_panel = $('<div></div>');
                __ztree_org_container_panel.css('height', __dialog_height - 125);
                //__ztree_org_container_panel.css('height', __dialog_height - 125);
                __ztree_org_container_panel.css('overflow', 'auto');
                var __ztree_common_person_container_panel;
                var __ztree_tab_panel = $('<ul class="nav nav-tabs"></ul>');
                __ztree_tab_panel.css('margin-top', -10);
                var __ztree_tab_btn_0 = $('<li role="presentation"><a>人员树</a></li>');
                var __ztree_tab_btn_1 = $('<li role="presentation"><a>常用联系人</a></li>');
                __ztree_tab_panel.append(__ztree_tab_btn_0);
                __ztree_tab_panel.append(__ztree_tab_btn_1);
                __ztree_tab_btn_0.click(function () {
                    __active_tab($(this));
                    __active_tree_panel(__ztree_org_container_panel);
                    if (__option_type == '1') {
                        for (var j = 0; j < __ztree_array.length; j++) {
                            var __ztree_person = __ztree_array[j];
                            __ztree_person.checkAllNodes(false);
                        }
                    }
                    //__window.parent().modal('hide');
                    __callback('');
                });
                __ztree_tab_btn_1.click(function () {
                    //if (!__ztree_common_person_container_panel) {
                    $("#__ztree_common_person_container_panel").remove();
                    __ztree_common_person_container_panel = $('<div id="__ztree_common_person_container_panel"></div>');
                    var __ztree_pserson_panel = $('<ul id="' + __open_btn_id + '_tree_tab_3" class="ztree"></ul>');
                    __ztree_common_person_container_panel.append(__ztree_pserson_panel);
                    __window.append(__ztree_common_person_container_panel);
                    ___dataParameter.type = "2";
                    var __ztree_3 = __init_metar_common_tree_panel(__ztree_common_person_container_panel, __ztree_pserson_panel, __select_type, ___dataParameter, __dialog_height - 155, __dialog_height - 16);
                    for (var j = 0; j < __ztree_array.length; j++) {
                        var __ztree_person = __ztree_array[j];
                        if (__ztree_person.checkAllPersonNodes)
                            __ztree_person.checkAllPersonNodes(false);
                    }
                    __ztree_array.push(__ztree_3);
                    //}
                    __active_tab($(this));
                    __active_tree_panel(__ztree_common_person_container_panel);
                    if (__option_type == '1') {
                        for (var j = 0; j < __ztree_array.length; j++) {
                            var __ztree_person = __ztree_array[j];
                            __ztree_person.checkAllNodes(false);
                        }
                    }
                    return __ztree_common_person_container_panel;
                });
                __window.append(__ztree_tab_panel);
                function __active_tab(__ztree_tab_btn) {
                    __ztree_tab_panel.find('li.active').removeClass("active");
                    __ztree_tab_btn.addClass("active");
                }

                function __active_tree_panel(__tree_panel) {
                    var __tree_panels = __window.children();
                    for (var x = 1; x < __tree_panels.length; x++) {
                        $(__tree_panels[x]).hide();
                    }
                    $(__tree_panel).show();
                }

                __ztree_org_container_panel.append(__ztree_org_panel);
                __window.append(__ztree_org_container_panel);
                __dataParameter.type = 2;
                var __ztree_0 = __init_metar_person_tree_panel(__ztree_org_container_panel, __ztree_org_panel, __select_type, ___dataParameter, null, __dialog_height);
                __ztree_array.push(__ztree_0);
                return __ztree_array;
            }

            function __init_metar_person_tree_panel(__window_container, __ztree_panel, __select_type, __dataParameter, __height, __dialog_height) {
//                __dataParameter.type = 2;
                var __ztree_0 = {};
                var __ztree_query_result_data;
                var __ztree_query_panel = $('<div class="input-group"></div>');
                //__ztree_query_panel.append($('<div class="Vertip"><i class="fa fa-check"></i></div>'));
                //__ztree_query_panel.append($('<a class="deldatabtn"><i class="fa fa-check"></i></a>'));
                //__ztree_query_panel.append($('<a class="querydatabtn"><i class="fa fa-check"></i></a>'));
                //<a id="__dispatch_tree_mainSent" class="seldatabtn"><i class="fa fa-user"></i></a>

                var __ztree_query_input = $('<input type="text" class="searchtext">');
                //var __ztree_query_clear_btn = $('<span class="input-group-addon glyphicon glyphicon-remove"></span>');
                //var __ztree_query_btn = $('<span class="input-group-addon glyphicon glyphicon-search"></span>');
                var __ztree_query_clear_btn = $('<a class="deldatabtn"><i class="fa fa-remove"></i></a>');
                //var __ztree_query_btn = $('<a class="querydatabtn"><i class="fa fa-search"></i></a>');
                var __ztree_query_result_panel = $('<div id="1111" class=""></div>');
                __ztree_query_result_panel.css('height', __dialog_height - 170);
                __ztree_query_result_panel.css('overflow', 'auto');
                __ztree_query_result_panel.css('margin-top', 5);
                __ztree_query_result_panel.hide();
                //__ztree_query_btn.click(function () {
                //    __ztree_0.search = true;
                //    var __ztree_query_keyword = __ztree_query_input.val();
                //    __ztree_query_keyword = trim(__ztree_query_keyword);
                //    __ztree_query_input.val(__ztree_query_keyword);
                //    if (__ztree_query_keyword == '') {
                //        __ztree_query_input.addClass('metar_not_null');
                //        __ztree_query_input.focus();
                //        return;
                //    } else {
                //        __ztree_query_input.removeClass('metar_not_null');
                //    }
                //    __ztree_panel.hide();
                //    __ztree_query_result_panel.empty().show();
                //    $.ajax({
                //        url: _PATH + '/commTreeController.do?method=queryPerson',
                //        type: 'POST',
                //        async: true,
                //        dataType: 'json',
                //        data: {name: __ztree_query_keyword},
                //        success: function (data) {
                //            __ztree_query_result_data = data;
                //            var ____ztree_query_result_panel_table = $('<table class="table" style="table-layout:fixed"></table>');
                //            var ____ztree_query_result_panel_tbody = $('<tbody></tbody>');
                //            for (var i = 0; i < data.length; i++) {
                //                ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="checkbox" value="' + i + '" name="' + __open_btn_id + '_person_check" /></td><td style="width:40px;">' + data[i].trueName + '</td><td style="width:150px;">' + data[i].orgEntity.fullOrgName + '</td></tr>');
                //            }
                //            ____ztree_query_result_panel_table.append(____ztree_query_result_panel_tbody);
                //            __ztree_query_result_panel.empty().append(____ztree_query_result_panel_table);
                //        },
                //        error: function () {
                //            alert("error");
                //        }
                //    })
                //});
                __ztree_query_clear_btn.click(function () {
                    __ztree_query_input.val('');
                    __ztree_query_result_panel.empty().hide();
                    __ztree_panel.show();
                    __ztree_0.search = false;
                });
                __ztree_query_input.keyup(function () {
                    __ztree_0.search = true;
                    var __ztree_query_keyword = __ztree_query_input.val();
                    __ztree_query_keyword = trim(__ztree_query_keyword);
                    __ztree_query_input.val(__ztree_query_keyword);
                    if (__ztree_query_keyword == '') {
                        __ztree_query_input.addClass('metar_not_null');
                        __ztree_query_input.focus();
                        __ztree_query_result_panel.empty().hide();
                        __ztree_panel.show();
                        __ztree_0.search = false;
                        return;
                    } else {
                        __ztree_query_input.removeClass('metar_not_null');
                    }
                    __ztree_panel.hide();
                    __ztree_query_result_panel.empty().show();
                    $.ajax({
                        url: _PATH + '/commTreeController.do?method=queryPerson',
                        type: 'POST',
                        async: true,
                        dataType: 'json',
                        data: {name: __ztree_query_keyword},
                        success: function (data) {
                            __ztree_query_result_data = data;
                            var ____ztree_query_result_panel_table = $('<table class="table" style="table-layout:fixed"></table>');
                            var ____ztree_query_result_panel_tbody = $('<tbody></tbody>');
                            for (var i = 0; i < data.length; i++) {
                                ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="checkbox" value="' + i + '" name="' + __open_btn_id + '_person_check" /></td><td style="width:40px;">' + data[i].trueName + '</td><td style="width:150px;">' + data[i].orgEntity.fullOrgName + '</td></tr>');
                            }
                            ____ztree_query_result_panel_table.append(____ztree_query_result_panel_tbody);
                            __ztree_query_result_panel.empty().append(____ztree_query_result_panel_table);
                        },
                        error: function () {
                            iMsg("error");
                        }
                    })
                });

                __ztree_query_panel.append(__ztree_query_input);
                __ztree_query_panel.append(__ztree_query_clear_btn);
                //__ztree_query_panel.append(__ztree_query_btn);
                __window_container.append(__ztree_query_panel);
                __window_container.append(__ztree_query_result_panel);
                if (__height) {
                    __ztree_panel.css('height', __height);
                } else {
                    __ztree_panel.css('height', __dialog_height - 167);
                    //__ztree_panel.css('height', __dialog_height - 135);
                }
                __ztree_panel.css('overflow', 'auto');
                __window_container.append(__ztree_panel);
                __ztree_0 = __init_ztree_panel(__ztree_panel, __select_type, __dataParameter);
                __ztree_0.getSelectedPersons = function () {
                    var __return_selected_persons_array = new Array();
                    var __person_check_list = __ztree_query_result_panel.find('input');
                    for (var j = 0; j < __person_check_list.length; j++) {
                        if (__person_check_list[j].checked) {
                            var __person = __ztree_query_result_data[__person_check_list[j].value];
                            __return_selected_persons_array.push({
                                id: __person.userId,
                                userName: __person.userName,
                                label: __person.trueName
                            });
                        }
                    }
                    return __return_selected_persons_array;
                }
                __ztree_0.checkAllNodes = function (bool) {
                    var __person_check_list = __ztree_query_result_panel.find('input');
                    for (var j = 0; j < __person_check_list.length; j++) {
                        __person_check_list[j].checked = bool;
                    }
                }
                return __ztree_0;
            }

            function __init_metar_person9_tree_panel(__window_container, __ztree_panel, __select_type, __dataParameter, __height, __dialog_height) {
//                __dataParameter.type = 2;
                var __ztree_org_container_panel = $('<div></div>');
                __ztree_org_container_panel.css('height', __dialog_height - 125);
                __ztree_org_container_panel.css('overflow', 'auto');
                __window.append(__ztree_org_container_panel);

                var __ztree_0 = {};
                var __ztree_query_result_data;
                var __ztree_query_result_panel = $('<div id="1111" class=""></div>');
                __ztree_query_result_panel.css('height', __dialog_height - 170);
                __ztree_query_result_panel.css('overflow', 'auto');
                __ztree_query_result_panel.css('margin-top', 5);
                __ztree_0.search = true;
                __ztree_query_result_panel.show();
                $.ajax({
                    url: _PATH + '/commTreeController.do?method=queryOfficePerson',
                    type: 'POST',
                    async: true,
                    dataType: 'json',
                    data: {},
                    success: function (data) {
                        __ztree_query_result_data = data;
                        var ____ztree_query_result_panel_table = $('<table class="table" style="table-layout:fixed"></table>');
                        var ____ztree_query_result_panel_tbody = $('<tbody></tbody>');
                        for (var i = 0; i < data.length; i++) {
                            ____ztree_query_result_panel_tbody.append('<tr ondblclick=""><td style="width:20px;text-align:center"><input type="radio" value="' + i + '" name="' + __open_btn_id + '_person_check" /></td><td style="width:40px;">' + data[i].participantName + '</td><td style="width:150px;">' + data[i].participantType + '</td></tr>');
                        }
                        ____ztree_query_result_panel_table.append(____ztree_query_result_panel_tbody);
                        __ztree_query_result_panel.empty().append(____ztree_query_result_panel_table);
                    },
                    error: function () {
                        alert("error");
                    }
                })
                __ztree_org_container_panel.append(__ztree_query_result_panel);
                if (__height) {
                    __ztree_org_container_panel.css('height', __height - 110);
                } else {
                    __ztree_org_container_panel.css('height', __dialog_height - 167);
                    __ztree_org_container_panel.css('height', __dialog_height - 135);
                }
                //__ztree_panel.hide();
                __ztree_query_result_panel.empty().show();
                //__ztree_0 = __init_ztree_panel(__ztree_org_container_panel, __select_type, __dataParameter);
                __ztree_0.getSelectedPersons = function () {
                    var __return_selected_persons_array = new Array();
                    var __person_check_list = __ztree_query_result_panel.find('input');
                    for (var j = 0; j < __person_check_list.length; j++) {
                        if (__person_check_list[j].checked) {
                            var __person = __ztree_query_result_data[__person_check_list[j].value];
                            __return_selected_persons_array.push({
                                id: __person.participantID,
                                userName: __person.participantID,
                                label: __person.participantName
                            });
                        }
                    }
                    return __return_selected_persons_array;
                }
                return __ztree_0;
            }


            function __init_metar_common_tree_panel(__window_container, __ztree_panel, __select_type, __dataParameter, __height, __dialog_height) {
//                __dataParameter.type = 2;
                var __ztree_0 = {};
                var __ztree_query_result_data;
                if ("1" == __dataParameter.type) {
                    var __ztree_query_result_panel = $('<div id="222" class=""></div>');
                    __ztree_query_result_panel.css('height', __dialog_height - 140);
                    __ztree_query_result_panel.css('overflow', 'auto');
                    //__ztree_query_result_panel.css('margin-top', 5);
                    __ztree_panel.hide();
                    __ztree_query_result_panel.empty().show();
                    $.ajax({
                        url: _PATH + '/commTreeController.do?method=queryCommonTree',
                        type: 'POST',
                        async: true,
                        dataType: 'json',
                        data: {isorgoruser: __dataParameter.type},
                        success: function (data) {
                            __ztree_query_result_data = data;
                            var ____ztree_query_result_panel_table = $('<table class="table" style="table-layout:fixed"></table>');
                            var ____ztree_query_result_panel_tbody = $('<tbody></tbody>');
                            ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="checkbox" onclick="checkAllOrgs(this)" > 选择</td><td style="width:150px;">组织名称</td></tr>');
                            for (var i = 0; i < data.length; i++) {
                                ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center">' +
                                    //'<input type="checkbox" value="' + i + '" name="' + __open_btn_id + '_person_check" /></td>' +
                                '<input type="checkbox" value="' + i + '" name="commonOrg_checkbox" /></td>' +
                                '<td style="width:150px;">' + data[i].fullOrgName +
                                '<span id="diyBtnOrg_' + i + '" value="1:1:' + data[i].orgId + '" onclick="clickAddOrDel(this);" onfocus="this.blur();">★</span></td></tr>');
                            }
                            ____ztree_query_result_panel_table.append(____ztree_query_result_panel_tbody);
                            __ztree_query_result_panel.empty().append(____ztree_query_result_panel_table);
                        },
                        error: function () {
                            iMsg("error");
                        }
                    })
                    __window_container.empty().append(__ztree_query_result_panel);
                    __ztree_0.getCheckedNodes = function () {
                        var __return_selected_orgs_array = new Array();
                        var __org_check_list = __ztree_query_result_panel.find('input');
                        for (var j = 0; j < __org_check_list.length; j++) {
                            if (__org_check_list[j].checked) {
                                var __org = __ztree_query_result_data[__org_check_list[j].value];
                                if (__org != undefined) {
                                    __return_selected_orgs_array.push({
                                        id: __org.orgId,
                                        code: __org.orgCode,
                                        label: __org.fullOrgName,
                                        type: 1,
                                        isParent: false,
                                        fullName: __org.fullOrgName
                                    });
                                }
                            }
                        }
                        return __return_selected_orgs_array;
                    }
                    __ztree_0.checkAllNodes = function (bool) {
                        var __org_check_list = __ztree_query_result_panel.find('input');
                        for (var j = 0; j < __org_check_list.length; j++) {
                            __org_check_list[j].checked = bool;
                        }
                    }
                    __ztree_0.checkAllOrgNodes = function (bool) {
                        var __org_check_list = __ztree_query_result_panel.find('input');
                        for (var j = 0; j < __org_check_list.length; j++) {
                            __org_check_list[j].checked = bool;
                        }
                    }
                    __ztree_0.getSelectedPersons = function () {
                        return new Array();
                    }
                } else {
                    var __ztree_query_panel = $('<div class="input-group"></div>');
                    var __ztree_query_result_panel = $('<div id="333" class=""></div>');
                    __ztree_query_result_panel.css('height', __dialog_height - 140);
                    __ztree_query_result_panel.css('overflow', 'auto');
                    __ztree_query_result_panel.css('margin-top', 5);
                    __ztree_panel.hide();
                    __ztree_query_result_panel.empty().show();
                    $.ajax({
                        url: _PATH + '/commTreeController.do?method=queryCommonTree',
                        type: 'POST',
                        async: true,
                        dataType: 'json',
                        data: {isorgoruser: __dataParameter.type},
                        success: function (data) {
                            __ztree_query_result_data = data;
                            var ____ztree_query_result_panel_table = $('<table class="table" style="table-layout:fixed"></table>');
                            var ____ztree_query_result_panel_tbody = $('<tbody></tbody>');
                            if (__select_type == 'radio') {
                                ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center">选择</td><td style="width:70px;">姓名</td><td style="width:150px;">组织名称</td></tr>');
                            } else {
                                ____ztree_query_result_panel_tbody.append('<tr><td style="width:30px;text-align:center"><input type="checkbox" onclick="checkAllPersons(this)" > 选择</td><td style="width:70px;">姓名</td><td style="width:150px;">组织名称</td></tr>');
                            }
                            for (var i = 0; i < data.length; i++) {
                                if (__select_type == 'radio') {
                                    ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="radio" value="' + i + '" name="' + __open_btn_id + '_person_check" /></td><td style="width:70px;">' + data[i].trueName + '</td><td style="width:150px;">' + data[i].orgEntity.fullOrgName +
                                        //____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="radio" value="' + i + '" name="' + __open_btn_id + '_person_check" /></td><td style="width:70px;">' + data[i].trueName + '</td><td style="width:150px;">' + data[i].orgEntity.fullOrgName +
                                    '<span id="diyBtnPerson_' + i + '" value="1:2:' + data[i].userId + '" onclick="clickAddOrDel(this);" onfocus="this.blur();">★</span></td></tr>');
                                } else {
                                    //____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="checkbox" value="' + i + '" name="' + __open_btn_id + '_person_check" /></td><td style="width:70px;">' + data[i].trueName + '</td><td style="width:150px;">' + data[i].orgEntity.fullOrgName +
                                    ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="checkbox" value="' + i + '" name="commonPerson_checkbox" /></td><td style="width:70px;">' + data[i].trueName + '</td><td style="width:150px;">' + data[i].orgEntity.fullOrgName +
                                    '<span id="diyBtnPerson_' + i + '" value="1:2:' + data[i].userId + '" onclick="clickAddOrDel(this);" onfocus="this.blur();">★</span></td></tr>');
                                }
                            }
                            ____ztree_query_result_panel_table.append(____ztree_query_result_panel_tbody);
                            __ztree_query_result_panel.empty().append(____ztree_query_result_panel_table);
                        },
                        error: function () {
                            iMsg("error");
                        }
                    })
                    __window_container.append(__ztree_query_result_panel);
                    __ztree_0.getCheckedNodes = function () {
                        var __return_selected_persons_array = new Array();
                        var __person_check_list = __ztree_query_result_panel.find('input');
                        for (var j = 0; j < __person_check_list.length; j++) {
                            if (__person_check_list[j].checked) {
                                var __person = __ztree_query_result_data[__person_check_list[j].value];
                                if (__person != undefined) {
                                    __return_selected_persons_array.push({
                                        id: __person.userId,
                                        userName: __person.userName,
                                        label: __person.trueName
                                    });
                                }
                            }
                        }
                        return __return_selected_persons_array;
                    }
                    __ztree_0.checkAllNodes = function (bool) {
                        var __person_check_list = __ztree_query_result_panel.find('input');
                        for (var j = 0; j < __person_check_list.length; j++) {
                            __person_check_list[j].checked = bool;
                        }
                    }
                    __ztree_0.checkAllPersonNodes = function (bool) {
                        var __person_check_list = __ztree_query_result_panel.find('input');
                        for (var j = 0; j < __person_check_list.length; j++) {
                            __person_check_list[j].checked = bool;
                        }
                    }
                    __ztree_0.getSelectedPersons = function () {
                        return new Array();
                    }
                }

                return __ztree_0;
            }


            function __init_metar_dispatch_tree_panel(__ztree_org_panel, __select_type, __option_type, ___dataParameter, __dialog_height) {
                var __ztree_array = new Array();
                var __ztree_org_container_panel = $('<div></div>');
                __ztree_org_container_panel.css('height', __dialog_height - 125);
                __ztree_org_container_panel.css('overflow', 'auto');
                var __ztree_pserson_container_panel;
                var __ztree_common_person_container_panel;
                var __ztree_common_org_container_panel;
                var __ztree_tab_panel = $('<ul class="nav nav-tabs"></ul>');
                __ztree_tab_panel.css('margin-top', -10);
                var __ztree_tab_btn_0 = $('<li role="presentation" class="active"><a>组织树</a></li>');

                var __ztree_tab_btn_1 = $('<li role="presentation"><a>人员树</a></li>');
                var __ztree_tab_btn_2 = $('<li role="presentation"><a>常用群组</a></li>');
                var __ztree_tab_btn_3 = $('<li role="presentation"><a>常用联系人</a></li>');
                __ztree_tab_panel.append(__ztree_tab_btn_0);
                __ztree_tab_panel.append(__ztree_tab_btn_1);
                __ztree_tab_panel.append(__ztree_tab_btn_2);
                __ztree_tab_panel.append(__ztree_tab_btn_3);
                __ztree_tab_btn_0.click(function () {
                    __active_tab($(this));
                    __active_tree_panel(__ztree_org_container_panel);
                    if (__option_type == '1') {
                        for (var j = 0; j < __ztree_array.length; j++) {
                            var __ztree_person = __ztree_array[j];
                            __ztree_person.checkAllNodes(false);
                        }
                    }
                    //__window.parent().modal('hide');
                    __callback('');
                });
                __ztree_tab_btn_1.click(function () {
                    if (!__ztree_pserson_container_panel) {
                        __ztree_pserson_container_panel = $('<div></div>');
                        var __ztree_pserson_panel = $('<ul id="' + __open_btn_id + '_tree_tab_1" class="ztree"></ul>');
                        __ztree_pserson_container_panel.append(__ztree_pserson_panel);
                        __window.append(__ztree_pserson_container_panel);
                        __dataParameter.type = 2;
                        var __ztree_1 = __init_metar_person_tree_panel(__ztree_pserson_container_panel, __ztree_pserson_panel, __select_type, ___dataParameter, __dialog_height - 155, __dialog_height - 16);
                        __ztree_array.push(__ztree_1);
                    }
                    __active_tab($(this));
                    __active_tree_panel(__ztree_pserson_container_panel);
                    if (__option_type == '1') {
                        for (var j = 0; j < __ztree_array.length; j++) {
                            var __ztree_person = __ztree_array[j];
                            __ztree_person.checkAllNodes(false);
                        }
                    }
                    //__window.parent().modal('hide');
                    __callback('');
                    return __ztree_pserson_container_panel;
                });
                __ztree_tab_btn_2.click(function () {
                    //if (!__ztree_common_person_container_panel) {
                    $("#__ztree_common_person_container_panel").remove();
                    __ztree_common_person_container_panel = $('<div id="__ztree_common_person_container_panel"></div>');
                    var __ztree_pserson_panel = $('<ul id="' + __open_btn_id + '_tree_tab_2" class="ztree"></ul>');
                    __ztree_common_person_container_panel.append(__ztree_pserson_panel);
                    __window.append(__ztree_common_person_container_panel);
                    ___dataParameter.type = "1";
                    var __ztree_2 = __init_metar_common_tree_panel(__ztree_common_person_container_panel, __ztree_pserson_panel, __select_type, ___dataParameter, __dialog_height - 155, __dialog_height - 16);
                    for (var j = 0; j < __ztree_array.length; j++) {
                        var __ztree_person = __ztree_array[j];
                        if (__ztree_person.checkAllOrgNodes)
                            __ztree_person.checkAllOrgNodes(false);
                    }
                    __ztree_array.push(__ztree_2);
                    //}
                    __active_tab($(this));
                    __active_tree_panel(__ztree_common_person_container_panel);
                    if (__option_type == '1') {
                        for (var j = 0; j < __ztree_array.length; j++) {
                            var __ztree_person = __ztree_array[j];
                            __ztree_person.checkAllNodes(false);
                        }
                    }
                    return __ztree_common_person_container_panel;
                });
                __ztree_tab_btn_3.click(function () {
                    //if (!__ztree_common_org_container_panel) {
                    $("#__ztree_common_org_container_panel").remove();
                    __ztree_common_org_container_panel = $('<div id="__ztree_common_org_container_panel"></div>');
                    var __ztree_pserson_panel = $('<ul id="' + __open_btn_id + '_tree_tab_3" class="ztree"></ul>');
                    __ztree_common_org_container_panel.append(__ztree_pserson_panel);
                    __window.append(__ztree_common_org_container_panel);
                    ___dataParameter.type = "2";
                    var __ztree_3 = __init_metar_common_tree_panel(__ztree_common_org_container_panel, __ztree_pserson_panel, __select_type, ___dataParameter, __dialog_height - 155, __dialog_height - 16);
                    for (var j = 0; j < __ztree_array.length; j++) {
                        var __ztree_person = __ztree_array[j];
                        if (__ztree_person.checkAllPersonNodes)
                            __ztree_person.checkAllPersonNodes(false);
                    }
                    __ztree_array.push(__ztree_3);
                    //}
                    __active_tab($(this));
                    __active_tree_panel(__ztree_common_org_container_panel);
                    if (__option_type == '1') {
                        for (var j = 0; j < __ztree_array.length; j++) {
                            var __ztree_person = __ztree_array[j];
                            __ztree_person.checkAllNodes(false);
                        }
                    }
                    return __ztree_common_org_container_panel;
                });
                __window.append(__ztree_tab_panel);
                function __active_tab(__ztree_tab_btn) {
                    __ztree_tab_panel.find('li.active').removeClass("active");
                    __ztree_tab_btn.addClass("active");
                }

                function __active_tree_panel(__tree_panel) {
                    var __tree_panels = __window.children();
                    for (var x = 1; x < __tree_panels.length; x++) {
                        //添加清除选中项的逻辑
                        $(__tree_panels[x]).hide();
                    }
                    $(__tree_panel).show();

                }

                __ztree_org_container_panel.append(__ztree_org_panel);
                __window.append(__ztree_org_container_panel);
                if (__dataParameter.type == 6) {
                    __dataParameter.type = 6;
                } else {
                    __dataParameter.type = 1;
                }
                var __ztree_0 = __init_metar_org_tree_panel(__ztree_org_container_panel, __ztree_org_panel, __select_type, ___dataParameter, null, null);
                __ztree_array.push(__ztree_0);
                //添加点击事件
                return __ztree_array;
            }

            /* 2015-10-11 之前的算法
             var __ztree_panel_1;
             var __ztree_0;
             var __ztree_1;
             var __btns_div = $('<div class="__dialog_panel_btns"></div>');
             if(__tree_type == 3){
             var __ztree_tab_panel = $('<ul class="nav nav-tabs"></ul>');
             __ztree_tab_panel.css('margin-top' , -10);
             var __ztree_tab_btn_0 = $('<li role="presentation" class="active"><a>组织树</a></li>');
             var __ztree_tab_btn_1 = $('<li role="presentation"><a>人员树</a></li>');
             __ztree_tab_panel.append(__ztree_tab_btn_0);
             __ztree_tab_panel.append(__ztree_tab_btn_1);
             __ztree_tab_btn_0.click(function(){
             __active_tab($(this));
             __active_tree_panel(__ztree_panel_0);
             });
             __ztree_tab_btn_1.click(function(){
             if(!__ztree_panel_1){
             __ztree_panel_1 = $('<ul id="'+__open_btn_id+'_tree_tab_1" class="ztree"></ul>');
             __ztree_panel_1.css('height' , 380);
             __ztree_panel_1.css('overflow' , 'auto');
             __window.append(__ztree_panel_1);
             __ztree_1 = __init_ztree_panel(__ztree_panel_1 , 2);
             }
             __active_tab($(this));
             __active_tree_panel(__ztree_panel_1);
             });
             __window.append(__ztree_tab_panel);
             function __active_tab(__ztree_tab_btn){
             __ztree_tab_panel.find('li.active').removeClass("active");
             __ztree_tab_btn.addClass("active");
             }
             function __active_tree_panel(__tree_panel){
             var __tree_panels = __window.children();
             for(var x = 1 ; x < __tree_panels.length ; x++){
             $(__tree_panels[x]).hide();
             }
             $(__tree_panel).show();
             }
             } else {
             if(__tree_type == 2){
             var __ztree_query_panel = $('<div class="input-group"></div>');
             var __ztree_query_input = $('<input type="text" class="form-control">');
             var __ztree_query_clear_btn = $('<span class="input-group-addon glyphicon glyphicon-remove"></span>');
             var __ztree_query_btn = $('<span class="input-group-addon glyphicon glyphicon-search"></span>');
             var __ztree_query_result_panel = $('<div class=""></div>');
             __ztree_query_result_panel.css('height' , 360);
             __ztree_query_result_panel.css('overflow' , 'auto');
             __ztree_query_result_panel.css('margin-top' , 5);
             __ztree_query_result_panel.hide();
             __ztree_query_btn.click(function(){
             var __ztree_query_keyword = __ztree_query_input.val();
             __ztree_query_keyword = trim(__ztree_query_keyword);
             __ztree_query_input.val(__ztree_query_keyword);
             if(__ztree_query_keyword == ''){
             __ztree_query_input.addClass('metar_not_null');
             __ztree_query_input.focus();
             return;
             }
             __ztree_panel_0.hide();
             __ztree_query_result_panel.empty().show();
             $.ajax({
             url : _PATH + '/commTreeController.do?method=queryPerson',
             type : 'POST',
             async : true,
             dataType : 'json',
             data : {name : __ztree_query_keyword},
             success: function (data) {
             var ____ztree_query_result_panel_table = $('<table class="table" style="table-layout:fixed"></table>');
             var ____ztree_query_result_panel_tbody = $('<tbody></tbody>');
             for (var i = 0 ; i < data.length ; i++) {
             ____ztree_query_result_panel_tbody.append('<tr><td style="width:20px;text-align:center"><input type="checkbox" value="' + data[i].userName + '" /></td><td style="width:40px;">' + data[i].trueName + '</td><td style="width:150px;">' + data[i].orgEntity.orgName + '</td></tr>');
             }
             ____ztree_query_result_panel_table.append(____ztree_query_result_panel_tbody);
             __ztree_query_result_panel.append(____ztree_query_result_panel_table);
             },
             error: function () {
             alert("error");
             }
             })
             });
             __ztree_query_clear_btn.click(function(){
             __ztree_query_input.val('');
             __ztree_query_result_panel.hide();
             __ztree_panel_0.show();
             });
             __ztree_query_panel.append(__ztree_query_input);
             __ztree_query_panel.append(__ztree_query_clear_btn);
             __ztree_query_panel.append(__ztree_query_btn);
             __window.append(__ztree_query_panel);
             __window.append(__ztree_query_result_panel);
             }
             }

             if(__tree_type == 3){
             __ztree_panel_0.css('height' , 380);
             __ztree_panel_0.css('overflow' , 'auto');
             } else if(__tree_type == 2){
             __ztree_panel_0.css('height' , 365);
             __ztree_panel_0.css('overflow' , 'auto');
             }
             __window.append(__ztree_panel_0);
             __ztree_0 = __init_ztree_panel(__ztree_panel_0 , __tree_type == 3 ? 1 : __tree_type);
             var __btn_submit = $('<span class="btn btn-success">确定</span>');
             __btn_submit.click(function(){
             if(__ztree_0.getCheckedNodes().length == 0){
             alert('请选择');
             } else {
             __window.parent().modal('hide');
             __callback(__ztree_0.getCheckedNodes());
             }
             });
             var __btn_clear = $('<span class="btn btn-default">清空</span>');
             __btn_clear.click(function(){
             __ztree_0.checkAllNodes(false);
             __window.parent().modal('hide');
             __callback('');
             });
             var __btn_close = $('<span class="btn btn-default">关闭</span>');
             __btn_close.click(function(){
             __window.parent().modal('hide');
             });
             __btns_div.append(__btn_submit);
             __btns_div.append('&nbsp;');
             __btns_div.append(__btn_clear);
             __btns_div.append('&nbsp;');
             __btns_div.append(__btn_close);
             __window.parent().append(__btns_div);
             */
        }
    )
    ;
}
function addDiyDom(treeId, treeNode) {
    var aObj;
    try {
        if (parent.$("#" + treeNode.tId + "_a").length > 0) {
            aObj = parent.$("#" + treeNode.tId + "_a");
        } else {
            aObj = $("#" + treeNode.tId + "_a");
        }
    } catch (e) {
        aObj = $("#" + treeNode.tId + "_a");
    }

    var editStr = "";
    if (treeNode.isCommon == "1")
        editStr = "<span class='demoIcon' id='diyBtn_" + treeNode.id + "' value='" + treeNode.isCommon + ":" + treeNode.type + ":" + treeNode.id + "' onfocus='this.blur();'>★</span>";
    else if (treeNode.isCommon == "0")
        editStr = "<span class='demoIcon' id='diyBtn_" + treeNode.id + "' value='" + treeNode.isCommon + ":" + treeNode.type + ":" + treeNode.id + "' onfocus='this.blur();'>☆</span>";
    if (editStr != "") {
        aObj.append(editStr);
        var btn;
        try {
            if (parent.$("#diyBtn_" + treeNode.id).length > 0) {
                btn = parent.$("#diyBtn_" + treeNode.id);
            } else {
                btn = $("#diyBtn_" + treeNode.id);
            }
        } catch (e) {
            btn = $("#diyBtn_" + treeNode.id);
        }

        //判断是否绑定了click事件
        var objEvt = $._data(btn, "events");
        if (btn) {
            if (objEvt && objEvt["click"]) {

            } else {
                btn.bind("click", function () {
                    var temp = btn.attr("value").split(":");
                    $.ajax({
                        url: _PATH + '/commTreeController.do?method=processContacts',
                        type: 'POST',
                        async: true,
                        dataType: 'json',
                        data: {isCommon: temp[0], type: temp[1], id: temp[2]},
                        success: function (data) {
                            if (temp[0] == "1") {
                                btn.attr("value", "0:" + temp[1] + ":" + temp[2]).html("☆");
                            } else {
                                btn.attr("value", "1:" + temp[1] + ":" + temp[2]).html("★");
                            }
                        },
                        error: function () {
                            iMsg("error");
                        }
                    })
                });
            }
        }
    }
}
function __init_ztree_panel(__ztree_panel, __select_type, __dataParameter) {
    var __current_tree;
    __dataParameter = $.param(__dataParameter);
    var __ztree_setting = {
        check: {
            /**复选框**/
            nocheckInherit: false,
            enable: true,
            chkStyle: 'checkbox',
            chkboxType: {'Y': 'ps', 'N': 'ps'}
        },
        async: {
            autoParam: ['id'],
            contentType: 'application/x-www-form-urlencoded',
            enable: true,
            dataFilter: __zTreeDataFilter,
            type: 'post',
            url: _PATH + '/commTreeController.do?method=createDisPatchTree2&' + __dataParameter
        },
        view: {
            addDiyDom: addDiyDom
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
                if (__select_type == 'radio' && !treeNode.checked) {
                    __current_tree.checkAllNodes(false);
                }
                return true;
            }
        }
    }
    __current_tree = $.fn.zTree.init(__ztree_panel, __ztree_setting, null);
    return __current_tree;
}

function __zTreeDataFilter(treeId, parentNode, responseData) {
//    if (responseData) {
//        for(var i =0; i < responseData.length; i++) {
//            responseData[i].nocheck = true;
//        }
//    }
    return responseData;
};

/*-------------------------- +
 拖拽函数
 +-------------------------- */
function drag(oDrag, handle, cursor) {
    var disX = dixY = 0;
    handle = handle || oDrag;
    //handle.style.cursor = "move";
    handle.onmousedown = function (event) {
        var event = event || window.event;

        var NS = navigator.appName == 'Netscape';//当前浏览器的类型 Netscape ,Microsoft Internet Explorer
        if (event.button == 2) { //単鼠标右击是不拖动对象
            //alert(event.button);
            return;
        }

        handle.style.cursor = cursor || "move"; //鼠标移动对象时的样式
        disX = event.clientX - oDrag.offsetLeft;
        disY = event.clientY - oDrag.offsetTop;

        document.onmousemove = function (event) {
            var event = event || window.event;
            var iL = event.clientX - disX;
            var iT = event.clientY - disY;
//            alert(iL + ',' + iT);
            var bgLeft = window.pageXOffset
                || document.documentElement.scrollLeft
                || document.body.scrollLeft || 0;

            var bgTop = window.pageYOffset
                || document.documentElement.scrollTop
                || document.body.scrollTop || 0;

            if (document.documentMode != null && typeof(document.documentMode) != "undefined" && document.documentMode < 7) {
                DOMwidth = document.documentElement.scrollWidth + bgLeft;
                DOMheight = document.documentElement.scrollHeight + bgTop;
            } else {
                DOMwidth = document.documentElement.clientWidth + bgLeft;
                DOMheight = document.documentElement.clientHeight + bgTop;
            }
            var maxL = DOMwidth - oDrag.offsetWidth;
            var maxT = DOMheight - oDrag.offsetHeight;

            //李强修改，修改拖拽方法，针对样式position: fixed;相对于浏览器窗口进行定位
            bgLeft = bgTop = 0;
            if (DOMwidth < document.body.clientWidth) {
                maxL = document.body.clientWidth - oDrag.offsetWidth;
            }
            if (DOMheight < document.body.clientHeight) {
                maxT = document.body.clientHeight - oDrag.offsetHeight;
            }


            iL >= maxL && (iL = maxL);
            iT >= maxT && (iT = maxT);
            iL <= bgLeft && (iL = bgLeft);
            iT <= bgTop && (iT = bgTop);
//            alert(iL + ',' + iT);
            oDrag.style.left = iL + "px";
            oDrag.style.top = iT + "px";
            oDrag.style.margin = 0 + "px";

            return false
        };

        document.onmouseup = function () {
            document.onmousemove = null;
            document.onmouseup = null;
            this.releaseCapture && this.releaseCapture()
            handle.style.cursor = ""; //鼠标移动对象时的样式
        };
        this.setCapture && this.setCapture();
        return false
    };
    /*//最大化按钮
     oMax.onclick = function ()
     {
     oDrag.style.top = oDrag.style.left = 0;
     oDrag.style.width = document.documentElement.clientWidth - 2 + "px";
     oDrag.style.height = document.documentElement.clientHeight - 2 + "px";
     this.style.display = "none";
     oRevert.style.display = "block";
     };
     //还原按钮
     oRevert.onclick = function ()
     {
     oDrag.style.width = dragMinWidth + "px";
     oDrag.style.height = dragMinHeight + "px";
     oDrag.style.left = (document.documentElement.clientWidth - oDrag.offsetWidth) / 2 + "px";
     oDrag.style.top = (document.documentElement.clientHeight - oDrag.offsetHeight) / 2 + "px";
     this.style.display = "none";
     oMax.style.display = "block";
     };
     //最小化按钮
     oMin.onclick = oClose.onclick = function ()
     {
     oDrag.style.display = "none";
     var oA = document.createElement("a");
     oA.className = "open";
     oA.href = "javascript:;";
     oA.title = "还原";
     document.body.appendChild(oA);
     oA.onclick = function ()
     {
     oDrag.style.display = "block";
     document.body.removeChild(this);
     this.onclick = null;
     };
     };
     //阻止冒泡
     oMin.onmousedown = oMax.onmousedown = oClose.onmousedown = function (event)
     {
     this.onfocus = function () {this.blur()};
     (event || window.event).cancelBubble = true
     };*/
}


/*---表单校验---*/
function __metar_check_form(__form) {
    __show_metar_loading();
    var __check_result = true;
    var __elements;
    var __first_null_element;
    if (__form) {
        __elements = $(__form).find('.__metar_check_form');
    } else {
        __elements = $('.__metar_check_form');
    }
    for (var i = 0; i < __elements.length; i++) {
        var __element = $(__elements[i]);
        if (__element) {
            var __current_element_null = false;
            var __value = __element.val();
            if (__value == '') {
                __current_element_null = true;
            } else {
                __value = trim(__value);
                if (__value == '') {
                    __current_element_null = true;
                } else {
                    __element.removeClass('metar_not_null');
                }
            }
            if (__current_element_null) {
                __element.addClass('metar_not_null');
                __check_result = false;
                if (!__first_null_element) {
                    __first_null_element = __element;
                }
            }
        }
    }
    if (__first_null_element) {
        $("html,body").animate({scrollTop: __first_null_element.offset().top});
        if (!__first_null_element.attr('onfocus') || __first_null_element.attr('onfocus').indexOf('WdatePicker') == -1) {
            __first_null_element.focus();
        }
        __first_null_element = '';
    }
    if (!__check_result) {
        __hide_metar_loading();
        var msg =
            iMsg('请完成必填项');
    }
    return __check_result;
}

/*---生成table-tree---*/
function __init_metar_tree_table(__settings) {
    __show_metar_loading();
    if ($('#' + __settings.container).html() != '') {
        return;
    }
//    setTimeout(function(){
    var __tree_table_loading = $('<table class="table table-horizon table-fixed"></table>');
    var __tree_table_tbody_loading = $('<tbody></tbody>');
    var __tree_table_tr_loading = $('<tr></tr>');
    for (var j = 0; j < __settings.columns.length; j++) {
        var __tree_table_th = $('<th></th>');
        var __column = __settings.columns[j];
        __tree_table_th.append(__column.title);
        if (__column.width) {
            __tree_table_th.width(__column.width);
        }
        __tree_table_tr_loading.append(__tree_table_th);
    }
    __tree_table_tbody_loading.append(__tree_table_tr_loading);
    __tree_table_loading.append(__tree_table_tbody_loading);
    $('#' + __settings.container).append(__tree_table_loading);
//    } , 1);

    $.ajax({
        url: __settings.loadUrl,
        type: 'POST',
        async: true,
        dataType: 'json',
        success: function (response) {
            if (response) {
                var showOnly = response.showOnly;
                var data = response.data;
//                console.info(response);
                var __tree_table = $('<table class="table table-horizon table-fixed"></table>');
                var __tree_table_tbody = $('<tbody></tbody>');
                for (var i = 0; i < data.length; i++) {
                    var __treeNode = data[i];
                    __treeNode.showOnly = showOnly;
                    var __tree_table_tr = $('<tr data-tt-id="' + __treeNode.id + '" data-tt-parent-id="' + __treeNode.parentId + '"></tr>');
                    for (var j = 0; j < __settings.columns.length; j++) {
                        var __tree_table_td = $('<td></td>');
                        var __column = __settings.columns[j];
                        if (showOnly && __column.hiddenInShow) {
                            continue;
                        }
                        var __column_value;
                        if (__column_value = __treeNode[__column.column] || __column.value) {
                            if (__column.wrapFunction) {
                                __column_value = __column.wrapFunction(__treeNode, __column_value);
                            }
                            if (__column.width) {
                                __tree_table_td.width(__column.width);
                                if (__column.overflowHidden) {
                                    var __span = $('<span class="overflow-hidden">' + __column_value + '</span>');
                                    __span.width(__column.width);
                                    __tree_table_td.append(__span);
                                } else {
                                    __tree_table_td.append('<span>' + __column_value + '</span>');
                                }
                            } else {
                                __tree_table_td.append('<span>' + __column_value + '</span>');
                            }
//                            __tree_table_td.append('<span>' + __column_value + '</span>');
                        }

                        __tree_table_tr.append(__tree_table_td);
                    }
                    __tree_table_tbody.append(__tree_table_tr);
                }

                var __tree_table_tr_title = $('<tr></tr>');
                for (var j = 0; j < __settings.columns.length; j++) {
                    var __tree_table_th = $('<th></th>');
                    var __column = __settings.columns[j];
                    if (showOnly && __column.hiddenInShow) {
                        continue;
                    }
                    __tree_table_th.append(__column.title);
                    if (__column.width) {
                        __tree_table_th.width(__column.width);
                    }
                    __tree_table_tr_title.append(__tree_table_th);
                }
                __tree_table_tbody.prepend(__tree_table_tr_title);
                __tree_table.append(__tree_table_tbody);
                __tree_table.treetable({
                    expandable: true,
                    initialState: 'collapsed'
                });
                $('#' + __settings.container).empty().append(__tree_table);
                if (__settings.callback) {
                    __settings.callback(response);
                }
                __hide_metar_loading();
            }
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            __hide_metar_loading();
        }
    })
}


/*---删除左右两端的空格---*/
function trim(str) {
    return str.replace(/(^\s*)|(\s*$)/g, "");
}

/*---json 转 String---*/
function JsonObjectToString(o) {
    var arr = [];
    var fmt = function (s) {
        if (typeof s == 'object' && s != null) return JsonObjectToString(s);
        return /^(string|number)$/.test(typeof s) ? "\"" + s + "\"" : s;
    };

    if (o instanceof Array) {
        for (var i in o) {
            arr.push(fmt(o[i]));
        }
        return '[' + arr.join(',') + ']';

    }
    else {
        for (var i in o) {
            arr.push("\"" + i + "\":" + fmt(o[i]));
        }
        return '{' + arr.join(',') + '}';
    }
}

function __init_query_select(select) {
    var value = select.val();
//    alert(value);
}

//添加或者删除常用联系人
function clickAddOrDel(star) {
    var clickObject;
    var temp;
    if (parent.$("#" + star.id).length > 0) {
        clickObject = parent.$("#" + star.id);
    } else {
        clickObject = $("#" + star.id);
    }
    temp = clickObject.attr("value").split(":");
    $.ajax({
        url: _PATH + '/commTreeController.do?method=processContacts',
        type: 'POST',
        async: true,
        dataType: 'json',
        data: {isCommon: temp[0], type: temp[1], id: temp[2]},
        success: function (data) {
            if (temp[0] == "1") {
                clickObject.attr("value", "0:" + temp[1] + ":" + temp[2]).html("☆");
            } else {
                clickObject.attr("value", "1:" + temp[1] + ":" + temp[2]).html("★");
            }
        },
        error: function () {
            iMsg("error");
        }
    })
}

function filterArray(res) {
    var jsonOrg = {};
    var jsonPerson = {};
    var result = new Array();
    for (var i = 0; i < res.length; i++) {
        if (res[i].type == "1") {
            if (!jsonOrg[res[i].id]) {
                result.push(res[i]);
                jsonOrg[res[i].id] = 1;
            }
        } else {
            if (!jsonPerson[res[i].id]) {
                result.push(res[i]);
                jsonPerson[res[i].id] = 1;
            }
        }
    }
    return result;
}

/**
 * 在页顶部显示提示信息
 * @param alertID 提示唯一编码，同一编码的提示只显示一次
 * @param alertContent 提示内容
 * @param autoCloseTime 自动关闭时间（毫秒）,默认50000
 * @param alterType 提示类型：success,info,warning,danger，默认danger
 * @private
 */
function __show_bootstrap_msg(alertID, alertContent, autoCloseTime, alterType) {
    if (!alterType) {
        alterType = 'danger';
    }

    if (!autoCloseTime) {
        autoCloseTime = 5000;
    }

    var alertContainer = $('#alert-container');
    if (alertContainer.length == 0) {
        alertContainer = $('<div id="alert-container" style="top:0;position:fixed;width:100%;z-index: 9999;background-color: #fff;"></div>');
        $('body').append(alertContainer);
    }

    var alertItem = $('#alert-item-' + alertID);
    if (alertItem.length > 0) {
        alertItem.remove();
    }

    alertItem = $('<div id="alert-item-' + alertID + '" class="alert alert-' + alterType + '" style="display:none">' + alertContent + '</div>');

    setTimeout(function () {
        if (alertItem) {
            alertItem.fadeOut(function () {
                alertItem.remove();
            });
        }
    }, autoCloseTime);

    var alterBtn = $('<button type="button" class="close">&times;</button>');
    alterBtn.click(function () {
        alertItem.fadeOut(function () {
            alertItem.remove();
        });
    });
    alertItem.append(alterBtn);
    alertItem.slideDown();
    alertContainer.append(alertItem);
}

function __show_bootstrap_confirm(content, callback) {

    var confirmModal = $('<div class="modal fade" id="addComponentFormWin" tabindex="-1" role="dialog" aria-hidden="true"></div>');
    var confirmModalDialog = $('<div class="modal-dialog" style="top:30%;"></div>');
    var confirmModalContent = $('<div class="modal-content"></div>');
    var confirmModalHeader = $('<div class="modal-header"></div>');
    var btn = $('<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>');
    var title = $('<h4 class="modal-title"><i class="glyphicon glyphicon-bell"></i>&nbsp;&nbsp;确认信息</h4>');
    confirmModalHeader.append(btn).append(title);

    var confirmModalBody = $('<div class="modal-body">' + content + '</div>');
    var confirmModalFooter = $('<div class="modal-footer"></div>');
    var submit = $('<button type="button" class="btn btn-success" data-dismiss="modal">确定</button>');
    submit.click(function () {
        if (callback) {
            callback();
        }
    });
    var close = $('<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>');
    confirmModalFooter.append(submit).append(close);

    confirmModalContent.append(confirmModalHeader).append(confirmModalBody).append(confirmModalFooter);

    confirmModalDialog.append(confirmModalContent);
    confirmModal.append(confirmModalDialog);

    //confirmModal.modal('show');
    confirmModal.modal({backdrop: 'static', show: true});

}

function __show_bootstrap_auditWin(title, callback) {
    var confirmModal = $('<div class="modal fade" id="addComponentFormWin" tabindex="-1" role="dialog" aria-hidden="true"></div>');
    var confirmModalDialog = $('<div class="modal-dialog" style="top:30%;"></div>');
    var confirmModalContent = $('<div class="modal-content"></div>');
    var confirmModalHeader = $('<div class="modal-header"></div>');
    var btn = $('<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>');
    var title = $('<h4 class="modal-title"><i class="glyphicon glyphicon-bell"></i>&nbsp;&nbsp;' + title + '</h4>');
    confirmModalHeader.append(btn).append(title);

    var confirmModalBody = $('<div class="modal-body"><table class="table table-editfdbktask"><tbody><tr><th>' +
    '审核意见<b class="Required">*</b></th><td colspan="5"><textarea rows="5" id="approvalOpinion_result" class="form-control"></textarea></td></td> </table></div>');
    var confirmModalFooter = $('<div class="modal-footer"></div>');
    var submit = $('<button type="button" class="btn btn-success" data-dismiss="modal">确定</button>');
    submit.click(function () {
        if ($(this).parent().parent().find("#approvalOpinion_result").val() == '') {
            iMsg("请输入审核意见");
            return false;
        }
        if (callback) {
            callback($(this).parent().parent().find("#approvalOpinion_result").val());
            return false;
        }
    });
    var close = $('<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>');
    confirmModalFooter.append(close).append(submit);

    confirmModalContent.append(confirmModalHeader).append(confirmModalBody).append(confirmModalFooter);

    confirmModalDialog.append(confirmModalContent);
    confirmModal.empty().append(confirmModalDialog);

    //confirmModal.modal('show');
    confirmModal.modal({backdrop: 'static', show: true});

}

function checkAllOrgs(_this) {
    $('input[name="commonOrg_checkbox"]').attr('checked', _this.checked);
}
function checkAllPersons(_this) {
    $('input[name="commonPerson_checkbox"]').attr('checked', _this.checked);
}