(function($) {
    $.fn.outFetterTable = {

        init : function(outFetterTableOption){
            var outFetterTableObject = {
                __data_index_value : 0,
                __curr_data_page_value : 0,
                __totalCount : 0,
                __page_size : 10,
                __foot_bar_container : null,
                __goto_scroll_resp : null,
                __settings : null,
                dtGridPager : {},
                __process_bar_thread : null,

                //初始化
                init : function(){
                    var outFetterTable = outFetterTableObject;

                    //自定义设置
                    $.extend(outFetterTable , outFetterTableOption);

                    //初始化查询参数
                    outFetterTable.dtGridPager.pageSize = outFetterTable.__page_size ;
                    outFetterTable.dtGridPager.highQueryParameters = {} ;

                    //等待提示
                    outFetterTable.__process_bar_container = $('<div class="progress progress-striped active"></div>');
                    outFetterTable.__process_bar = $('<div class="progress-bar" role="progressbar" aria-valuenow="1" aria-valuemin="0" aria-valuemax="100"></div>');
                    outFetterTable.__process_bar_container.append(outFetterTable.__process_bar);

                    //刷新按钮
                    outFetterTable.__refresh_btn = $('<span class="btn btn-default float-left btn-refresh">刷新<img src="'+_PATH+'/base/_resources/refresh.png"/></span>');
                    outFetterTable.__refresh_btn.click(function(){
                        outFetterTable.refresh();
                    });

                    //数据总数
                    outFetterTable.__total_count_label = $('<span class="float-left total-count-label">共<span id="__total_count_value' + outFetterTable.__settings.renderTo + '" style="font-weight: bold">0</span>条</span>');

                    //快速检索
                    outFetterTable.__quick_search = $('<div class="float-left quick-search-container"><img src="'+_PATH+'/base/_resources/search.png"/></div>');
                    outFetterTable.__quick_search_input = $('<input id="__quick_search_input" class="__quick_search_input" placeholder="快速检索"/>');
                    outFetterTable.__quick_search.append(outFetterTable.__quick_search_input);
                    outFetterTable.__quick_search_btn = $('<div class="float-left btn btn-default __quick_search_btn">搜索</div>');
                    outFetterTable.__quick_search_btn.click(function(){
                       outFetterTable.refresh();
                    });
                    outFetterTable.__quick_search_input.bind('keyup', function(event) {
                       if (event.keyCode == "13") {
                           //回车执行查询
                           outFetterTable.refresh();
                       }
                    });

                    //高级查询
                    outFetterTable.__detail_search_btn = $('<span class="btn btn-default float-right btn-high-query">高级查询<img src="'+_PATH+'/base/_resources/h-query.png"/></span>');
                    outFetterTable.__detail_search_btn = $('<span class="btn btn-default float-right btn-high-query">高级查询</span>');
                    outFetterTable.__detail_search_btn.click(function(){
                       outFetterTable.showHighQueryDialog();
                    });

                    //数据表头
                    outFetterTable.__data_title_table = $('<table  class="out-fetter-header-table" style="margin-top: 5px;"></table>');
                    outFetterTable.__data_title_tbody = $('<tbody></tbody>');
                    outFetterTable.__data_title_tr = $('<tr></tr>');
                    for (var x = 0; x < outFetterTable.__settings.columns.length; x++) {
                        var __data_title_td = $('<th></th>');
                        var __data_title_column = outFetterTable.__settings.columns[x];
                        var __data_title_value = __data_title_column.title;
                        __data_title_td.append(__data_title_value);
                        outFetterTable.__data_title_tr.append(__data_title_td);
                        if (__data_title_column.width) {
                            __data_title_td.width(__data_title_column.width);
                        }
                    }
                    outFetterTable.__data_title_tbody.append(outFetterTable.__data_title_tr);
                    outFetterTable.__data_title_table.append(outFetterTable.__data_title_tbody);

                    //头部，包含等待提示、刷新、快速检索、高级查询、数据表头
                    outFetterTable.__header_container = $('<div class="header-container"></div>');
                    outFetterTable.__header_container.append(outFetterTable.__refresh_btn).append(outFetterTable.__total_count_label).append(outFetterTable.__quick_search).append(outFetterTable.__quick_search_btn).append(outFetterTable.__detail_search_btn).append($('<div style="clear:both"></div>')).append(outFetterTable.__process_bar_container).append(outFetterTable.__data_title_table);

                    //底部，包含已加载提示、top钮
                    outFetterTable.__foot_bar_container = $('<div class="out-fetter-footer foot-bar-container"></div>');
                    outFetterTable.__go_top_img = $('<img class="float-right" style="cursor:pointer;" src="'+_PATH+'/base/_resources/go-top.png"/>');
                    //outFetterTable.__go_top_img = $('<div class="float-right"><div style="font-size: 20px;" class="glyphicon glyphicon-chevron-up"></div><div>TOP</div></div></div>');
                    outFetterTable.__go_top_img.click(function(){
                        outFetterTable.goTop(); x
                    });
                    //数据列表底部，补空位
                    outFetterTable.__foot_blank_div = $('<div style="height:45px;"></div>');

                    //添加头部、底部
                    $('#' + outFetterTable.__settings.renderTo).empty().append(outFetterTable.__header_container).append(outFetterTable.__foot_bar_container);

                    //数据列表容器
                    outFetterTable.__data_table_container = $('<div class="data-table-container"></div>');

                    //滚动加载
                    $(window).scroll(function () {
                        if(outFetterTable.__goto_scroll_resp == null){
                            outFetterTable.__goto_scroll_resp = setTimeout(function(){
                                if($(window).scrollTop() > 50){
                                    outFetterTable.__header_container.css({position : 'fixed'});
                                } else if($(window).scrollTop() < 50){
                                    outFetterTable.__header_container.css({position : ''});
                                }
                                if (($(window).scrollTop()) >= ($(document).height() - $(window).height()) && outFetterTable.__data_index_value != outFetterTable.__totalCount) {
                                    outFetterTable.load(function(){
                                        outFetterTable.__goto_scroll_resp = null;
                                    });
                                } else {
                                    outFetterTable.__goto_scroll_resp = null;
                                }
                            } , 100)
                        }
                    });

                    outFetterTable.refresh();


                } ,
                //加载数据
                load : function(__callback , query_model){
                    var outFetterTable = outFetterTableObject;
                    outFetterTable.dtGridPager.startRecord = outFetterTable.__curr_data_page_value++*outFetterTable.__page_size;

                    if(query_model == 'HIGH' && outFetterTable.__highQueryDialogPanel){
                        var highQueryItems = outFetterTable.__highQueryDialogPanel.find('input');
                        for(var h = 0 ; h < highQueryItems.length ; h++){
                            outFetterTable.dtGridPager.highQueryParameters[highQueryItems[h].id] = highQueryItems[h].value;
                        }
                        outFetterTable.dtGridPager.fastQueryKeyWord = '';
                    }else if(outFetterTable.__highQueryDialogPanel){
                        var highQueryItems = outFetterTable.__highQueryDialogPanel.find('input');
                        for(var h = 0 ; h < highQueryItems.length ; h++){
                            outFetterTable.dtGridPager.highQueryParameters[highQueryItems[h].id] = highQueryItems[h].value;
                        }
                        outFetterTable.dtGridPager.fastQueryKeyWord = '';
                        // outFetterTable.dtGridPager.fastQueryKeyWord = outFetterTable.__quick_search_input.val();
                    }else {
                        outFetterTable.dtGridPager.fastQueryKeyWord = outFetterTable.__quick_search_input.val();
                        outFetterTable.dtGridPager.highQueryParameters = {};
                        // outFetterTable.dtGridPager.highQueryParameters[lk_processModelName] = 'aaaa';
                    }

                    $.ajax({
                        url: outFetterTable.__settings.loadURL,
                        data : {dtGridPager : JsonObjectToString(outFetterTable.dtGridPager)},
                        contentType: "application/x-www-form-urlencoded; charset=utf-8",
                        type: 'POST',
                        async: true,
                        dataType: 'json',
                        success: function (response) {
                            if (response) {
                                var __dataList = response.exhibitDatas;
                                outFetterTable.__totalCount = response.recordCount;
                                $('#__total_count_value' + outFetterTable.__settings.renderTo).text(outFetterTable.__totalCount);

                                for (var i = 0; i < __dataList.length; i++) {
                                    var __data_table = $('<table class="out-fetter-table"></table>');
                                    var __data_table_tbody = $('<tbody></tbody>');
                                    var __data = __dataList[i];
                                    var __data_tr = $('<tr></tr>');
                                    for (var j = 0; j < outFetterTable.__settings.columns.length; j++) {
                                        var __data_td = $('<td></td>');
                                        var __data_column = outFetterTable.__settings.columns[j];
                                        var __data_value = __data[__data_column.column];
                                        if (__data_column.wrapFunction) {
                                            __data_value = __data_column.wrapFunction(__data, __data_value);
                                        }
                                        if (__data_column.width) {
                                            __data_td.width(__data_column.width);
                                        }
                                        if (__data_column.textAlign) {
                                            __data_td.css({'text-align':__data_column.textAlign});
                                        }
                                        __data_td.append(__data_value);
                                        __data_tr.append(__data_td);
                                    }
                                    __data_table_tbody.append(__data_tr);
                                    //if(outFetterTable.__settings.extendRow){
                                    //    __data_table_tbody.append($(outFetterTable.__settings.extendRow(__data)));
                                    //}
                                    __data_table.append(__data_table_tbody);
                                    outFetterTable.__data_table_container.append(__data_table);
                                    if(outFetterTable.__data_index_value++%2 == 1){
                                        __data_table.addClass('out-fetter-table-odd');
                                    }
                                    outFetterTable.__data_table_container.append(outFetterTable.__foot_blank_div);
                                }
                                $('#' + outFetterTable.__settings.renderTo).append(outFetterTable.__data_table_container);
                                if(outFetterTable.__data_index_value == outFetterTable.__totalCount){
                                    outFetterTable.__foot_bar_container.text(outFetterTable.__data_index_value + "条全部加载完成").append(outFetterTable.__go_top_img);
                                } else {
                                    $('#__loaded_count_value' + outFetterTable.__settings.renderTo).text(outFetterTable.__data_index_value);
                                }

                                if(__callback){
                                    __callback();
                                }
                            }
                        }
                    })
                } ,
                //刷新
                refresh : function(query_model){
                    var outFetterTable = outFetterTableObject;
                    outFetterTable.showProcessBar();
                    outFetterTable.__foot_bar_more_action = $('<a class="foot-bar-more-action">已加载<span class="__loaded_count_value" id="__loaded_count_value' + outFetterTable.__settings.renderTo + '">0</span>条，点击或滚动继续加载</a>');
                    outFetterTable.__foot_bar_more_action.click(function(){
                        outFetterTable.load();
                    });
                    outFetterTable.__foot_bar_container.empty().append(outFetterTable.__foot_bar_more_action).append(outFetterTable.__go_top_img);
                    if(outFetterTable.__data_table_container){
                        outFetterTable.__data_table_container.empty();
                    }
                    outFetterTable.__data_index_value = 0;
                    outFetterTable.__curr_data_page_value = 0;
                    outFetterTable.__totalCount = 0;
                    outFetterTable.load(function(){
                        outFetterTable.goTop();
                        outFetterTable.hideProcessBar();
                    } , query_model);
                } ,
                //显示高级查询窗口
                showHighQueryDialog : function(){
                    var outFetterTable = outFetterTableObject;
                    if(outFetterTable.__highQueryDialogPanel){
                        outFetterTable.__highQueryDialogPanel.modal('show');
                    } else {
                        outFetterTable.__highQueryDialogPanel = $('<div class="modal fade"></div>');
                        var __modal_dialog = $('<div class="modal-dialog"></div>');
                        var __modal_content = $('<div class="modal-content"></div>');
                        var __modal_header = $('<div class="modal-header"><button class="close" data-dismiss="modal" type="button">×</button><h4 class="modal-title">高级查询</h4></div>');
                        var __modal_body = $('<div class="modal-body form-horizontal"></div>');

                        var __high_query_columns = outFetterTable.__settings.columns;
                        if(outFetterTable.__settings.extQueryColumns){
                            __high_query_columns = __high_query_columns.concat(outFetterTable.__settings.extQueryColumns);
                        }

                        __high_query_columns.sort(function(col1 , col2){
                            if(col1.queryIndex){
                                return -1;
                            }
                            if(col2.queryIndex){
                                return 1;
                            }
                            return col1.queryIndex > col2.queryIndex;
                        });

                        for (var k = 0; k < __high_query_columns.length; k++) {
                            var __data_column = __high_query_columns[k];
                            //alert(__data_column.title + ',' + __data_column.highQuery);
                            if(__data_column.highQuery){
                                var __row = $('<div class="form-group"></div>');
                                var __label = $('<label <!--class="col-sm-3 control-label text-right"-->'+__data_column.title+'：</label>');
                                __row.append(__label);
                                if(__data_column.highQueryType == 'range'){
                                    var __le = $('<div class="col-sm-4"><div class=" input-group"><input id="le_' + __data_column.column + '" class="form-control" placeholder="请输入开始'+__data_column.title+'" onclick="WdatePicker({dateFmt: \'yyyy-MM-dd HH:mm:ss\'})"/><div class="input-group-addon"><i class="fa fa-calendar"></i></div></div></div>');
                                    var __ge = $('<div class="col-sm-4"><div class=" input-group"><input id="ge_' + __data_column.column + '" class="form-control" placeholder="请输入结束'+__data_column.title+'" onclick="WdatePicker({dateFmt: \'yyyy-MM-dd HH:mm:ss\'})"/><div class="input-group-addon"><i class="fa fa-calendar"></i></div></div></div>');
                                    __row.append(__le).append(__ge);
                                } else if(__data_column.highQueryType == 'enum'){
                                    var __select = $('<select id="eq_' + __data_column.column + '" class="form-control" />');
                                    __select.append('<option value="">请选择</option>');
                                    if(__data_column.enums){
                                        for(var i = 0 ; i < __data_column.enums.length; i++){
                                            var _option = __data_column.enums[i];
                                            __select.append('<option value="'+_option.value+'">'+_option.label+'</option>');
                                        }
                                    }
                                    var __eq_select_div = $('<div class="col-sm-8"></div>').append(__select);
                                    __row.append(__eq_select_div);
                                } else {
                                    var __text_input = $('<div class="col-sm-8"><input id="lk_' + __data_column.column + '" class="form-control" placeholder="请输入'+__data_column.title+'" /></div>');
                                    __row.append(__text_input);
                                }
                                __row.append('<div class="clearfix"></div>');
                                __modal_body.append(__row);
                            }
                        }

                        var __modal_fotter = $('<div class="modal-footer"></div>');
                        var __btn_query = $('<button class="btn btn-danger" type="button"><i class="fa fa-search"></i>查询</button>');
                        __btn_query.click(function(){
                            outFetterTable.refresh('HIGH');
                            outFetterTable.__highQueryDialogPanel.modal('hide')
                        });
                        var __btn_reset = $('<button class="btn btn-default" type="button"><i class="fa fa-reply"></i>重置</button>');
                        __btn_reset.click(function(){
                            outFetterTable.__highQueryDialogPanel.find('input').val('');
                        });
                        __modal_fotter.append(__btn_query).append(__btn_reset);

                        __modal_content.append(__modal_header).append(__modal_body).append(__modal_fotter);
                        __modal_dialog.append(__modal_content);
                        outFetterTable.__highQueryDialogPanel.append(__modal_dialog);
                        $('#' + outFetterTable.__settings.renderTo).append(outFetterTable.__highQueryDialogPanel.modal('show'));
                    }

                },
                //显示等待提示
                showProcessBar : function(){
                    var outFetterTable = outFetterTableObject;
                    var __processWidth = 0;
                    outFetterTable.__process_bar.show();
                    outFetterTable.__process_bar_thread = setInterval(function(){
                        __processWidth += Math.random()*(100-__processWidth)*0.1;
                        outFetterTable.__process_bar.animate({width:__processWidth+'%'}, 200);
                    }, 200);
                } ,
                //隐藏等待提示
                hideProcessBar : function(){
                    var outFetterTable = outFetterTableObject;
                    clearInterval(outFetterTable.__process_bar_thread);
                    outFetterTable.__process_bar.animate({width:'100%'} , 100 , function(){
                        outFetterTable.__process_bar.fadeOut(100 , function(){
                            outFetterTable.__process_bar.width(0);
                        });
                    });
                } ,
                //回到顶部
                goTop : function(){
                    $('html, body').animate({scrollTop:0}, 1000);
                }
            }
            outFetterTableObject.init();
            return outFetterTableObject;
        }

    }
})(jQuery);