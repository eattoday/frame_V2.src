package com.metarnet.core.common.adapter;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.model.ProcessParameterModel;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.utils.HttpClientUtil;
import com.metarnet.core.common.workflow.*;
import com.metarnet.core.common.workflow.ActivityInstance;
import com.metarnet.core.common.workflow.Participant;
import com.metarnet.core.common.workflow.ProcessInstance;
import com.metarnet.core.common.workflow.ProcessModelParams;
import com.metarnet.core.common.workflow.TaskFilter;
import com.metarnet.core.common.workflow.TaskInstance;
import com.metarnet.driver.WorkFlowAPI;
import com.metarnet.driver.bean.*;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jtwu
 * Date: 12-11-28
 * Time: 下午4:47
 * 工作流引擎代理，负责封装工作流引擎提供的接口。
 */
public class WorkflowAdapter {
    private static Logger logger = Logger.getLogger(WorkflowAdapter.class);
    public static Map<String, Map<String, Object>> relativeDataMap;


    public static void main(String[] args) {
        com.metarnet.driver.bean.TaskInstance taskInstanceObject = WorkFlowAPI.getTaskInstanceObject("", "36541", "");
        System.out.println(taskInstanceObject.toString());
    }
    /**
     * 1.启动流程
     *
     * @param accountId          当前登录ID
     * @param processModelID     流程模型ID
     * @param participant        参与者
     * @param bizModleParams     业务参数
     * @param processModelParams 流程模型对象参数
     * @return
     * @throws AdapterException
     */
    public static String startProcess(String accountId, String processModelID, Participant participant, Map<String,
            Object> bizModleParams, ProcessModelParams processModelParams) throws AdapterException {
        return WorkflowAdapter4Activiti.startProcess(accountId, processModelID, participant, bizModleParams, processModelParams);
    }


    /**
     * 2.查询待办
     *
     * @param taskFilter 待办查询实体
     * @param userName   当前登录ID
     * @return 待办列表
     * @throws AdapterException
     */

    public static List<TaskInstance> getMyWaitingTasks(TaskFilter taskFilter, String userName) throws AdapterException {
        return WorkflowAdapter4Activiti.getMyWaitingTasks(taskFilter, userName);
    }

    /**
     * 3.通过流程实例获取代办，用于刚起流程时查询代办
     *
     * @param processInstID
     * @param accountId
     * @return
     * @throws AdapterException
     */
    public static List<TaskInstance> queryNextWorkItemsByProcessInstID(String processInstID, String accountId) throws AdapterException {
        return WorkflowAdapter4Activiti.queryNextWorkItemsByProcessInstID(processInstID, accountId);
    }


    /**
     * 4.查询已办
     *
     * @param taskFilter 待办查询实体
     * @param accountId  当前登录ID
     * @return 待办列表
     * @throws AdapterException
     */

    public static List<TaskInstance> getMyCompletedTasks(TaskFilter taskFilter,
                                                         String accountId) throws AdapterException {
        return WorkflowAdapter4Activiti.getMyCompletedTasks(taskFilter, accountId);
    }

    /**
     * 5.查询已办--基于同一工单流水号、活动环节的合并
     *
     * @param accountId
     * @param taskFilter
     * @throws AdapterException
     */
    public static Pager getMyCompletedTasksDistinctJobId(TaskFilter taskFilter, String accountId) throws AdapterException {
        return WorkflowAdapter4Activiti.getMyCompletedTasksDistinctJobId(taskFilter, accountId);
    }

    /**
     * 6.查询已办--基于同于流程实例中相同处理人的已办合并
     *
     * @param accountId
     * @param taskFilter
     * @throws AdapterException
     */
    public static List<TaskInstance> getMyCompletedTasksDistinctProinstanceId(TaskFilter taskFilter, String accountId)
            throws AdapterException {
        return null;
    }

    /**
     * 7.根据活动实例ID获取任务实例ID
     *
     * @param accountId      用户ID
     * @param activityInstID 活动实例ID
     * @return
     * @throws AdapterException
     */
    public static List<TaskInstance> getTaskInstancesByActivityID(String accountId,
                                                                  String activityInstID) throws AdapterException {
        return WorkflowAdapter4Activiti.getTaskInstancesByActivityID(accountId, activityInstID);
    }

    /**
     * 8.提交待办
     *
     * @param accountId    当前登录ID
     * @param taskInstance 任务实例信息
     * @param participants 下一步执行人列表
     * @throws
     */
    public static void submitTask(String accountId, TaskInstance taskInstance, List<Participant> participants,String nextStep,String tenantId) throws
            AdapterException {
        WorkflowAdapter4Activiti.submitTask(accountId, taskInstance, participants, nextStep, tenantId);
    }
    public static void submitTask(String accountId, TaskInstance taskInstance, List<Participant> participants) throws
            AdapterException {
        WorkflowAdapter4Activiti.submitTask(accountId, taskInstance, participants);
    }

    /**
     * 9.传递数据
     *
     * @param processInstID 流程实例ID
     * @param relaDatas     设置更新内容
     */
    public static void setRelativeData(String processInstID, Map<String, Object> relaDatas, String accountId) throws AdapterException {
        WorkflowAdapter4Activiti.setRelativeData(processInstID, relaDatas, accountId);
    }

    /**
     * 10.获取数据
     *
     * @param processInstID 流程实例ID
     * @param keys          关键字
     */
    public static Map<String, Object> getRelativeData(String processInstID, List<String> keys,
                                                      String accountId) throws AdapterException {
        return WorkflowAdapter4Activiti.getRelativeData(processInstID, keys, accountId);
    }

    /**
     * 11.查询当前流程模板对应的所有流程环节信息；
     *
     * @param processModelID
     * @param accountId
     * @return
     * @throws AdapterException
     */
    public static List<ActivityDef> getActivitDefLists(String processModelID,
                                                       String accountId) throws AdapterException {
        return null;
    }

    /**
     * 12.获取流程实例流转过的活动
     *
     * @param accountId     当前用户
     * @param processInstID 流程实例ID
     * @return
     * @throws AdapterException
     */
    public static List<ActivityInstance> getActivityInstances(String accountId, String processInstID)
            throws AdapterException {
        return WorkflowAdapter4Activiti.getActivityInstances(accountId, processInstID);
    }

    /**
     * 13.转办
     *
     * @param accountId      当前登录ID
     * @param taskInstanceId 任务实例ID
     * @param participants   转办执行人
     */
    public static void forwardTask(String accountId, String taskInstanceId,
                                   List<Participant> participants) throws AdapterException {
        TaskInstance taskInstance=new TaskInstance();
        taskInstance.setTaskInstID(taskInstanceId);
        WorkflowAdapter4Activiti.forwardTask(accountId,taskInstance,participants);
    }


    /**
     * 14.协办
     *
     * @param accountId      当前登录ID
     * @param taskInstanceId 任务实例ID
     * @param participants   下一步执行人列表
     */
    public static void delegateTask(String accountId, String taskInstanceId,
                                    List<Participant> participants) throws AdapterException {

    }

    /**
     * 15.获取流程api接口
     *
     * @param accountId 当前用户
     * @return
     * @throws
     *
     */
//    public static WorkflowObjectInterface getWorkflowService(String accountId) throws WFException {
//        int _index;
//        if((_index = accountId.indexOf(",")) > -1){
//            String account = accountId.substring(0 , _index);
//            String tenantId = accountId.substring(_index + 1 , accountId.length());
//            return factory.getWorkflowService(account, tenantId, null);
//        } else {
//            return factory.getWorkflowService(accountId, Constants.APP_ID, null);
//        }
//    }

    /**
     * 16.业务描述：撤回任务，在对方没有提交之前进行撤回
     *
     * @param currentActivityInstId 当前活动实例ID
     * @param targetActivityInstId  需要回退/取回的目标活动实例ID
     */
    public static void backActivity(String accountId, String currentActivityInstId,
                                    String targetActivityInstId) throws AdapterException {
        WorkflowAdapter4Activiti.backActivity(accountId, currentActivityInstId, targetActivityInstId);
    }

    /**
     * 17.更新工单编号
     *
     * @param accountId
     * @throws AdapterException
     */
    public static void updateJobCodeInfo(String accountId, String processInstID,
                                         String jobCode) throws AdapterException {

    }


    /**
     * 18.根据任务实例ID获取当前实例扩展信息
     *
     * @param accountId      当前登录ID
     * @param processModelID 流程定义ID
     * @param activityDefID  环节定义ID
     * @return
     * @throws AdapterException
     */
    public static String getActivityExtendAttributes(String accountId, String processModelID,
                                                     String activityDefID) throws AdapterException {
        return null;
    }

    /**
     * 19.根据流程实例ID获取流程对象
     *
     * @param processInstId 流程实例ID
     * @param accountId     当前登录ID
     * @return
     * @throws AdapterException
     */
    public static ProcessInstance getProcessInstance(String accountId, String processInstId) throws AdapterException {
        return WorkflowAdapter4Activiti.getProcessInstance(accountId, processInstId);
    }


//

    /**
     * 20.根据流程模型ID获取第一个流程环节ID
     *
     * @param processModelID 流程模型ID
     * @param accountId      当前登录ID
     * @return
     */
    public static ActivityDef getStartActivity(String accountId, String processModelID) throws AdapterException {
        throw new AdapterException("");
//        try {
//            return getWorkflowService(accountId).getStartActivity(processModelID);
//        } catch (WFException e) {
//            throw new AdapterException(e);
//        }
    }

    /**
     * 21.获取流程实例的子流程
     *
     * @param accountId     当前登录ID
     * @param processInstId 流程实例ID
     * @return
     * @throws AdapterException
     */
    public static List<ProcessInstance> getSubProcessInstance(String accountId,
                                                              String processInstId) throws AdapterException {
        return WorkflowAdapter4Activiti.getSubProcessInstance(accountId , processInstId);
    }

    /**
     * 22.
     * @param accountId
     * @param taskInstId
     * @return
     * @throws AdapterException
     */
    public static TaskInstance getTaskInstanceObject(String accountId, String taskInstId) throws AdapterException {
        return WorkflowAdapter4Activiti.getTaskInstanceObject(accountId, taskInstId);
    }

    /**
     * 23.查询流程模板
     *
     * @param accountId
     * @return
     * @throws AdapterException
     */
    public static JSONObject getProcessModeLists(String accountId) throws AdapterException {
        JSONObject processModeLists = WorkflowAdapter4Activiti.getProcessModeLists(accountId);
        return processModeLists;

    }



    /**
     * 24.根据传入的processParameter 分解组装processParameter对象集合
     *
     * @param areacode    区域名称
     * @param orgcode     组织名称
     * @param majorcode   专业名称
     * @param productcode 产品名称
     * @return
     */
    public static String[] resolveProcsParameter(String areacode, String orgcode, String majorcode,
                                                 String productcode) throws AdapterException {

        List<ProcessParameterModel> processParameterModelList = new ArrayList<ProcessParameterModel>();
        try {
            if (null == areacode) {
                areacode = "";
            }
            if (null == orgcode) {
                orgcode = "";
            }
            if (null == majorcode) {
                majorcode = "";
            }
            if (null == productcode) {
                productcode = "";
            }

            String[] areacode_ = areacode.split(",");
            String[] orgcode_ = orgcode.split(",");
            String[] majorcode_ = majorcode.split(",");
            String[] productcode_ = productcode.split(",");

            for (int a = 0; a < areacode_.length; a++) {
                for (int o = 0; o < orgcode_.length; o++) {
                    if (StringUtils.isBlank(orgcode_[o])) {
                        continue;
                    }
                    for (int m = 0; m < majorcode_.length; m++) {
                        for (int p = 0; p < productcode_.length; p++) {
                            ProcessParameterModel processParameterModel = new ProcessParameterModel();
                            Set<String> orgcodeSet = new HashSet<String>();//
                            orgcodeSet.add(orgcode_[o]);
                            processParameterModel.setOrgcode(orgcodeSet); //组织名称
                            Set<String> areacodeSet = new HashSet<String>();//
                            areacodeSet.add(areacode_[a]);
                            processParameterModel.setAreacode(areacodeSet);
                            Set<String> majorcodeSet = new HashSet<String>();//
                            majorcodeSet.add(majorcode_[m]);
                            processParameterModel.setMajorcode(majorcodeSet);
                            Set<String> productcodeSet = new HashSet<String>();//
                            productcodeSet.add(productcode_[p]);
                            processParameterModel.setProductcode(productcodeSet);
                            processParameterModelList.add(processParameterModel);
                        }
                    }
                }
            }

            String[] processParameterJson = new String[processParameterModelList.size()];
            for (int pa = 0; pa < processParameterModelList.size(); pa++) {
                processParameterJson[pa] = JSONArray.toJSONString(processParameterModelList.get(pa));
            }
            return processParameterJson;
        } catch (Exception e) {
            throw new AdapterException(e);
        }
        //return JSONArray.toJSONString(processParameterModelList);
    }

    /**
     * 25.查询待阅
     *
     * @param notificationFilter 通知查询过滤条件
     * @param accountId          当前登录ID
     * @return 待阅信息列表
     * @throws AdapterException
     */
    public static List<NotificationInstance> getMyUnreadNotifications(NotificationFilter notificationFilter,
                                                                      String accountId) throws AdapterException {
        return null;
    }

    /**
     * 26.查询已阅
     *
     * @param notificationFilter 通知查询过滤条件
     * @param accountId          当前登录ID
     * @return 已阅信息列表
     * @throws AdapterException
     */
    public static List<NotificationInstance> getMyReadNotifications(NotificationFilter notificationFilter,
                                                                    String accountId) throws AdapterException {
        return null;
    }

    /**
     * 27.更新待阅为已阅读
     *
     * @param notificationInstId 通知对象实例IDt
     * @param accountId          当前登录ID
     * @return 已阅信息列表
     * @throws AdapterException
     */
    public static void setNotificationToRead(String notificationInstId, String accountId) throws AdapterException {


    }

    /**
     * 28.
     * @param notificationfilter
     * @param state
     * @param accountId
     * @return
     * @throws AdapterException
     */
    private static List<NotificationInstance> getNotifications(NotificationFilter notificationfilter, String state, String accountId) throws AdapterException {


        return null;
    }

    /**
     * 29.任务/通知抄送
     *
     * @param taskInstId   任务id
     * @param participants 用户对象列表
     * @param userId       当前登录ID
     */
    public static void ccTask(String taskInstId, List<Participant> participants,
                              String userId) throws AdapterException {

    }

    /**
     * 30.任务认领
     *
     * @param taskInstId 任务id
     * @param accountId  当前登录ID
     * @throws AdapterException
     */
    public static void claimTask(String taskInstId, String accountId) throws AdapterException {

    }

    /**
     * 31.动态启动并挂接子流程到父流程
     *
     * @param processDefName      子流程模版名称
     * @param processInstName     子流程实例名称
     * @param parentProcessInstID 父流程实例id
     * @param actInstID           父流程环节实例id
     * @param params              子流程启动参数
     * @throws AdapterException
     */
    public static String addAndStartProcessWithParentActivityInstID(String processDefName, String processInstName,
                                                                    String parentProcessInstID, String actInstID,
                                                                    LinkedHashMap<String, Object> params,
                                                                    String accountId) throws AdapterException {
        return null;
    }

    /**
     * 32.根据流程实例id和环节定义id查找正在运行的环节实例id
     *
     * @param processInstID 流程实例ID
     * @param activityDefID 环节定义ID
     * @return
     */
    public static ActivityInstance findActivityInstByActivityDefID(String processInstID, String activityDefID,
                                                                   String accountId) throws AdapterException {
        return null;
    }

    /**
     * 18.1	通过当前环节实例ID，找到后继可能到达的活动的角色
     *
     * @param processInstID  流程实例ID
     * @param activityInstID 活动实例ID
     * @param accountId
     * @param parameters     可选 所需的维度参数值 应用可以指定值，如果某个维度没有指定，则流程引擎使用流程模板中的配置值
     * @return
     */
    public static List<Participant> getNextActivitiesMaybeRoles(String processInstID, String activityInstID,
                                                                String accountId, LinkedHashMap parameters) throws
            AdapterException {
        return null;
    }

    /**
     * 42  查询出当前环节的后续环节可能的参与者
     *
     * @param processInstID         流程实例ID
     * @param currentActivityInstID 当前环节实例 id
     * @param activityID            要查询的流程环 节定义 id（一般指 下环节的环节 id）
     * @param accountId
     * @param parameters            可选 所需的维度参数值 应用可以指定值，如果某个维度没有指定，则流程引擎使用流程模板中的配置值
     * @return
     */
    public static List<Participant> getProbableParticipants(String processInstID, String currentActivityInstID,
                                                            String activityID, String accountId,
                                                            LinkedHashMap parameters) throws AdapterException {
        return null;
    }

    /**
     * 43.	获取指定环节的参与者
     *
     * @param processModelID 流程定义ID
     * @param activityID     环节定义 id
     * @param accountId
     * @param parameters     可选 所需的维度参数值 应用可以指定值，如果某个维度没有指定，则流程引擎使用流程模板中的配置值
     * @return
     */
    public static List<Participant> getActivityParticipants(String processModelID, String activityID,
                                                            LinkedHashMap parameters,
                                                            String accountId) throws AdapterException {
        return null;
    }

    /**
     * 33.通过当前环节实例ID，找到后继可能到达的活动定义
     *
     * @param activityInstID 活动实例ID
     */
    public static List<ActivityDef> getNextActivitiesMaybeArrived(String activityInstID,
                                                                  String accountId) throws AdapterException {
        return null;
    }

    /**
     * 34.回退到指定的步骤
     *
     * @param accountId
     * @param processId             当前流程id
     * @param currentActivityInstId 当前活动实例id
     * @param activityDefID         要回退环节的活动定义id
     * @throws AdapterException
     */

    public static void backTargetActivity(String accountId, String processId, String currentActivityInstId, String activityDefID
    ) throws AdapterException {
        try {
            String targetActivityInstId = "";
            //获得当前流程流转过的所有活动
            List<ActivityInstance> activityInstances = getActivityInstances(accountId, processId);
            //遍历所有活动如果有与传过来的定义id相同的才执行回退
            for (ActivityInstance activityInstance : activityInstances) {
                String defId = activityInstance.getActivityDefID();
                if ((activityDefID).equals(defId)) {
                    targetActivityInstId = activityInstance.getActivityInstID();
                }
            }
            if (!"".equals(targetActivityInstId)) {
                backActivity(accountId, currentActivityInstId, targetActivityInstId);
            }
        } catch (Exception e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 35.撤销流程
     *
     * @param processInstID 流程实例ID
     */
    public static void terminateProcessInstance(String processInstID, String accountId) throws AdapterException {

    }

    /**
     * 36.更新工单主题
     *
     * @param processInstID 流程实例
     * @param jobTilt       工单主题
     * @param accountId
     * @throws AdapterException
     */
    public static void updateJobTitleInfo(String processInstID, String jobTilt,
                                          String accountId) throws AdapterException {

    }

    /**
     * 37.工作项实例ID
     *
     * @param taskInstanceId 任务实例Id
     * @param accountId
     * @throws AdapterException
     */
    public static void drawbackWorkItem(long taskInstanceId, String accountId) throws AdapterException {

    }

    /**
     * 38.根据工作项ID判断该工作项是否可以拽回
     *
     * @param taskInstanceId 任务实例Id
     * @param accountId
     * @return
     * @throws AdapterException
     */
    public static boolean isDrawbackEnable(long taskInstanceId, String accountId) throws AdapterException {
        return false;
    }

    /**
     * 39.根据工作项ID，活动定义ID删除工作项被指定的参与者。
     *
     * @param taskInstanceId 任务实例Id
     * @param activityDefID  环节定义ID
     * @param accountId
     * @throws AdapterException
     */
    public static void clearAppointedActivityParticipants(long taskInstanceId, String activityDefID,
                                                          String accountId) throws AdapterException {

    }

    /**
     * 48.	根据环节实列结束指定环节
     *
     * @param activityInstID 流程环节实例ID
     */

    public static void finishActivityInstance(String activityInstID, String accountId) throws AdapterException {

    }

    /**
     * 40.根据流程实例ID获取当前流程正在处理的工作项参与
     *
     * @param processInstID
     * @param accountId
     * @return
     * @throws AdapterException
     */
    public static List<Participant> findDoingParticipant(String processInstID,
                                                         String accountId) throws AdapterException {
        return null;
    }

    /**
     * 41.重启已完成的流程实例（未转历史），激活指定活动并指派参与者。
     *
     * @param processInstID 流程实例ID
     * @param activityDefID 活动定义ID
     * @param participants  参与值列表
     * @return
     * @throws AdapterException
     */

    public static String restartFinishedProcessInst(String accountId, long processInstID, String activityDefID,
                                                    List<Participant> participants) throws AdapterException {
        return null;
    }

    /**
     * 42.挂起流程
     *
     * @param processInstID 流程实例ID
     * @throws AdapterException
     */
    public static void suspendProcessInstance(long processInstID, String accountId) throws AdapterException {

    }


    /**
     * 43.激活流程
     *
     * @param processInstID 流程实例ID
     * @throws AdapterException
     */
    public static void resumeProcessInstance(long processInstID, String accountId) throws AdapterException {

    }

    /**
     * 44.获取根流程实例id
     *
     * @param processInstanceId 流程实例ID
     */
    public static ProcessInstance getRootProcessInstance(String accountId, String processInstanceId) throws AdapterException {
        try {
            ProcessInstance processInstance = getProcessInstance(accountId, processInstanceId);
            if ("0".equals(processInstance.getParentProcessInstID())) {
                return processInstance;
            }
            return getRootProcessInstance(accountId, processInstance.getParentProcessInstID());
        } catch (AdapterException e) {
            throw new AdapterException(e);
        }
    }

    /**
     * 45.激活活动实例
     *
     * @param accountId
     * @param activityInstID
     * @throws AdapterException
     */
    public static void restartActivityInstance(String accountId, String activityInstID) throws AdapterException {

    }

    /**
     * 46.启动一个未开始的环节
     *
     * @param accountId
     * @param processInstID
     * @param activityDefID
     * @throws AdapterException
     */
    public static void createAndStartActivityInstance(String accountId, String processInstID, String activityDefID) throws AdapterException {

    }

    /**
     * 47.终止活动实例
     *
     * @param accountId
     * @param activityInstID
     * @throws AdapterException
     */
    public static void terminateActivityInstance(String accountId, String activityInstID) throws AdapterException {

    }


    /**
     * 49
     * @param specialty
     * @param orgID
     * @param orgName
     * @param process
     * @param node
     * @param flag
     * @return
     */
    public static String findNextParticipant(String specialty, String orgID, String orgName, String process, String node, Boolean flag) {
        Map<String, String> paramsMap = new HashMap<String, String>();
        List<Participant> participants = new ArrayList<Participant>();
        paramsMap.put("specialty", specialty);
       try {
            paramsMap.put("orgID", AAAAAdapter.getCompany(Integer.parseInt(orgID)).getOrgId().toString());
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        }
        paramsMap.put("process", process);
        paramsMap.put("node", node);
        paramsMap.put("flag", String.valueOf(flag));
        String userAccountIds = HttpClientUtil.sendPostRequestByJava(Constants.POWERURL + "/powerController.do?method=findNextParticipant2", paramsMap);

        if (userAccountIds != null && !"".equals(userAccountIds)) {
            List list = JSON.parseArray(userAccountIds);
            for (Object userAccountId : list) {
                Participant participant = new Participant();
                participant.setParticipantID(String.valueOf(userAccountId));
                participant.setParticipantName(orgID + ":" + orgName);
                participant.setParticipantType("1");
                participants.add(participant);
            }
        }
        return JSON.toJSONString(participants);
    }

    //这两个方法基本上无用,在controller层进行orgname和flag的判断即可
    public static String findNextParticipant(String specialty, String orgID, String process, String node, Boolean flag) {
        return findNextParticipant(specialty, orgID, "", process, node, flag);
    }
    public static String findNextParticipant(String specialty, String orgID, String process, String node) {
        return findNextParticipant(specialty, orgID, process, node, false);
    }


    /**
     * 50.
     * @param specialty 专业    z
     * @param orgID     组织id         z
     * @param process   流程定义id
     * @param node      流程活动定义id         l
     * @return
     */
    public static List<String> findNextParticipantToChildInstance(String specialty, String orgID, String process, String node) {
        return findNextParticipantToChildInstance(specialty, orgID, "", process, node);
    }
    public static List<String> findNextParticipantToChildInstance(String specialty, String orgID, String orgName, String process, String node) {
        Map<String, String> paramsMap = new HashMap<String, String>();
        List<Participant> participants = new ArrayList<Participant>();
        paramsMap.put("specialty", specialty);
        try {
            paramsMap.put("orgID", AAAAAdapter.getCompany(Integer.parseInt(orgID)).getOrgId().toString());
        } catch (PaasAAAAException e) {
            e.printStackTrace();
        }
        paramsMap.put("process", process);
        paramsMap.put("node", node);
        String userAccountIds = HttpClientUtil.sendPostRequestByJava(Constants.POWERURL + "/powerController.do?method=findNextParticipant2", paramsMap);
        ;
        List<String> listInfo = new ArrayList<String>();
        if (userAccountIds != null && !"".equals(userAccountIds)) {
            List list = JSON.parseArray(userAccountIds);
            for (Object userAccountId : list) {
                Participant participant = new Participant();
                participant.setParticipantID(String.valueOf(userAccountId));
                participant.setParticipantName(orgID + ":" + orgName);
                participant.setParticipantType("1");
                participants.add(participant);
                listInfo.add(JSON.toJSONString(participants));
                participants = new ArrayList<Participant>();
            }
        }
        return listInfo;
    }

    /**
     * 51.
     * @param accountId
     * @param jobId
     * @return
     */
    public static String findDoingActivitysByJobID(String accountId, String jobId) {
        try {
            return WorkflowAdapter4Activiti.findDoingActivitysByJobID(accountId, jobId);
        } catch (AdapterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 52.
     * @param accountId
     * @param jobId
     * @return
     */
    public static String findHisDoingActivitysByJobID(String accountId, String jobId) {
        try {
            return WorkflowAdapter4Activiti.findHisDoingActivitysByJobID(accountId, jobId);
        } catch (AdapterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 53.
     * @param processInstID
     * @return
     */
    public static List queryActivityInstsByProcessInstID(long processInstID) {
        return null;
    }

    /**
     * 54.
     * @param tenantId
     * @return
     */
    public static List queryProcessDefByBizCatalogUUID(String tenantId) {
        return null;
    }

    /**
     * 55.
     * @param tenantId
     * @return
     */
    public static List queryCatalogs(String tenantId) {
        return null;
    }

    /**
     * 56.
     * @param tenantId
     * @param catalogUUID
     * @return
     */
    public static List queryProcessDefByBizCatalogUUID(String tenantId, String catalogUUID) {
        return null;
    }

    /**
     * 57.
     * @param tenantId
     * @param processDefId
     * @return
     */
    public static List queryActivitiesOfProcess(String tenantId, String processDefId) {
        return null;
    }
}