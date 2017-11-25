package com.metarnet.core.common.controller;

import com.alibaba.fastjson.JSON;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.EnumConfigAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter4Activiti;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.*;
import com.metarnet.core.common.service.IWorkflowBaseService;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.workflow.ProcessInstance;
import com.ucloud.paas.agent.PaasException;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 流程监控日志
 *
 * @author zwwang
 */
@Controller
public class CommWorkFlowMonitorController extends BaseController {


    @Resource
    private IWorkflowBaseService workflowBaseService;

    @RequestMapping(value = "/commWorkFlowMonitorController.do", params = "method=getOrderLog")
    public void getOrderLog(HttpServletRequest request, HttpServletResponse response, String processInstID, String rootProcessInstId, String jobID) throws UIException {
        JSONObject tree = new JSONObject();
        String accountId = getAccountId(request);

        List<GeneralInfoModel> orderLogModels = null;
        List<GeneralInfoModel> nowActivityList = null;
        Map<String, String> processInstID2treeNodeIdMap = null;
        Map<String, WorkFlowMonitorTreeNode> processInstID2treeNodeMap = null;

        Map<Integer, WorkFlowMonitorTreeNode> group2treeNodeMap = new HashMap<Integer, WorkFlowMonitorTreeNode>();

        int currentTreeNodeId = 0;
        String parentTreeNodeId = null;
        WorkFlowMonitorTreeNode parentTreeNode = null;

        try {
            orderLogModels = workflowBaseService.getGeneralInfoByRootProcessId(rootProcessInstId);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        try {
            nowActivityList = workflowBaseService.getAllActivityInstanceInfos(rootProcessInstId, jobID, getUserEntity(request));
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        List<WorkFlowMonitorTreeNode> trees = new ArrayList();
        if (orderLogModels != null) {
            if (nowActivityList != null) {
                orderLogModels.addAll(nowActivityList);
            }

            /*Collections.sort(orderLogModels, new Comparator<GeneralInfoModel>() {

                @Override
                public int compare(GeneralInfoModel o1, GeneralInfoModel o2) {
                    if (o1.getCreationTime().after(o2.getCreationTime())) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });*/

            processInstID2treeNodeIdMap = new HashMap<String, String>();
            processInstID2treeNodeMap = new HashMap<String, WorkFlowMonitorTreeNode>();

            for (int i = 0; i < orderLogModels.size(); i++) {
                WorkFlowMonitorTreeNode treeNode = new WorkFlowMonitorTreeNode();

                GeneralInfoModel logModel = orderLogModels.get(i);

//                processInstID2treeNodeIdMap.put(logModel.getProcessInstanceId() , String.valueOf(currentTreeNodeId));
                treeNode.setId(String.valueOf(currentTreeNodeId));
                treeNode.setOperateOrg(logModel.getOperOrgName());
                if (logModel.getOperFullOrgName() != null)
                    treeNode.setOperateOrg(logModel.getOperFullOrgName());
                if (logModel.getCreationTime() != null) {
                    String creationDateStr = logModel.getCreationTime().toString();
                    creationDateStr = creationDateStr.substring(0, 19);
                    treeNode.setArriveDateTime(creationDateStr);
                }
                if (logModel.getTaskStartTime() != null) {
                    String creationDateStr = logModel.getTaskStartTime().toString();
                    creationDateStr = creationDateStr.substring(0, 19);
                    treeNode.setArriveDateTime(creationDateStr);
                }

                if (logModel.getOpenDateTime() != null) {
                    String openDateStr = logModel.getOpenDateTime().toString();
                    openDateStr = openDateStr.substring(0, 19);
                    treeNode.setOpenDateTime(openDateStr);
                } else {
                    List generalList = workflowBaseService.getGeneraInfoList(((GeneralInfoModel) logModel).getTaskInstId());
                    if (generalList != null && generalList.size() > 0 && ((GeneralInfoModel) generalList.get(0)).getOpenDateTime() != null) {
                        String openDateStr = ((GeneralInfoModel) generalList.get(0)).getOpenDateTime().toString();
                        openDateStr = openDateStr.substring(0, 19);
                        treeNode.setOpenDateTime(openDateStr);
                    }
                }

                treeNode.setOperator(logModel.getOperUserTrueName());

                treeNode.setActivityName(logModel.getActivityInstName());
                if (logModel.getOperTime() != null) {
                    String operateTimeStr = logModel.getOperTime().toString();
                    operateTimeStr = operateTimeStr.substring(0, 19);
                    treeNode.setCompleteDateTime(operateTimeStr);
                } else {
                    treeNode.setNowActivity(true);
                }
                if (logModel.getOperTypeEnumId() != null) {
                    try {
//                        EnumValue enumValue = EnumConfigAdapter.getInstence().getEnumValueById(logModel.getOperTypeEnumId());
//                        if (enumValue != null) {
//                            if (40050227 == logModel.getOperTypeEnumId() || 40050228 == logModel.getOperTypeEnumId()) {
//                                if (logModel.getProcessingStatus().equals(Constants.Y)) {
//                                    if (40050227 == logModel.getOperTypeEnumId() && StringUtils.isNotBlank(logModel.getAttribute1())) {
//                                        if ("up".equals(logModel.getAttribute1())) {
//                                            treeNode.setProcessType(enumValue.getEnumValueName() + "-上报");
//                                        } else if ("peer".equals(logModel.getAttribute1())) {
//                                            treeNode.setProcessType(enumValue.getEnumValueName() + "-同级审核");
//                                        }
//                                    } else {
//                                        treeNode.setProcessType(enumValue.getEnumValueName() + "-通过");
//                                    }
//                                } else {
//                                    treeNode.setProcessType(enumValue.getEnumValueName() + "-驳回");
//                                }
//                            } else {
//                                treeNode.setProcessType(enumValue.getEnumValueName());
//                            }
//                        }
//                        EnumValue enumValue = EnumConfigAdapter.getInstence().getEnumValueById(logModel.getOperTypeEnumId());
//                        if (enumValue != null) {
                        //优化建议增加匿名功能，根流程修改匿名
                        if (logModel.getProcessInstId().equals(logModel.getRootProInstId())) {
                            //判断是优化建议工单
                            if ("XJ_SUGGESTION_TABLE".equals(logModel.getProcessingObjectTable().toUpperCase())) {
                                treeNode.setOperator("匿名");
                            }
                        }
                        if ("XJ_FLOWORDER_TABLE".equals(logModel.getProcessingObjectTable().toUpperCase())) {
                            if (40050444 == logModel.getOperTypeEnumId()) {
                                treeNode.setProcessType(logModel.getActivityInstName() + "取回");
                            } else if (99990004 == logModel.getOperTypeEnumId()) {
                                treeNode.setProcessType(logModel.getActivityInstName() + "撤销");
                            } else {
                                if (Constants.Y.equals(logModel.getProcessingStatus())) {
                                    treeNode.setProcessType(logModel.getActivityInstName() + "-通过");
                                } else if (Constants.N.equals(logModel.getProcessingStatus())) {
                                    treeNode.setProcessType(logModel.getActivityInstName() + "-退回");
                                } else {
                                    treeNode.setProcessType(logModel.getActivityInstName());
                                }
                            }
                        } else {
                            if (40050227 == logModel.getOperTypeEnumId() || 40050228 == logModel.getOperTypeEnumId()) {
                                if (Constants.Y.equals(logModel.getProcessingStatus())) {
                                    treeNode.setProcessType("通过");
//                                    treeNode.setProcessType(logModel.getOperTypeEnumId().toString().replace("40050227", "上级反馈审核") + "-通过");
                                } else {
                                    treeNode.setProcessType("退回");
//                                    treeNode.setProcessType(logModel.getOperTypeEnumId().toString().replace("40050227", "上级反馈审核") + "-退回");
                                }
                            } else {
                                treeNode.setProcessType(logModel.getOperTypeEnumId().toString().replace("40050227", "上级反馈审核").
                                        replace("40050253", "调度单派发").replace("40050257", "转派").replace("40050254", "反馈单拟稿").
                                        replace("40050439", "归档").replace("40050444", "取回").replace("40050255", "通过并反馈")
                                        .replace("40050992", "答复").replace("40050991", "提交建议"));
                            }
                        }
//                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                treeNode.setProcessOpinion(logModel.getOperDesc());
                treeNode.setNextCandidateUsers(logModel.getNextCandidateUsers());
                treeNode.setIpAddress(logModel.getIpAddress());

                /*String parentProcessInstID = logModel.getParentProInstId();
                if(parentProcessInstID == null || "".equals(parentProcessInstID)){
                    try {
                        parentProcessInstID = WorkflowAdapter.getProcessInstance(accountId, orderLogModels.get(i).getProcessInstId()).getParentProcessInstID();
                    } catch (AdapterException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }*/

                treeNode.setProcessingObjectTable(logModel.getProcessingObjectTable());

//                processInstID2treeNodeMap.put(logModel.getProcessInstId() , treeNode);

                if (logModel.getBelongCityCode() == null) {
                    if ((parentTreeNode = group2treeNodeMap.get(logModel.getBelongProvinceCode())) == null) {
                        parentTreeNode = new WorkFlowMonitorTreeNode();
                        parentTreeNode.setId(currentTreeNodeId + "-parent");
                        parentTreeNode.setOperateOrg(logModel.getBelongProvinceName());
                        trees.add(parentTreeNode);
                        group2treeNodeMap.put(logModel.getBelongProvinceCode(), parentTreeNode);
                    }
                } else {
                    WorkFlowMonitorTreeNode grandParTreeNode = group2treeNodeMap.get(logModel.getBelongProvinceCode());

                    if ((parentTreeNode = group2treeNodeMap.get(logModel.getBelongCityCode())) == null) {
                        parentTreeNode = new WorkFlowMonitorTreeNode();
                        parentTreeNode.setId(currentTreeNodeId + "-parent");
                        parentTreeNode.setOperateOrg(logModel.getOperFullOrgName());
                        group2treeNodeMap.put(logModel.getBelongCityCode(), parentTreeNode);
                        if (grandParTreeNode != null) {
                            parentTreeNode.setParentId(grandParTreeNode.getId());
                            grandParTreeNode.getChildren().add(parentTreeNode);
                        } else {
                            parentTreeNode.setOperateOrg(logModel.getOperFullOrgName());
                            trees.add(parentTreeNode);
                        }
                    }


                }

                treeNode.setParentId(parentTreeNode.getId());
                parentTreeNode.getChildren().add(treeNode);


//                trees.add(treeNode);

                currentTreeNodeId++;
            }
        }
        List newTree = transTrees(trees);
        tree.put("data", newTree);
        endHandle(request, response, JSON.toJSONString(tree), "", false);
    }

    private List transTrees(List<WorkFlowMonitorTreeNode> trees) {
        List<WorkFlowMonitorTreeNode> newTrees = new ArrayList();
        for (int i = 0; i < trees.size(); i++) {
            WorkFlowMonitorTreeNode treeNode = trees.get(i);
            newTrees.add(treeNode);
            List list = getChildren(treeNode);
            if (list != null && list.size() > 0) {
                newTrees.addAll(list);
            }
        }
        return newTrees;
    }

    private List<WorkFlowMonitorTreeNode> getChildren(WorkFlowMonitorTreeNode treeNode) {
        List<WorkFlowMonitorTreeNode> finalChildren = new ArrayList<WorkFlowMonitorTreeNode>();
        List<WorkFlowMonitorTreeNode> children = treeNode.getChildren();
        if (children.size() != 0) {
            for (int i = 0; i < children.size(); i++) {
                WorkFlowMonitorTreeNode node = children.get(i);
                finalChildren.add(node);
                List list = getChildren(node);
                finalChildren.addAll(list);
            }
        } else {
            return children;
        }
        return finalChildren;
    }

    private void addTreeNode(List<TreeNode> trees, WorkFlowMonitorTreeNode treeNode) {
        if ("".equals(treeNode.getParentId()) || treeNode.getParentId() == null) {
            trees.add(treeNode);
        } else {
            String parNodeId = treeNode.getParentId();
            for (int i = Integer.valueOf(parNodeId); i < trees.size(); i++) {
                WorkFlowMonitorTreeNode parTreeNode = (WorkFlowMonitorTreeNode) trees.get(i);
                if (parTreeNode.getId().equals(parNodeId)) {
                    trees.add(i + 1, treeNode);
                }
            }
        }
    }

    @RequestMapping(value = "/commWorkFlowMonitorController.do", params = "method=getUserEntityByUserNames")
    public void getUserEntityByUserNames(HttpServletRequest request, HttpServletResponse response, String userNames) throws UIException {

        List<String> userNameList = new ArrayList<String>();
        String[] userNamesArray = userNames.split(",");
        for (int i = 0; i < userNamesArray.length; i++) {
            userNameList.add(userNamesArray[i]);
        }

        List<UserEntity> list = null;
        try {
            list = AAAAAdapter.getInstence().findUserListByUserNames(userNameList);
            for (UserEntity userEntity : list) {
                userEntity.setOrgName(userEntity.getOrgEntity().getFullOrgName());
            }
        } catch (PaasAAAAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        endHandle(request, response, JSON.toJSONString(list), "", false);
    }

    @RequestMapping(value = "/commWorkFlowMonitorController.do", params = "method=getUserEntityByIds")
    public void getUserEntityByIds(HttpServletRequest request, HttpServletResponse response, String ids, String type) throws UIException {

        List<UserEntity> list = null;
        try {
            if ("name".equals(type)) {
                List<String> names = Arrays.asList(ids.split(","));
                list = AAAAAdapter.getInstence().findUserListByUserNames(names);
            } else {
                list = AAAAAdapter.getInstence().findUserListByUserIDs(ids);
                for (UserEntity userEntity : list) {
                    userEntity.setOrgName(userEntity.getOrgEntity().getOrgName());
                }
            }
        } catch (PaasAAAAException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        endHandle(request, response, JSON.toJSONString(list), "", false);
    }

    /**
     * 流程监控图形化
     *
     * @param request
     * @param response
     * @param rootProcessInstId
     * @param jobID
     * @return
     * @throws UIException
     */
    @RequestMapping(value = "/commGrapMonitor.do")
    public ModelAndView commGrapMonitor(HttpServletRequest request, HttpServletResponse response, String rootProcessInstId, String jobID) throws UIException {
        String accountId = getAccountId(request);

        List<GeneralInfoModel> orderLogModels = null;
        List<GeneralInfoModel> nowActivityList = null;

        Map<Integer, WorkFlowMonitorTreeNode> group2treeNodeMap = new HashMap<Integer, WorkFlowMonitorTreeNode>();

        int currentTreeNodeId = 0;
        WorkFlowMonitorTreeNode parentTreeNode = null;

        try {
//            rootProcessInstId = "145947";
            orderLogModels = workflowBaseService.getGeneralInfoByRootProcessId(rootProcessInstId);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        try {
            nowActivityList = workflowBaseService.getAllActivityInstanceInfos(rootProcessInstId, jobID, getUserEntity(request));
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Map<String, MonitorNode> monitorNodeMap = new HashMap<String, MonitorNode>();
        Map<String, List<MonitorNode>> processNodeMap = new HashMap<String, List<MonitorNode>>();

        List<MonitorNode> list = new ArrayList();

        String startProcessID = "";

        List<WorkFlowMonitorTreeNode> trees = new ArrayList();
        if (orderLogModels != null) {
            if (nowActivityList != null) {
                orderLogModels.addAll(nowActivityList);
            }

            for (int i = 0; i < orderLogModels.size(); i++) {
                MonitorNode monitorNode = new MonitorNode();

                GeneralInfoModel logModel = orderLogModels.get(i);

                int belongOrgId = logModel.getBelongCityCode() == null ? logModel.getBelongProvinceCode() : logModel.getBelongCityCode();
                String belongOrgName = logModel.getBelongCityName() == null ? logModel.getBelongProvinceName() : logModel.getBelongCityName();
                String processID = logModel.getProcessInstId();

                if (i == 0) {
                    startProcessID = logModel.getProcessInstId();
                }

                Integer operTypeEnumId = logModel.getOperTypeEnumId();
                if (logModel.getOperTypeEnumId() != null) {
                    try {
//                        EnumValue enumValue = EnumConfigAdapter.getInstence().getEnumValueById(logModel.getOperTypeEnumId());
//                        if (enumValue != null) {
//                            if (40050227 == logModel.getOperTypeEnumId() || 40050228 == logModel.getOperTypeEnumId()) {
//                                if (logModel.getProcessingStatus().equals(Constants.Y)) {
//                                    if (40050227 == logModel.getOperTypeEnumId() && StringUtils.isNotBlank(logModel.getAttribute1())) {
//                                        if ("up".equals(logModel.getAttribute1())) {
//                                            monitorNode.setLabel(enumValue.getEnumValueName() + "-上报");
//                                        } else if ("peer".equals(logModel.getAttribute1())) {
//                                            monitorNode.setLabel(enumValue.getEnumValueName() + "-同级审核");
//                                        }
//                                    } else {
//                                        monitorNode.setLabel(enumValue.getEnumValueName() + "-通过");
//                                    }
//                                } else {
//                                    monitorNode.setLabel(enumValue.getEnumValueName() + "-驳回");
//                                    monitorNode.setState("REJECT");
//                                }
//                            } else {
//                                monitorNode.setLabel(enumValue.getEnumValueName());
//                            }
//                        }
//优化建议增加匿名功能，根流程修改匿名
                        if (logModel.getProcessInstId().equals(logModel.getRootProInstId())) {
                            //判断是优化建议工单
                            if ("XJ_SUGGESTION_TABLE".equals(logModel.getProcessingObjectTable().toUpperCase())) {
                                logModel.setOperUserTrueName("匿名");
                            }
                        }
                        if ("XJ_FLOWORDER_TABLE".equals(logModel.getProcessingObjectTable().toUpperCase())) {
                            if (40050444 == logModel.getOperTypeEnumId()) {
                                monitorNode.setLabel(logModel.getActivityInstName() + "取回");
                            } else if (99990004 == logModel.getOperTypeEnumId()) {
                                monitorNode.setLabel(logModel.getActivityInstName() + "撤销");
                            } else {
                                if (Constants.Y.equals(logModel.getProcessingStatus())) {
                                    monitorNode.setLabel(logModel.getActivityInstName() + "-通过");
                                } else if (Constants.N.equals(logModel.getProcessingStatus())) {
                                    monitorNode.setLabel(logModel.getActivityInstName() + "-退回");
                                } else {
                                    monitorNode.setLabel(logModel.getActivityInstName());
                                }
                            }
                        } else {
                            if (40050227 == logModel.getOperTypeEnumId() || 40050228 == logModel.getOperTypeEnumId()) {
                                if (Constants.Y.equals(logModel.getProcessingStatus())) {
                                    monitorNode.setLabel("通过");
                                } else {
                                    monitorNode.setLabel("退回");
                                }
                            } else {
                                monitorNode.setLabel(logModel.getOperTypeEnumId().toString().replace("40050227", "上级反馈审核").
                                        replace("40050253", "调度单派发").replace("40050257", "转派").replace("40050254", "反馈单拟稿").
                                        replace("40050439", "归档").replace("40050444", "取回").replace("40050255", "通过并反馈")
                                        .replace("40050992", "答复").replace("40050991", "提交建议"));

                            }
                        }
//                        if (40050227 == logModel.getOperTypeEnumId() || 40050228 == logModel.getOperTypeEnumId()) {
//                            if (logModel.getProcessingStatus().equals(Constants.Y)) {
//                                monitorNode.setLabel(logModel.getOperTypeEnumId().toString().replace("40050227", "上级反馈审核").replace("40050253", "调度单拟稿") + "-通过");
//                            } else {
//                                monitorNode.setLabel(logModel.getOperTypeEnumId().toString().replace("40050227", "上级反馈审核") + "-驳回");
//                            }
//                        } else {
//                            monitorNode.setLabel(logModel.getOperTypeEnumId().toString().replace("40050227", "上级反馈审核").replace("40050253", "调度单拟稿").replace("40050257", "转派").replace("40050254", "反馈单拟稿").replace("40050439", "归档"));
//                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    monitorNode.setLabel(logModel.getActivityInstName());
                }

                List<MonitorNode> groupNodeList;
                if ((groupNodeList = processNodeMap.get(processID)) == null) {
                    //如果当前流程在map中没有节点
                    //说明此节点为该流程中第一个节点
                    //此时应寻找其父流程最后一个节点作为此节点的父节点
                    groupNodeList = new ArrayList<MonitorNode>();
                    processNodeMap.put(processID, groupNodeList);

                    String parentProcessID = logModel.getParentProInstId();
                    List<MonitorNode> parentNodeList = processNodeMap.get(parentProcessID);
                    if (parentNodeList != null && parentNodeList.size() > 0) {
                        MonitorNode parent = parentNodeList.get(parentNodeList.size() - 1);

                        parent.getNextNodes().add(monitorNode);
                        monitorNode.getPreNodes().add(parent);
                    }
                }

                StringBuffer monitorLabel = new StringBuffer();

                if (logModel.getObjectId() == null) {
                    //当操作动作为空，说明此处为待办

                    String state = "TODO";

                    monitorNode.setId(logModel.getProcessInstId() + "-" + logModel.getTaskInstId());
                    monitorNode.setOrgName(belongOrgName);

                    monitorLabel.append("<span style='position:absolute;top:5px;right:0;font-size:20px;' title='查看详情' class='glyphicon glyphicon-edit'></span>");
                    monitorLabel.append("<div><span class='glyphicon glyphicon-edit'></span>");
                    monitorLabel.append(logModel.getActivityInstName());

                    if (logModel.getReqFdbkTime() != null && System.currentTimeMillis() > logModel.getReqFdbkTime().getTime()) {
                        monitorLabel.append("(已超时)");
                        state = "DANGER";
                    }

                    monitorLabel.append("</div>");
                    monitorLabel.append("<div style='text-align:center;overflow:hidden;text-overflow:ellipsis;'><span class='glyphicon glyphicon-user'></span><a id='todo-users-");
                    monitorLabel.append(i);
                    monitorLabel.append("' onclick='__show_person_info(this)' style='color:#fff;' class='todo-users' usernames='");

                    String operUserTrueName = logModel.getOperUserTrueName();
                    StringBuffer operUserSB = new StringBuffer();
                    StringBuffer operUserNameSB = new StringBuffer();
                    String operUser = "";
                    String operUserName = "";
                    for (String userInfo : operUserTrueName.split(",")) {
                        int _index = userInfo.indexOf("||");
                        String trueName = userInfo.substring(0, _index);
                        String userName = userInfo.substring(_index + 2, userInfo.length());
                        operUserSB.append(trueName);
                        operUserSB.append(",");

                        operUserNameSB.append(userName);
                        operUserNameSB.append(",");
                    }

                    if (operUserSB.length() > 0) {
                        operUser = operUserSB.substring(0, operUserSB.length() - 1);
                        operUserName = operUserNameSB.substring(0, operUserNameSB.length() - 1);
                    }

                    monitorLabel.append(operUserName);
                    monitorLabel.append("'>");

                    monitorLabel.append(operUser);
//                    monitorLabel.append(operUserTrueName);
//                    monitorLabel.append(i);
                    monitorLabel.append("</a></div>");

                    if (logModel.getReqFdbkTime() != null) {
                        String reqFdbkTimeStr = logModel.getReqFdbkTime().toString();
                        reqFdbkTimeStr = reqFdbkTimeStr.substring(0, 19);
                        monitorLabel.append("<div style='text-align:right;text-decoration:underline;'><span class='glyphicon glyphicon-alert'></span>");
                        monitorLabel.append(reqFdbkTimeStr);
                        monitorLabel.append("</div>");
                    }

                    monitorNode.setLabel(monitorLabel.toString());
                    monitorNode.setState(state);
                } else {
                    monitorNode.setId(logModel.getProcessInstId() + "-" + operTypeEnumId);
                    monitorNode.setOrgName(logModel.getActivityInstName() + " - " + logModel.getOperFullOrgName());

                    monitorLabel.append("<span style='position:absolute;top:5px;right:0;font-size:20px;' title='查看详情' class='glyphicon glyphicon-open-file'></span>");
                    monitorLabel.append("<div><span class='glyphicon glyphicon-ok'></span>");
                    monitorLabel.append(monitorNode.getLabel());
                    monitorLabel.append("</div>");
                    monitorLabel.append("<div style='text-align:center'><span class='glyphicon glyphicon-user'></span><a class='user-card' userid='");
                    monitorLabel.append(logModel.getOperUserId());
                    monitorLabel.append("'>");
                    monitorLabel.append(logModel.getOperUserTrueName());
//                    monitorLabel.append(i);
                    monitorLabel.append("</a></div>");

                    if (logModel.getOperTime() != null) {
                        String operateTimeStr = logModel.getOperTime().toString();
                        operateTimeStr = operateTimeStr.substring(0, 19);
                        monitorLabel.append("<div style='text-align:right'><span class='glyphicon glyphicon-time'></span>");
                        monitorLabel.append(operateTimeStr);
                        monitorLabel.append("</div>");
                    }

                    if (logModel.getOperDesc() != null && !"".equals(logModel.getOperDesc())) {
                        monitorLabel.append("<div class='logModeloperDesc'><span class='glyphicon glyphicon-comment'></span>");
                        monitorLabel.append(logModel.getOperDesc());
                        monitorLabel.append("</div>");
                    }


                    monitorNode.setLabel(monitorLabel.toString());
                }

                monitorNode.setOrgId(belongOrgId);
                monitorNode.setProcessID(logModel.getProcessInstId());
                monitorNode.setParentProcessID(logModel.getParentProInstId());


                //判断之前是否存在该节点，如存在，修改ID
                MonitorNode oldNode = monitorNodeMap.get(monitorNode.getId());
                if (oldNode != null) {
                    oldNode.setId(oldNode.getId() + "-" + i);
                    monitorNodeMap.put(oldNode.getId(), oldNode);
                }

                groupNodeList.add(monitorNode);

//                if(monitorNodeMap.get(monitorNode.getId()) == null){
//                    list.add(monitorNode);
//                }
                monitorNodeMap.put(monitorNode.getId(), monitorNode);

            }


            Map<Integer, MonitorNode> positionMap = new HashMap<Integer, MonitorNode>();

            layoutNodes(startProcessID, 0, positionMap, processNodeMap, list);

//            //最后补齐没有preNodes的节点
//            if(list.size() > 0){
//                MonitorNode preNode =list.get(0);
//                for(int i = 1 ; i < list.size() ; i++){
//                    MonitorNode currNode =list.get(i);
//                    if(currNode.getPreNodes().size() == 0){
//                        currNode.getPreNodes().add(preNode);
//                    }
//
//                    preNode = currNode;
//                }
//            }


//            for(String key : processNodeMap.keySet()){
//                list.addAll(processNodeMap.get(key));
//            }

        }


        return new ModelAndView(new InternalResourceView("/workflow/wfMonitor.jsp")).addObject("list", list);
    }


    /**
     * @param startProcessID 开始分组
     * @param level          当前节点级别
     * @param positionMap    位置库
     * @param groupNodeMap   分组库
     * @param finalList      最终返回结果
     * @return
     */
    Object[] layoutNodes(String startProcessID, int level, Map<Integer, MonitorNode> positionMap, Map<String, List<MonitorNode>> groupNodeMap, List<MonitorNode> finalList) {

        List<MonitorNode> list = groupNodeMap.get(startProcessID);

        if (list == null) {
            return null;
        }

        int i = 0;
        for (; i < list.size(); i++) {

            MonitorNode monitorNode = list.get(i);

            finalList.add(monitorNode);

            if (monitorNode.getPositionY() == 0) {

                int currLevel = level++;

                monitorNode.setPositionY(currLevel);
                MonitorNode levelLast;
                int positionX = 0;
                if ((levelLast = positionMap.get(currLevel)) != null) {
//                    levelLast.setRightNode(monitorNode);

                    //当前节点与同级前一个节点相距不足3个单位距离时
                    //当前节点右移2个单位距离
                    if (monitorNode.getPositionX() - positionX < 3) {
                        positionX = levelLast.getPositionX();
                        positionX += 2;
                    }
                }

                monitorNode.setPositionX(monitorNode.getPositionX() + positionX);
                positionMap.put(currLevel, monitorNode);
            }

            if (monitorNode.getPreNodes().size() == 0 && i > 0) {
                MonitorNode preNode = list.get(i - 1);
                monitorNode.getPreNodes().add(preNode);
                monitorNode.setPositionX(preNode.getPositionX());
            }

            if (monitorNode.getNextNodes().size() > 0) {
                List<MonitorNode> nextNodeList = monitorNode.getNextNodes();

                MonitorNode next = null;
                if (i + 1 < list.size()) {
                    next = list.get(i + 1);
                }

                int maxDeep = 0;
                for (int j = 0; j < nextNodeList.size(); j++) {
                    Object[] result = layoutNodes(nextNodeList.get(j).getProcessID(), level, positionMap, groupNodeMap, finalList);

                    //计算最大深度
                    int deep = (Integer) result[0];
                    if (deep > maxDeep) {
                        maxDeep = deep;
                    }

                    //连接当前节点的下一个与子节点串中的最后一个
                    if (next != null) {
                        MonitorNode last = (MonitorNode) result[1];
                        next.getPreNodes().add(last);
                        last.getNextNodes().add(next);
                    }
                }

                level = maxDeep;

                if (nextNodeList.size() > 1) {
                    MonitorNode firstNext = nextNodeList.get(0);
                    MonitorNode lastNext = nextNodeList.get(nextNodeList.size() - 1);
                    monitorNode.movePositionXCascade((firstNext.getPositionX() + lastNext.getPositionX()) / 2 - monitorNode.getPositionX());
                }

                if (next != null) {
                    List<MonitorNode> preList = next.getPreNodes();
                    MonitorNode firstPre = preList.get(0);
                    if (preList.size() > 1) {
                        MonitorNode lastPre = preList.get(preList.size() - 1);
                        next.setPositionX((firstPre.getPositionX() + lastPre.getPositionX()) / 2);
                    } else {
                        next.setPositionX(firstPre.getPositionX());
                    }
                }
            }

        }

        return new Object[]{level, list.get(i - 1)};
    }

}
