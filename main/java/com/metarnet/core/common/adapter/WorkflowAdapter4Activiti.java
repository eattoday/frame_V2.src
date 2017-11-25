package com.metarnet.core.common.adapter;

import com.alibaba.fastjson.util.TypeUtils;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.workflow.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.activemq.store.kahadb.disk.page.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * WorkflowAdapter之Activiti实现
 * 此类采用rest api
 * 后续可支持嵌入api
 */
@SuppressWarnings({"unchecked", "Convert2Diamond"})
public class WorkflowAdapter4Activiti {

    private static Logger logger = Logger.getLogger(WorkflowAdapter4Activiti.class);
    private static final String BIZ = "BIZ.";
    private static final String PROC = "PROC.";
    private static final String URI = Constants.activi_rest_url;
    private static RestTemplate restTemplate = new RestTemplate();
    static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


    private static String getIdFromUri(String uri) {
        //http://121.28.209.22:60080/activiti-rest/runtime/process-instances/5
        return StringUtils.substringAfterLast(uri, "/");
    }


    /**
     * 1.启动流程
     * @param accountId
     * @param processModelID
     * @param participant
     * @param bizModleParams
     * @param processModelParams
     * @return
     * @throws AdapterException
     */
    public static String startProcess(String accountId, String processModelID, Participant participant, Map<String, Object> bizModleParams, ProcessModelParams processModelParams) throws AdapterException {
        JSONObject request = new JSONObject(), response;
        JSONArray variables = new JSONArray();
        //流程启动者，自定义变量，需要在流程图中设置initiator，例如activiti:initiator="${initiator}"
        variables.add(new JSONObject().element("name", "initiator").element("value", accountId));
        //设置方法activiti:candidateUsers="${   candidateUsers}"，多人逗号隔开
        variables.add(new JSONObject().element("name", "candidateUsers").element("value", participant.getParticipantID()));
        //业务变量，定义一个规则BIZ.开头的
        for (Map.Entry<String, Object> entry : bizModleParams.entrySet()) {
            variables.add(new JSONObject().element("name", BIZ + entry.getKey()).element("value", entry.getValue()));
        }
        //流程参数，定义一个规则PROC.开头的
        for (Map.Entry<String, Object> entry : processModelParams.getParameters().entrySet()) {
            variables.add(new JSONObject().element("name", PROC + entry.getKey()).element("value", entry.getValue()));
        }
        //流程定义
        request.put("processDefinitionKey", processModelID);
        //业务实例id，用于关联业务
        request.put("businessKey", bizModleParams.get("jobID") + "");
        //流程变量
        request.put("variables", variables);
        try {
            response = restTemplate.postForObject(URI + "/runtime/process-instances", request, JSONObject.class);
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
        return response.getString("id");
    }

    /**
     * 2.获取待办
     * @param taskFilter
     * @param userName
     * @return
     * @throws AdapterException
     */
    public static List<TaskInstance> getMyWaitingTasks(TaskFilter taskFilter, String userName) throws AdapterException {

        PageCondition pageCondition = taskFilter.getPageCondition();
        if (pageCondition == null) {
            pageCondition = new PageCondition();
            pageCondition.setBegin(0);
            pageCondition.setLength(10);
            taskFilter.setPageCondition(pageCondition);
        }

        JSONObject request = new JSONObject(), response;
        if (StringUtils.isNotEmpty(userName))
            request.put("candidateUser", userName);
        request.put("includeProcessVariables", true);
        request.put("processInstanceId", taskFilter.getProcessInstID());
        request.put("start", taskFilter.getPageCondition().getBegin());
        request.put("size", taskFilter.getPageCondition().getLength());
        request.put("order", "desc");
        request.put("sort", "createTime");
        request.put("includeProcessVariables", "true");
        try {
            response = restTemplate.postForObject(URI + "/query/tasks", request, JSONObject.class);
            return jsonArray2TaskInstance(response.getJSONArray("data"));
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new AdapterException("other error", e);
        }
    }


    /**
     *
     * @param taskFilter
     * @param userName
     * @return
     * @throws AdapterException
     */
    public static Pager getMyWaitingTasksDistinctJobId(TaskFilter taskFilter, String userName) throws AdapterException {
        Pager pager = new Pager();
        PageCondition pageCondition = taskFilter.getPageCondition();
        if (pageCondition == null) {
            pageCondition = new PageCondition();
            pageCondition.setBegin(0);
            pageCondition.setLength(10);
            taskFilter.setPageCondition(pageCondition);
        }

        JSONObject request = new JSONObject(), response;
        request.put("candidateUser", userName);
        request.put("includeProcessVariables", true);
        request.put("processInstanceId", taskFilter.getProcessInstID());
        request.put("start", taskFilter.getPageCondition().getBegin());
        request.put("size", taskFilter.getPageCondition().getLength());
        request.put("includeProcessVariables", "true");
        request.put("order", "desc");
        request.put("sort", "createTime");
        try {
            response = restTemplate.postForObject(URI + "/custom/query/tasks", request, JSONObject.class);
            pager.setExhibitDatas(jsonArray2TaskInstance(response.getJSONArray("data")));
            pager.setRecordCount(Integer.parseInt(response.get("total").toString()));
            return pager;
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new AdapterException("other error", e);
        }
    }

    /**
     * 3.根据流程ID查询待办
     * @param processInstID
     * @param accountId
     * @return
     * @throws AdapterException
     */
    public static List<TaskInstance> queryNextWorkItemsByProcessInstID(String processInstID, String accountId) throws AdapterException {
        JSONObject request = new JSONObject(), response;
        request.put("processInstanceId", processInstID);
        request.put("candidateUser", accountId);
        try {
            response = restTemplate.postForObject(URI + "/query/tasks", request, JSONObject.class);
            return jsonArray2TaskInstance(response.getJSONArray("data"));
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new AdapterException("other error", e);
        }
    }

    /**
     * 4.获取已办
     * @param taskFilter
     * @param accountId
     * @return
     * @throws AdapterException
     */
    public static List<TaskInstance> getMyCompletedTasks(TaskFilter taskFilter, String accountId) throws AdapterException {
        JSONObject request = new JSONObject(), response;
        if (taskFilter.getProcessInstID() != null)
            request.put("processInstanceId", taskFilter.getProcessInstID());
        if (StringUtils.isNotEmpty(accountId))
            request.put("taskAssignee", accountId);
        request.put("includeProcessVariables", true);
        request.put("order", "desc");
        request.put("start", taskFilter.getPageCondition().getBegin());
        request.put("size", taskFilter.getPageCondition().getLength());
        try {
            response = restTemplate.postForObject(URI + "/query/historic-task-instances", request, JSONObject.class);
            return jsonArray2TaskInstance(response.getJSONArray("data"));
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new AdapterException("other error", e);
        }
    }

    /**
     *
     * @param taskFilter
     * @param accountId
     * @return
     * @throws AdapterException
     */
    public static Pager getMyCompletedTasksDistinctJobId(TaskFilter taskFilter, String accountId) throws AdapterException {
        Pager pager = new Pager();
        //合并已办
        JSONObject request = new JSONObject(), response;
        request.put("taskAssignee", accountId);
        request.put("includeProcessVariables", true);
        request.put("start", taskFilter.getPageCondition().getBegin());
        request.put("size", taskFilter.getPageCondition().getLength());

        request.put("sort", "id");
        try {
            response = restTemplate.postForObject(URI + "/custom/query/historic-task-instances", request, JSONObject.class);
            pager.setExhibitDatas(jsonArray2TaskInstance(response.getJSONArray("data")));
            pager.setRecordCount(Integer.parseInt(response.get("total").toString()));
            return pager;
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new AdapterException("other error", e);
        }
    }

    /**
     * 7.根据活动实例ID获取任务实例
     * @param accountId
     * @param activityInstID
     * @return
     * @throws AdapterException
     */
    public static List<TaskInstance> getTaskInstancesByActivityID(String accountId, String activityInstID) throws AdapterException {
        JSONObject response = new JSONObject();
        try {
            response = restTemplate.getForObject(URI + "/history/historic-activity-instances?activityInstanceId=" + activityInstID, JSONObject.class);
            return jsonArray2ActivityInstance(response.getJSONArray("data"));
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new AdapterException("other error", e);
        }
    }

    //        //更新任务负责人
//        try {
//            request.put("action", "delegate");
//            request.put("assignee", "");
//            restTemplate.postForObject(URI + "/runtime/tasks/" + taskInstance.getTaskInstID(), request, JSONObject.class);
//        } catch (HttpStatusCodeException e) {
//            throw new AdapterException(e.getResponseBodyAsString(), e);
//        }

//        //设置候选人列表
//        try {
//            request.put("value", "ght");
//            request.put("name", "candidateUsers");
//            request.put("type", "string");
//            request.put("scope", "global");
//            System.out.println(request.toString());
//            restTemplate.put(URI + "/runtime/tasks/" + taskInstance.getTaskInstID()+"/variables/candidateUsers", request, JSONObject.class);
//        } catch (HttpStatusCodeException e) {
//            throw new AdapterException(e.getResponseBodyAsString(), e);
//        }

    /**
     * 13.转办
     * @param accountId
     * @param taskInstance
     * @throws AdapterException
     */
    public static void forwardTask(String accountId, TaskInstance taskInstance,  List<Participant> participants) throws AdapterException {
        JSONObject request = new JSONObject(), response;
        try {
            try {
                 restTemplate.delete(URI + "/runtime/tasks/" + taskInstance.getTaskInstID()+"/identitylinks/users/"+accountId+"/candidate");
            }catch (Exception e){
                e.printStackTrace();
            }
            for (Participant p:participants){
                request.put("user", p.getParticipantID());
                request.put("type", "candidate");
                try {
                    restTemplate.postForObject(URI + "/runtime/tasks/" + taskInstance.getTaskInstID()+"/identitylinks", request, JSONObject.class);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    request.clear();
                }
            }
        } catch (HttpStatusCodeException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
    }


    /**
     * 8 提交待办
     * @param accountId
     * @param taskInstance
     * @param participants
     * @param nextStep
     * @param tenantId
     * @throws AdapterException
     */
    public static void submitTask(String accountId, TaskInstance taskInstance, List<Participant> participants,
                                  String nextStep,String tenantId) throws AdapterException {

        JSONObject request = new JSONObject(), response;

        //由当前用户认领任务
        try {
            request.put("action", "claim");
            request.put("assignee", accountId);
            restTemplate.postForObject(URI + "/runtime/tasks/" + taskInstance.getTaskInstID(), request, JSONObject.class);
        } catch (HttpStatusCodeException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
        logger.info("===认领任务===");

        //设置候选人集合
        List<String> candidateUser = new ArrayList<String>();
        if (participants != null) {
            for (Participant participant : participants) {
                candidateUser.add(participant.getParticipantID());
            }
        }

        JSONArray variables = new JSONArray();
        variables.add(new JSONObject().element("name", "candidateUsers").element("value", StringUtils.join(candidateUser.toArray(), ",")));
        //通过setRelativeData()设置
        variables.add(new JSONObject().element("name", "approved").element("value", true));
        variables.add(new JSONObject().element("name", "days").element("value", 4));

        //记录通用处理信息
        variables.add(new JSONObject().element("name", "nextStep").element("value", nextStep));
        variables.add(new JSONObject().element("name", "tenantId").element("value", tenantId));


        //向rest发送请求的参数
        request.put("action", "complete");
        request.put("variables", variables);
        try {
            restTemplate.postForObject(URI + "/runtime/tasks/" + taskInstance.getTaskInstID(), request, JSONObject.class);
        } catch (HttpStatusCodeException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
        logger.info("===完成任务===");
    }

    /**
     * 提交待办 旧版
     * @param accountId
     * @param taskInstance
     * @param participants
     * @throws AdapterException
     */
    public static void submitTask(String accountId, TaskInstance taskInstance, List<Participant> participants
                                  ) throws AdapterException {
        JSONObject request = new JSONObject(), response;

        try {
            request.put("action", "claim");
            request.put("assignee", accountId);
            restTemplate.postForObject(URI + "/runtime/tasks/" + taskInstance.getTaskInstID(), request, JSONObject.class);
        } catch (HttpStatusCodeException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
        logger.info("===认领任务===");
        List<String> candidateUser = new ArrayList<String>();
        if (participants != null) {
            for (Participant participant : participants) {
                candidateUser.add(participant.getParticipantID());
            }
        }
        JSONArray variables = new JSONArray();
        variables.add(new JSONObject().element("name", "candidateUsers").element("value", StringUtils.join(candidateUser.toArray(), ",")));
        //通过setRelativeData()设置
        variables.add(new JSONObject().element("name", "approved").element("value", true));
        variables.add(new JSONObject().element("name", "days").element("value", 4));

        request.put("action", "complete");
        request.put("variables", variables);
        try {
            restTemplate.postForObject(URI + "/runtime/tasks/" + taskInstance.getTaskInstID(), request, JSONObject.class);
        } catch (HttpStatusCodeException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
        logger.info("===完成任务===");
    }

    /**
     * 9.设置相关数据
     * @param processInstID
     * @param relaDatas
     * @param accountId
     * @throws AdapterException
     */
    public static void setRelativeData(String processInstID, Map<String, Object> relaDatas, String accountId) throws AdapterException {
        JSONArray request = new JSONArray(), response;
        for (Map.Entry entry : relaDatas.entrySet()) {
            Object name = entry.getKey();
            Object value = entry.getValue();
            Object type = "string";
            if (value instanceof Collection<?>) {

                Map map = new HashMap();
                value = JSONArray.fromObject(value);
                map.put("name", name);
                map.put("value", value.toString());
                map.put("type", "list");
                request.add(map);
            } else if (value instanceof Date) {
                request.add(new JSONObject().element("name", name).element("value", value).element("type", "date"));
            } else if (value instanceof Integer) {
                request.add(new JSONObject().element("name", name).element("value", value).element("type", "integer"));
            } else if (value instanceof Boolean) {
                request.add(new JSONObject().element("name", name).element("value", value).element("type", "boolean"));
            } else {
                request.add(new JSONObject().element("name", name).element("value", value).element("type", type));
            }
        }
        try {
//            response = restTemplate.postForObject(URI + "/runtime/process-instances/" + processInstID + "/variables", request, JSONArray.class);
            restTemplate.put(URI + "/runtime/process-instances/" + processInstID + "/variables", request);
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
    }

    /**
     * 10.获取相关数据
     * @param processInstID
     * @param keys
     * @param accountId
     * @return
     * @throws AdapterException
     */
    public static Map<String, Object> getRelativeData(String processInstID, List<String> keys, String accountId) throws AdapterException {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String key : keys) {
            JSONObject request = new JSONObject().element("processInstanceId", processInstID).element("variableName", key);
            JSONObject response = new JSONObject();
            try {
                response = restTemplate.postForObject(URI + "/query/historic-variable-instances", request, JSONObject.class);
                if (response.getJSONArray("data").size() > 0) {
                    JSONObject variable = response.getJSONArray("data").getJSONObject(0).getJSONObject("variable");
                    if (variable.containsKey("name") && variable.containsKey("value"))
                        map.put(variable.getString("name"), variable.getString("value"));
                }
            } catch (HttpClientErrorException e) {
                throw new AdapterException(e.getResponseBodyAsString(), e);
            }
        }
        return map;
    }

    /**
     * 44.获取根流程实例
     * @param accountId
     * @param processInstanceId
     * @return
     * @throws AdapterException
     */
    public static ProcessInstance getRootProcessInstance(String accountId, String processInstanceId) throws AdapterException {
        JSONObject response = new JSONObject();
        try {
            response = restTemplate.getForObject(URI + "/history/historic-process-instances/" + processInstanceId, JSONObject.class);
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
        ProcessInstance processInstance = jsonObject2ProcessInstance(response);
        if (processInstance.getParentProcessInstID() == null) {
            return processInstance;
        }
        return getRootProcessInstance(null, processInstanceId);
    }

    /**
     * 51.	根据业务主键jobID获取当前待办参数
     * 根据业务主键查询这个工单下的当前处理人及相关参数
     * @param accountId
     * @param jobId
     * @return
     * @throws AdapterException
     */

    public static String findDoingActivitysByJobID(String accountId, String jobId) throws AdapterException {

        JSONObject request = new JSONObject(), response;
        //根据业务主键jobID查询历史流程实例集合
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(new JSONObject()
                    .element("name", "BIZ.jobID")
                    .element("value", jobId)
                    .element("operation", "equals")
                    .element("type", "string"));
            request.put("variables", jsonArray.toString());
        try {
            //查询历史流程实例集合
            response = restTemplate.postForObject(URI + "/query/historic-process-instances", request, JSONObject.class);
            JSONArray jsonArrayPro = response.getJSONArray("data");
            jsonArray = new JSONArray();
            //遍历历史流程实例集合
            for (int i = 0; i < jsonArrayPro.size(); i++) {
                JSONObject object = jsonArrayPro.getJSONObject(i);
                //通过流程实例ID查询任务实例集合
                response = restTemplate.getForObject(URI + "/runtime/tasks?processInstanceId=" + object.getString("id"), JSONObject.class);
                List<TaskInstance> taskInstanceList = jsonArray2TaskInstance(response.getJSONArray("data"));
                //遍历任务实例集合
                for (TaskInstance taskInstance : taskInstanceList) {
                    //根据任务实例ID查询当前处理人
                    JSONArray array = restTemplate.getForObject(URI + "/runtime/tasks/{taskId}/identitylinks", JSONArray.class, taskInstance.getTaskInstID());
                    List<String> participants = new ArrayList<String>();
                    //这个方法用来将所有user属性的值写入一个集合
                    participants.addAll(jsonArrayUser2List(array));
                    jsonArray.add(new JSONObject().element("proinstid", object.getString("id"))//流程实例ID
                            .element("parproinstid", object.getString("superProcessInstanceId"))//父流程实例ID
                            //将所有处理人通过逗号分隔写入一个字符串
                            .element("participants", StringUtils.join(participants, ","))//处理人集合(包括owner,assignee,candidateUser)
                            .element("actinstname", taskInstance.getActivityInstName())//活动节点名字
                            .element("creattime", taskInstance.getCreateDate().getTime())//创建时间
                            .element("actinstid", taskInstance.getTaskInstID()));//任务实例ID
                }
            }
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new AdapterException(e.getMessage(), e);
        }
        return jsonArray.toString();
    }

    /**
     * 52.根据业务主键获取活动
     * @param accountId
     * @param jobId
     * @return
     * @throws AdapterException
     */
    public static String findHisDoingActivitysByJobID(String accountId, String jobId) throws AdapterException {
        //根据业务主键查询这个工单下的当前处理人
        //根据jobId查询出processInstanceId，然后查询活动任务，进而查询待办人
        JSONObject request = new JSONObject(), response;
        JSONArray jsonArray = new JSONArray();
        try {
            response = restTemplate.getForObject(URI + "/history/historic-task-instances?processInstanceId="+jobId, JSONObject.class);
            JSONArray jsonArrayPro = response.getJSONArray("data");
            jsonArray = new JSONArray();
            for (int i = 0; i < jsonArrayPro.size(); i++) {
                JSONObject object = jsonArrayPro.getJSONObject(i);
                if (!"completed".equals(object.getString("deleteReason"))) {

                } else {
                    TaskInstance taskInstance = jsonObject2TaskInstance(object);
                    jsonArray.add(new JSONObject().element("proinstid", object.getString("id"))
                            .element("parproinstid", "null")
                            .element("participants", object.getString("assignee"))
                            .element("actinstname", taskInstance.getActivityInstName())
                            .element("creattime", taskInstance.getCreateDate().getTime())
                            .element("actinstid", taskInstance.getTaskInstID()));
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return jsonArray.toString();
    }

    /**
     * 19.根据流程实例ID获取流程对象
     * @param accountId
     * @param processInstId
     * @return
     * @throws AdapterException
     */
    public static ProcessInstance getProcessInstance(String accountId, String processInstId) throws AdapterException {
        JSONObject response = new JSONObject();
        try {
//            response = restTemplate.getForObject(URI + "/runtime/process-instances/" + processInstId, JSONObject.class);
            response = restTemplate.getForObject(URI + "/history/historic-process-instances/" + processInstId, JSONObject.class);
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
        return jsonObject2ProcessInstance(response);
    }


    /**
     * 21.获取流程实例的子流程
     * @param accountId
     * @param processInstId
     * @return
     * @throws AdapterException
     */
    public static List<ProcessInstance> getSubProcessInstance(String accountId,
                                                              String processInstId) throws AdapterException {

        List<ProcessInstance> processInstanceList = null;

        JSONObject request = new JSONObject(), response;
        request.put("superProcessInstanceId", processInstId);
        try {
            response = restTemplate.postForObject(URI + "/query/historic-process-instances", request, JSONObject.class);
            JSONArray jsonArrayPro = response.getJSONArray("data");
            processInstanceList = jsonArray2ProcessInstance(jsonArrayPro);
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new AdapterException(e.getMessage(), e);
        }

        return processInstanceList;
    }

    /**
     * 22.根据任务实例ID获取任务实例对象
     * @param accountId
     * @param taskInstId
     * @return
     * @throws AdapterException
     */
    public static TaskInstance getTaskInstanceObject(String accountId, String taskInstId) throws AdapterException {
        JSONObject response = new JSONObject();
        try {
            response = restTemplate.getForObject(URI + "/runtime/tasks/" + taskInstId, JSONObject.class);
            return jsonObject2TaskInstance(response);
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new AdapterException("other error", e);
        }
    }

    /**
     * 12.获取流程实例流转过的活动
     * 若流程未结束,则数据集合的最后一个元素是当前待办
     * @param accountId
     * @param processInstID
     * @return
     * @throws AdapterException
     */
    public static List<ActivityInstance> getActivityInstances(String accountId, String processInstID) throws AdapterException {
        JSONObject request = new JSONObject(), response;
        try {
            request.put("processInstanceId", processInstID);
            response = restTemplate.postForObject(URI + "/query/historic-activity-instances", request, JSONObject.class);
            return jsonObject2ActivityInstance(response.getJSONArray("data"));
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
    }

    /**
     * 23.查询流程模板
     *
     * @param accountId
     * @return
     * @throws AdapterException
     */
    public static JSONObject getProcessModeLists(String accountId) throws AdapterException {

        try {
            ResponseEntity<JSONObject> forEntity = restTemplate.getForEntity(URI + "/repository/process-definitions?category=http://www.activiti.org/processdef", JSONObject.class);
            JSONObject body = forEntity.getBody();
            return body;
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
    }


    public static void suspendProcessInstance(long processInstID, String accountId) throws AdapterException {
        JSONObject request = new JSONObject();
        try {
            request.put("action", "suspend");
            restTemplate.put(URI + "/runtime/process-instances/" + processInstID, request);
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
    }

    public static void resumeProcessInstance(long processInstID, String accountId) throws AdapterException {
        JSONObject request = new JSONObject();
        try {
            request.put("action", "activate");
            restTemplate.put(URI + "/runtime/process-instances/" + processInstID, request);
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
    }

    /**
     * 业务描述：撤回任务，在对方没有提交之前进行撤回
     *
     * @param currentActivityInstId 当前活动实例ID
     * @param targetActivityInstId  需要回退/取回的目标活动实例ID
     */
    public static void backActivity(String accountId, String currentActivityInstId, String targetActivityInstId) throws AdapterException {
        JSONObject request = new JSONObject(), response;
        //查询出taskId
        try {
            request.put("action", "rollback");
            response = restTemplate.getForObject(URI + "/history/historic-task-instances?taskId=" + targetActivityInstId, JSONObject.class);
            JSONArray jsonArray = response.getJSONArray("data");
            String activityId = jsonArray.getJSONObject(0).getString("taskDefinitionKey");
            request.put("activityId", activityId);
//            response = restTemplate.getForObject(URI + "/history/historic-activity-instances?activityInstanceId=" + currentActivityInstId, JSONObject.class);
//            jsonArray = response.getJSONArray("data");
//            String taskId = jsonArray.getJSONObject(0).getString("taskId");
            restTemplate.postForObject(URI + "/custom/runtime/tasks/" + currentActivityInstId, request, JSONArray.class);
        } catch (HttpClientErrorException e) {
            throw new AdapterException(e.getResponseBodyAsString(), e);
        }
    }

    /**
     * json转任务实例对象
     * @param object
     * @return
     * @throws Exception
     */
    public static TaskInstance jsonObject2TaskInstance(JSONObject object) throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskInstID(object.getString("id"));
//        if(!"".equals(object.getString("parentTaskId"))&&object.getString("parentTaskId")!=null)
//            taskInstance.setParentTaskInstID(object.getString("parentTaskId"));
        taskInstance.setActivityDefID(object.getString("taskDefinitionKey"));
        taskInstance.setActivityInstName(object.getString("name"));
        taskInstance.setProcessInstID(object.getString("processInstanceId"));
        taskInstance.setProcessModelId(object.getString("processDefinitionId"));
//        taskInstance.setActivityInstID(object.getString("id"));
        taskInstance.setProcessModelName(object.getString("processDefinitionId").split(":")[0]);
//        taskInstance.setStrColumn1(object.getString("assignee"));
        JSONArray array = object.getJSONArray("variables");

        try {
            String createTime = "";
            if (object.get("createTime") != null) { //待办到达时间
                createTime = object.getString("createTime");
                taskInstance.setCreateDate(formatter.parse(createTime));
            }
            if (object.get("startTime") != null) {   //已办到达时间
                taskInstance.setCreateDate(formatter.parse(object.getString("startTime")));
            }
            if (object.get("endTime") != null) {   //已办操作时间
                taskInstance.setCompletionDate(formatter.parse(object.getString("endTime")));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < array.size(); i++) {
            JSONObject o = array.getJSONObject(i);
            if (o.getString("name").equals("sn")) taskInstance.setJobCode(o.getString("value"));
            if (o.getString("name").equals(BIZ+"jobTitle")) taskInstance.setJobTitle(o.getString("value"));
            if (o.getString("name").equals(BIZ + "jobID")) taskInstance.setJobID(o.getString("value"));
            if (o.getString("name").equals(BIZ + "jobCode")) taskInstance.setJobCode(o.getString("value"));
            if (o.getString("name").equals(PROC + "datColumn1")) {
                try {
                    taskInstance.setDatColumn1(TypeUtils.castToDate(o.getString("value")));
                } catch (Exception e) {
                    logger.info(taskInstance.getProcessModelName() + "未设置完成时间datColumn1");
                }
            }
            if (o.getString("name").equals("ROOT_PROCESSINSTID"))
                taskInstance.setRootProcessInstId(o.getString("value"));
            if (o.getString("name").equals("shouldNum"))
                taskInstance.setStrColumn6(o.getString("value"));   //应反馈数量
            if (o.getString("name").equals("feeds"))                //实际反馈数
                taskInstance.setStrColumn7(o.getString("value"));
        }
        return taskInstance;
    }

    /**
     * jsonarray转任务实例对象集合
     * @param jsonArray
     * @return
     * @throws Exception
     */
    public static List<TaskInstance> jsonArray2TaskInstance(JSONArray jsonArray) throws Exception {
        List<TaskInstance> taskInstanceList = new ArrayList<TaskInstance>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            taskInstanceList.add(jsonObject2TaskInstance(object));
        }
        return taskInstanceList;
    }

    /**
     * json转流程实例对象
     * @param object
     * @return
     */
    public static ProcessInstance jsonObject2ProcessInstance(JSONObject object) {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setProcessInstID(object.getString("id"));
        if (object.containsKey("completed"))
            processInstance.setProcessInstStatus(object.getString("completed"));
        if (object.containsKey("superProcessInstanceId"))
            processInstance.setParentProcessInstID(object.getString("superProcessInstanceId"));
        processInstance.setProcessModelID(object.getString("processDefinitionId"));
        processInstance.setProcessModelName(object.get("processDefinitionId").toString().split(":")[0]);
        return processInstance;
    }

    /**
     * jsonarray转流程实例集合
     * @param jsonArray
     * @return
     */
    public static List<ProcessInstance> jsonArray2ProcessInstance(JSONArray jsonArray) {
        List<ProcessInstance> processInstanceList = new ArrayList<ProcessInstance>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = (JSONObject) jsonArray.get(i);
            ProcessInstance processInstance = new ProcessInstance();
            processInstance.setProcessInstID(object.getString("id"));
            processInstance.setParentProcessInstID(object.getString("superProcessInstanceId"));
            if (object.containsKey("completed")) processInstance.setProcessInstStatus(object.getString("completed"));
            processInstance.setProcessModelID(object.getString("processDefinitionId"));
            processInstance.setProcessModelName(object.get("processDefinitionId").toString().split(":")[0]);
            processInstance.setStartAccountID(object.getString("startUserId"));
            processInstanceList.add(processInstance);
        }
        return processInstanceList;
    }

    /**
     * jsonArray转活动实例集合
     * @param jsonArray
     * @return
     */
    public static List<ActivityInstance> jsonObject2ActivityInstance(JSONArray jsonArray) {
        List<ActivityInstance> activityInstanceList = new ArrayList<ActivityInstance>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            ActivityInstance activityInstance = new ActivityInstance();
            activityInstance.setProcessInstID(object.getString("processInstanceId"));
            activityInstance.setActivityInstID(object.getString("id"));
            activityInstance.setActivityDefID(object.getString("activityId"));
            activityInstance.setActivityInstName(object.getString("activityName"));
            activityInstance.setActivityType(object.getString("activityType"));
            activityInstance.setCurrentState("");
            activityInstance.setSubProcessID(new ArrayList<String>());
            try {
                activityInstance.setCreateTime(formatter.parse(object.getString("startTime")));
                if (!object.getString("endTime").equals("null"))
                    activityInstance.setEndTime(formatter.parse(object.getString("endTime")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            activityInstanceList.add(activityInstance);
        }
        return activityInstanceList;
    }

    /**
     * jsonArray转任务实例集合
     * @param jsonArray
     * @return
     */
    public static List<TaskInstance> jsonArray2ActivityInstance(JSONArray jsonArray) {
        List<TaskInstance> taskInstanceList = new ArrayList<TaskInstance>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            TaskInstance taskInstance = new TaskInstance();
            taskInstance.setProcessInstID(object.getString("processInstanceId"));
            taskInstance.setTaskInstID(object.getString("taskId"));
//            taskInstance.setActivityInstID(object.getString("id"));
            taskInstance.setActivityDefID(object.getString("activityId"));
            taskInstance.setActivityInstName(object.getString("activityName"));
            taskInstance.setProcessModelName(object.get("processDefinitionId").toString().split(":")[0]);
            taskInstance.setCurrentState("");
            try {
                taskInstance.setCreateDate(formatter.parse(object.getString("startTime")));
                if (!object.getString("endTime").equals("null")) {
                    taskInstance.setEndDate(formatter.parse(object.getString("endTime")));
                    taskInstance.setCompletionDate(formatter.parse(object.getString("endTime")));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            taskInstanceList.add(taskInstance);
        }
        return taskInstanceList;
    }

    /**
     * jsonArray转user集合
     * @param jsonArray
     * @return
     */
    public static List<String> jsonArrayUser2List(JSONArray jsonArray) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            list.add(object.getString("user"));
        }
        return list;
    }


}