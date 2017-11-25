<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html>
<meta http-equiv="X-UA-Compatible" content="IE=8">
<meta http-equiv="content-type" content="text/html;charset=utf-8" />
<link rel="stylesheet" type="text/css"
      href="<%=request.getContextPath()%>/component/jquery.dtGrid.v1.1.9/dependents/bootstrap/css/bootstrap.min.css"/>
<link rel="stylesheet" href="http://cdn.bootcss.com/bootstrap/3.3.5/css/bootstrap.min.css">
<head>

    <style type="text/css">
        .nodeContainer { position:absolute;text-align: center;}
        .node { padding:20px;margin-bottom:5px;border-radius:8px;width:120px; }
        .info_win { display:none;position:absolute;z-index:1;width:400px;height:400px;background-color: #fff;border: 1px solid #bbb;text-align: left;}
    </style>
</head>
<body>


<script type="text/javascript"
        src="<%=request.getContextPath()%>/component/jquery.dtGrid.v1.1.9/dependents/jquery/jquery.min.js"></script>
<script src="<%=request.getContextPath()%>/component/jsPlumb-1.4.1/jquery.jsPlumb-1.4.1-all-min.js"></script>
<script>

    var data = [{
        id:"node1",label:"调度单拟稿",parent:"start",level:1,state:"success",orgName:"中国联通总部"
    },{
        id:"node2",label:"调度单签发",parent:"node1",level:2,state:"success",orgName:"中国联通总部"
    },{
        id:"node3",label:"签收",parent:"node2",level:3,state:"info",orgName:"山东省分公司"
    },{
        id:"node4",label:"签收",parent:"node2",level:3,state:"warning",orgName:"福建省分公司"
    },{
        id:"node5",label:"签收",parent:"node2",level:3,state:"danger",orgName:"浙江省分公司"
    },{
        id:"node6",label:"签收",parent:"node2",level:3,state:"success",orgName:"河南省分公司"
    },{
        id:"node7",label:"签收",parent:"node2",level:3,state:"primary",orgName:"北京市分公司"
    },{
        id:"node8",label:"转派",parent:"node3",level:4,state:"primary",orgName:"济南市分公司"
    },{
        id:"node9",label:"转派",parent:"node3",level:4,state:"primary",orgName:"青岛市分公司"
    },{
        id:"node11",label:"转派",parent:"node4",level:4,state:"primary",orgName:"厦门市分公司"
//    },{
//        id:"node12",label:"转派",parent:"node5",level:4,state:"primary",orgName:"杭州市分公司"
//    },{
//        id:"node13",label:"转派",parent:"node5",level:4,state:"primary",orgName:"宁波市分公司"
//    },{
//        id:"node14",label:"转派",parent:"node6",level:4,state:"primary",orgName:"郑州市分公司"
//    },{
//        id:"node15",label:"转派",parent:"node6",level:4,state:"primary",orgName:"洛阳市分公司"
//    },{
//        id:"node16",label:"转派",parent:"node7",level:4,state:"primary",orgName:"北京一分公司"
//    },{
//        id:"node17",label:"转派",parent:"node7",level:4,state:"primary",orgName:"北京二分公司"
//    },{
//        id:"node18",label:"转派",parent:"node12",level:5,state:"primary",orgName:"杭州运维部"
    },{
        id:"node21",label:"反馈",parent:"node8",level:5,state:"primary",orgName:"济南市分公司"
    },{
        id:"node22",label:"反馈审核",parent:"node21",level:6,state:"primary",orgName:"济南市分公司"
    },{
        id:"node19",label:"反馈",parent:"node9",level:5,state:"primary",orgName:"青岛市分公司"
    },{
        id:"node20",label:"反馈审核",parent:"node19",level:6,state:"primary",orgName:"青岛市分公司"
    },{
        id:"node23",label:"反馈汇总",parent:"node20,node22",level:7,state:"primary",orgName:"山东省分公司"
    }];

    jsPlumb.ready(function() {

        var unit_x = 40;
        var unit_y = 150;
        var position_lib = {};
        var sons_lib = {};
        var max_unit_count = 0;

        // alert(123);
        var firstInstance = jsPlumb.getInstance();
        firstInstance.importDefaults({
            PaintStyle:{ strokeStyle:"#333"},
            EndpointStyle : { fillStyle: "#333"  },
            Connector : "Flowchart",
            Endpoint:"Blank",
            ConnectionOverlays : [
                [ "Arrow", {
                    location:1,
                    length:8,
                    width:10,
                    foldback:1
                } ]
            ]
        });

        var start_node = $('<a class="nodeContainer flow_level_0" id="start" sonsCount="0"><div class="node btn btn-default">开始</div></a>');
        $('body').append(start_node);
        position_lib[0] = [start_node];

        var pre_flow_node = start_node;

        for(var i = 0 ; i < data.length ; i++){
            var _level_header = false;
            var _this = data[i];
            var _this_level = _this.level;
            if(!position_lib[_this_level]){
                _level_header = true;
                position_lib[_this_level] = new Array();
            }
            var _this_level_array = position_lib[_this_level];

            var flow_node = $('<a class="nodeContainer flow_level_'+_this_level+'" id="'+_this.id+'" style="left:'+_this_level_array.length*4*unit_x+'px;top:'+_this_level*unit_y+'px" sonsCount="0"><div class="node btn btn-'+_this.state+'">'+_this.label+'</div><div>'+_this.orgName+'</div></a>');
            flow_node.attr("label" , _this.label);
            flow_node.attr("parentID" , _this.parent);
            if(!_level_header){
                pre_flow_node.attr('nextFlowNodeID' , _this.id);
            }
            pre_flow_node = flow_node;
            _this_level_array.push(flow_node);
            var _parent_flow_node = $('#' + _this.parent);
            var _parent_flow_node_sonsCount = parseInt(_parent_flow_node.attr('sonsCount'));
            _parent_flow_node.attr('sonsCount' , ++_parent_flow_node_sonsCount);
            var new_max_unit_count = _this_level_array.length*4;
            if(new_max_unit_count > max_unit_count){
                max_unit_count = new_max_unit_count;
            }
            $('body').append(flow_node);
            // bindHover(flow_node);
        };

        for(var j in position_lib){
           var _level_array = position_lib[j];
           if(_level_array.length > 0){
             var _ref_left = 0;
             // var _level_unit_count = max_unit_count/(_level_array.length);
             var _level_nodes = $('.flow_level_' + j);
             for(var index = 0; index < _level_nodes.length ; index++){
               var _this_level_node = $(_level_nodes[index]);
               var _parent_level_node = $('#' + _this_level_node.attr("parentID"));
               var _parent_level_node_left_now = 0;
               var _parent_level_node_left = 0;
               if(_parent_level_node.length > 0 ){
                 _parent_level_node_left_now = _parent_level_node_left = _parent_level_node.offset().left;
               }
               var _parent_render_sons;

               if(!(_parent_render_sons = sons_lib[_this_level_node.attr("parentID")])){
                 _parent_render_sons = sons_lib[_this_level_node.attr("parentID")] = 1;
                 if(unit_x*4*index > _parent_level_node_left){
                   _parent_level_node_left_now = unit_x*4*index;
                 }
               } else {
                 _parent_render_sons = sons_lib[_this_level_node.attr("parentID")] = parseInt(sons_lib[_this_level_node.attr("parentID")]) + 1;
               }

               if(_parent_render_sons){
                 if(_parent_render_sons < _parent_level_node.attr('sonsCount')){
                   // relocateParent(_parent_level_node , _parent_level_node_left_now , unit_x*2);
                   relocateParent(_parent_level_node , _parent_level_node_left , unit_x*2 +　_parent_level_node_left_now - _parent_level_node_left);
                 } else if(_parent_level_node.attr('sonsCount') == 1){
                   if(j == 4){

                   }
                   relocateParent(_parent_level_node , _parent_level_node_left , _parent_level_node_left_now - _parent_level_node_left);
                 }
               }
               _this_level_node.css('left' , (_parent_level_node_left > unit_x*4*index ? _parent_level_node_left : unit_x*4*index) + 'px');
             }
           }
        }

        for(var j in position_lib){
           var _level_array = position_lib[j];
           if(_level_array.length > 0){
             var _level_nodes = $('.flow_level_' + j);
             for(var index = 0; index < _level_nodes.length ; index++){
               var _this_level_node = $(_level_nodes[index]);

                 var preNodes = _this_level_node.attr("parentID");

               if(preNodes){
                   var nodes = preNodes.split(',');
                   for(var i = 0 ; i < nodes.length ; i++){
                       firstInstance.connect({source:nodes[i], target:_this_level_node[0].id , anchors:[ "BottomCenter", "TopCenter" ]});
                   }

               }
             }
           }
        }

    });

    function bindHover(flow_node){
        var _info_win;
        flow_node.hover(function(e){
            if(!_info_win){
                _info_win = $('<div class="info_win" style="top:0;left:'+flow_node.width()+'px">'+flow_node.attr("label")+'</div>');
                _info_win.append(flow_node.parentID);
                flow_node.append(_info_win);
            }
            _info_win.stop().fadeIn();

        } , function(){
            _info_win.stop().fadeOut();
        });
    }
    function relocateParent(_this_level_node , _parent_level_node_left , unit_x){
        if(_this_level_node.offset().left > _parent_level_node_left){
            return;
        }
        _this_level_node.css('left' , _parent_level_node_left + unit_x);
        var _parent_level_node = $('#' + _this_level_node.attr("parentID"));
        var _level_next_node = $('#' + _this_level_node.attr("nextFlownodeId"));
        if(_level_next_node.length > 0 ){
            relocateNext(_level_next_node , _level_next_node.offset().left , unit_x*2);
        }

        if(_parent_level_node.length > 0 ){
            relocateParent(_parent_level_node , _parent_level_node.offset().left , unit_x);
        }
    }
    function relocateNext(_this_level_node , _level_pre_node_left , unit_x){
        if(_this_level_node.offset().left > _level_pre_node_left){
            return;
        }
        _this_level_node.css('left' , _level_pre_node_left + unit_x);
        var _level_next_node = $('#' + _this_level_node.attr("nextFlownodeId"));
        if(_level_next_node.length > 0 ){
            debugger;
            relocateNext(_level_next_node , _level_next_node.offset().left , unit_x);
        }

    }
</script>
</body>
</html>