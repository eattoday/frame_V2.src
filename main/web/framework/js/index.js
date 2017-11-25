//监听浏览器地址栏发生变化
window.onhashchange = function(){
    //alert(window.location.hash);
}
$(document).ready(function(){

    var first = 'business';

    if(!window.location.hash){
        window.location.hash = first;
    }
    var menu = $(window.location.hash);

    var menuID = menu.attr('id');
    var split = menuID.indexOf('-');
    if(split > -1){
        first = menuID.substring(0 , split);
    }

    $('#header-container .first').show();

    locateFirstMenu(first);

    forwardContent(menu.attr('data-href'));
    menu.addClass('active');

    //左侧菜单中的所有超链接都加动画效果
    $('#left-menu-container').find('a').addClass('fadeInUp animated');
    //重置框架布局
    layout();


    $('#header-container .first').click(function(){
        var current = $(this);
        if(current.parent().attr('id') == 'header-left'){
            return false;
        }
        locateFirstMenu(this.id);
    });

    //父级菜单点击处理
    $('.uf-nav-parent').click(function(){
        var parentNav = $(this);
        var clildren = parentNav.parent().find('.uf-nav');
        var menuIcon = parentNav.find('.menu-icon');
        clildren.stop().slideToggle(function(){
            if($(this).css('display') != 'none'){
                menuIcon.removeClass('fa-caret-right fadeInLeft').addClass('fa-caret-down fadeInDown');
            } else {
                menuIcon.removeClass('fa-caret-down fadeInDown').addClass('fa-caret-right fadeInLeft');
            }
        });
    });

    //鼠标划过二级菜单，给出提示信息
    $('.uf-nav-item a').hover(function(){
        var tipText = $(this).find('.menu-text')[0].innerHTML;
        $('#left-menu-tooltip-text').text(tipText);
        $('#left-uf-nav-bar').stop().css({top : $(this).offset().top - 45, height:'45px'});
        if($('.menu-text').css('display') == 'none'){
            $('#left-menu-tooltip').stop().show().css({top : $(this).offset().top , left : $(this).width() + 40});
        }
    } , function(){
        $('#left-menu-tooltip').stop().hide();
        $('#left-uf-nav-bar').stop().css({height:0});
    })

    //菜单点击处理
    //$('.uf-nav-item a').click(function(){
    //    var href = $(this).attr('data-href');
    //    forwardContent(href);
    //
    //});

    //收缩展开按钮处理
    var left_menu_width = $('#left-menu-container').width();
    $('#btn-expand-collapse').click(function(){
        if($('.menu-text').css('display') == 'none'){
            $('#left-menu-container').animate({width:left_menu_width + 'px'} , 200 , function(){
                layout();
            });
            $('.menu-text').show();
            $(this).html('<span class="fa fa-angle-double-left"></span>');
        } else {
            $('#left-menu-container').animate({width:'55px'} , 200 , function(){
                layout();
            });
            $('.menu-text').hide();
            $(this).html('<span class="fa fa-angle-double-right"></span>');
        }
    });

    //点击菜单，修改浏览器地址栏地址
    $('.uf-menu').click(function(){
        var menu = $(this);
        var href = menu.attr('data-href');
        forwardContent(href);
        var hash = menu.attr('uf-url') || menu.attr('id');
        if(!hash){
            hash = '';
        }
        window.location.hash = hash;

        $('.uf-menu').removeClass('active');
        menu.addClass('active');
    });

    $("#left-menu-nav").mCustomScrollbar({ theme:"minimal" });

    //窗口变化重置框架布局
    //var resizing = null;
    //$(window).resize(function(){
    //    if(resizing == null){
    //        resizing = setTimeout(function(){
    //            layout();
    //            resizing = null;
    //        } , 100);
    //    }
    //});

})

function layout(){
    var leftMenuWidth = $('#left-menu-container').width();
    $('#content-container').css({left : leftMenuWidth});
}

function locateFirstMenu(menuID){
    $('#header-right').prepend($('#header-left').children().removeClass('fadeInRight').addClass('fadeInLeft animated'));
    $('#header-left').append($('#' + menuID).removeClass('fadeInLeft').addClass('fadeInRight animated'));

    $('.left-menu-container').hide();
    $('#' + menuID + '-left-menu-container').show();
}

function forwardContent(href){
    showILoading();
    $('#content-iframe').remove();
    if(href){
        if(href.indexOf('?') > -1){
            href += '&';
        } else {
            href += '?';
        }
        href += 'globalUniqueID=' + _globalUniqueID;
        $('#content-container').prepend('<iframe id="content-iframe" frameborder="no" src="'+href+'"></iframe>');

        $('#content-iframe').removeClass().addClass('fadeInUp animated').one('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', function(){
            $('#content-iframe').removeClass();
            closeILoading();
        });

        setTimeout(function(){
            closeILoading();
        } , 2000);
    }

}
