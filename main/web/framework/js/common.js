/**
 * modal窗口
 * @param option
 */
function showIModal(option){
    layer.open(option);
}
function closeIModal(index){
    if(!index){
        layer.closeAll('page');
    } else {
        layer.close(index);
    }

}
/**
 * 显示confirm
 * @param option
 */
function iConfirm(content , option , yes , cancel){
    layer.confirm(content , option , yes , cancel);
}
/**
 * 警告信息
 * @param msg
 */
function iAlert(msg){
    layer.alert(msg);
}

/**
 * 提示信息
 * @param msg
 */
function iMsg(msg , icon){
    if(!icon){
        icon = 5;
    }
    layer.msg(msg, {icon: icon});
}

/**
 * 加载等待提示
 */
var __load;
function showILoading(){
    __load = layer.load(3);
}
function closeILoading(){
    layer.close(__load);
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