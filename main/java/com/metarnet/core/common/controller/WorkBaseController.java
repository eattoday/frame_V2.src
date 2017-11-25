package com.metarnet.core.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.EnumConfigAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter4Activiti;
import com.metarnet.core.common.client.WFServiceClient;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.exception.UIException;
import com.metarnet.core.common.model.*;
import com.metarnet.core.common.service.ICommEntityService;
import com.metarnet.core.common.service.IWorkflowBaseService;
import com.metarnet.core.common.service.IWorkflowProcessor;
import com.metarnet.core.common.service.impl.GeneOperateService;
import com.metarnet.core.common.utils.*;
import com.metarnet.core.common.workflow.*;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class WorkBaseController extends BaseController {
    static org.apache.logging.log4j.Logger logger = LogManager.getLogger();

    @Resource
    private ICommEntityService iCommEntityService;
    @Resource
    private IWorkflowBaseService workflowBaseService;

    @Resource
    private IBaseDAO baseDAO;

    @Resource
    private NotifyInter remoteOAForXJ;

    private final String CREATIONTIME = "creationTime";

    private TaskFilter initCondition(TaskFilter taskFilter, Pager pager) {

        PageCondition pageCondition = taskFilter.getPageCondition();
        if (pageCondition == null) {
            pageCondition = new PageCondition();
            taskFilter.setPageCondition(pageCondition);
        }

        pageCondition.setBegin(pager.getStartRecord());
        pageCondition.setLength(pager.getPageSize());

        return taskFilter;
    }

    /**
     * 查询当前人待办
     *
     * @param response
     * @param request
     * @param taskFilter 待办查询条件实体
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=queryTodo")
    @ResponseBody
    public void queryTodo(TaskFilter taskFilter, HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            if (pager == null) {
                pager = new Pager();
            }

            taskFilter = initCondition(taskFilter, pager);
            pager = queryTodoList(taskFilter, response, request, getAccountId(request));

            //设置查询成功
            pager.setIsSuccess(true);
//            pager.setExhibitDatas(list);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "todo");
        } catch (Exception e) {
            throw new UIException("todo", e);
        }
    }

    public Pager queryTodoList(TaskFilter taskFilter, HttpServletResponse response, HttpServletRequest request, String userName) throws UIException {
        Pager pager = new Pager();
        List<TaskInstance> list2 = new ArrayList<TaskInstance>();
        try {
            if ("true".equals(Constants.IS_GROUPBY)) {
                pager = queryTodo2(taskFilter, response, request, userName);
                list2 = pager.getExhibitDatas();
//                return pager.getExhibitDatas();
            } else {
//                taskFilter = setPageCodition(taskFilter, request);
                List<TaskInstance> list = null;
                if (userName != null && !"".equals(userName)) {
                    list = workflowBaseService.getMyWaitingTasks(taskFilter, userName);
                } else {
                    list = workflowBaseService.getMyWaitingTasks(taskFilter, getAccountId(request));
                }

                if (taskFilter.getDatColumn1StartTime() != null && taskFilter.getDatColumn1EndTime() != null) {
                    for (TaskInstance task : list) {
                        if ((task.getDatColumn1() != null) && (task.getDatColumn1().getTime() <= taskFilter.getDatColumn1EndTime().getTime()) && (task.getDatColumn1().getTime() >= taskFilter.getDatColumn1StartTime().getTime())) {
                            list2.add(task);
                        }
                    }
                } else {
                    list2 = list;
                }
                pager.setExhibitDatas(list2);
            }
        } catch (Exception e) {
            throw new UIException("todo", e);
        }

        for (TaskInstance taskInstance : list2) {
            Calendar timeout = Calendar.getInstance();
            Calendar comingTimeout = Calendar.getInstance();
            comingTimeout.add(Calendar.HOUR_OF_DAY, 2);

            Date require = taskInstance.getDatColumn1();
            if (require != null) {
                if (require.before(timeout.getTime())) {
                    //超时
                    taskInstance.setNumColumn1(2);
                } else if (require.after(timeout.getTime()) && require.before(comingTimeout.getTime())) {
                    //即将超时
                    taskInstance.setNumColumn1(1);
                }
            }
        }

        return pager;
    }

    //查询合并后代办
//    @RequestMapping(value = "/workBaseController.do", params = "method=queryTodo2")
//    @ResponseBody
    private Pager queryTodo2(TaskFilter taskFilter, HttpServletResponse response, HttpServletRequest request, String userName) throws Exception {

        return WorkflowAdapter4Activiti.getMyWaitingTasksDistinctJobId(taskFilter, userName);
//        return WorkflowAdapter4Activiti.getMyCompletedTasksDistinctJobId(taskFilter,userName);
//        return WorkflowAdapter4Activiti.getMyCompletedTasksDistinctJobId(taskFilter,userName);
    }

    /**
     * 查询合并后代办
     *
     * @param taskFilter
     * @param response
     * @param request
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=queryCombinedTodo")
    public void queryCombinedTodo(TaskFilter taskFilter, HttpServletResponse response, HttpServletRequest request) throws UIException {

    }

    /**
     * 查询当前人已办
     *
     * @param response
     * @param request
     * @param taskFilter 已办查询条件实体
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=getReady")
    @ResponseBody
    public void getReady(TaskFilter taskFilter, HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            taskFilter = setPageCodition(taskFilter, request);
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));

//            Map map = new HashMap();
//            map.put("datColumn2" , "2017-02-25 11:22:33");
//            map.put("strColumn7" , "2017-02-25 11:22:33");
//            WFServiceClient.getInstance().updateBusiInfoByRoot("234" , map);

//            pager.getFastQueryParameters().put("in_processmodelname" , Constants.PROCESS_MODELS);

//            pager = WFServiceClient.getInstance().getMyCompletedTasks(getAccountId(request) , pager);

//            taskFilter.setProcessModelName(Constants.PROCESS_MODELS);
            List temp = new ArrayList();
//            非合并已办
//            temp = WorkflowAdapter.getMyCompletedTasks(taskFilter, getAccountId(request));
            if ("true".equals(Constants.IS_GROUPBY)) {
                pager = WorkflowAdapter.getMyCompletedTasksDistinctJobId(taskFilter, getAccountId(request));
            } else {
                temp = WorkflowAdapter.getMyCompletedTasks(taskFilter, getAccountId(request));
                pager.setExhibitDatas(temp);
            }
//            将JobCode,JobTitle的空值设置为NULL
//            if (StringUtils.isEmpty(taskFilter.getJobCode())) {
//                taskFilter.setJobCode(null);
//            }
//            if (StringUtils.isEmpty(taskFilter.getJobTitle())) {
//                taskFilter.setJobTitle(null);
//            }
//            Date start_get_ready = new Date();
//            logger.info("Start to getMyCompletedTasksDistinctProinstanceId...");
//            List<TaskInstance> list = WorkflowAdapter.getMyCompletedTasksDistinctProinstanceId(taskFilter, getAccountId(request));
//            logger.info("Complete getMyCompletedTasksDistinctProinstanceId.../COST = " + (new Date().getTime() - start_get_ready.getTime()));
//            for (TaskInstance taskInstance : list) {
//                if (StringUtils.isEmpty(taskInstance.getActivityInstID())) {
//                    start_get_ready = new Date();
//                    logger.info("Start to getTaskInstanceObject...");
//                    TaskInstance instance = WorkflowAdapter.getTaskInstanceObject(getAccountId(request), taskInstance.getTaskInstID());
//                    logger.info("Complete getTaskInstanceObject.../COST = " + (new Date().getTime() - start_get_ready.getTime()));
//                    String processInstId = taskInstance.getProcessInstID();
//                    if (null == processInstId) {
//                        processInstId = taskInstance.getRootProcessInstId();
//                    }
//                    ProcessInstance processInstance = WorkflowAdapter.getProcessInstance(getAccountId(request), processInstId);
//                    if (null != processInstance && processInstance.getProcessInstStatus().equals("5")) {
//                        9为已撤销
//                        instance.setCurrentState("9");
//                    }
//                    temp.add(instance);
//                }
//                }
//            }
//            pager.setRecordCount(taskFilter.getPageCondition().getCount());
//            int pageSize = pager.getPageSize();
//            int recordCount = pager.getRecordCount();
//            int pageCount = pager.getPageCount();
//            pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
//            pager.setPageCount(pageCount);
//            pager.setExhibitDatas(temp);
//			设置查询成功
            pager.setIsSuccess(true);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "todo");
        } catch (Exception e) {
            throw new UIException("todo", e);
        }
    }

    /**
     * 获取分页信息
     */
    private TaskFilter setPageCodition(TaskFilter taskFilter, HttpServletRequest request) {
        String dtGridPager = request.getParameter("dtGridPager");
        int start;// 开始行数
        int length;// 每页大小
        String jobCode = null;
        String jobTitle = null;
        String startCreateDate = null;
        String endCreateDate = null;
        try {
            JSONObject dtGridPagerJson = JSON.parseObject(dtGridPager);
            start = Integer.valueOf((String) dtGridPagerJson.get("startRecord"));
            length = Integer.valueOf((String) dtGridPagerJson.get("pageSize"));

            JSONObject fastQueryParameters = (JSONObject) dtGridPagerJson.get("fastQueryParameters");
            try {
                jobCode = fastQueryParameters.getString("lk_jobCode");
                jobTitle = fastQueryParameters.getString("lk_jobTitle");
                //到单时间查询
                startCreateDate = fastQueryParameters.getString("ge_completionDate");
                endCreateDate = fastQueryParameters.getString("le_completionDate");
            } catch (Exception e) {

            }
            if (jobCode != null && !"".equals(jobCode)) {
                taskFilter.setJobCode(jobCode);
            }
            if (jobTitle != null && !"".equals(jobTitle)) {
                taskFilter.setJobTitle(jobTitle);
            }

        } catch (Exception e) {
            start = 0;
            length = 10;
        }

        PageCondition pageCondition = taskFilter.getPageCondition();
        if (pageCondition == null) {
            pageCondition = new PageCondition();
            pageCondition.setBegin(start);
            pageCondition.setLength(length);
            taskFilter.setPageCondition(pageCondition);
        }

        return taskFilter;
    }

    /**
     * 查询当前人已阅
     *
     * @param response
     * @param request
     * @param notificationFilter 通知查询条件实体
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=getReaded")
    @ResponseBody
    public void getReaded(NotificationFilter notificationFilter, HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            notificationFilter = setPageCodition(notificationFilter, request);
            notificationFilter.setProcessModelName(Constants.PROCESS_MODELS);
            List<com.metarnet.core.common.workflow.NotificationInstance> list = null;
            list = WorkflowAdapter.getMyReadNotifications(notificationFilter, getAccountId(request));
            List<NotificationInstance> list2 = new ArrayList<NotificationInstance>();
            for (com.metarnet.core.common.workflow.NotificationInstance notificationInstance : list) {
                TaskInstance taskInstance = WorkflowAdapter.getTaskInstanceObject(getAccountId(request), notificationInstance.getTaskinstid());
                notificationInstance.setSenderID(AAAAAdapter.getInstence().findUserByUserName(notificationInstance.getSenderID().replace("P{", "").replace("}", "")).getTrueName());
                notificationInstance.setJobCode(taskInstance.getJobCode());
                notificationInstance.setJobTitle(taskInstance.getJobTitle());
                notificationInstance.setStrColumn1(taskInstance.getStrColumn1());
                notificationInstance.setStrColumn2(taskInstance.getStrColumn2());
                notificationInstance.setStrColumn3(taskInstance.getStrColumn3());
                notificationInstance.setStrColumn4(taskInstance.getStrColumn4());
                notificationInstance.setStrColumn5(taskInstance.getStrColumn5());
                notificationInstance.setStrColumn6(taskInstance.getStrColumn6());
                notificationInstance.setStrColumn7(taskInstance.getStrColumn7());
//                if (notificationInstance.getJobCode().contains((String) request.getParameter("jobCode"))) {
//                    list2.add(notificationInstance);
//                }
            }

//            TablePageModel model = new TablePageModel();
//            JsonConfig jsonConfig = new JsonConfig();
//            jsonConfig.registerJsonValueProcessor(Date.class, new JsonTimestampToStringUtil());
//            model.setTotalCount(notificationFilter.getPageCondition().getCount());
//            model.setGlist(list);
//            endHandle(request, response, JSONObject.fromObject(model, jsonConfig), "readed");
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager.setRecordCount(notificationFilter.getPageCondition().getCount());
            int pageSize = pager.getPageSize();
            int recordCount = pager.getRecordCount();
            int pageCount = pager.getPageCount();
            pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
            pager.setPageCount(pageCount);
            pager.setExhibitDatas(list);
//			设置查询成功
            pager.setIsSuccess(true);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "readed");
        } catch (Exception e) {
            throw new UIException("readed", e);
        }
    }

    /**
     * 查询当前人待阅
     *
     * @param response
     * @param request
     * @param notificationFilter 通知查询条件实体
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=getUnread")
    @ResponseBody
    public void getUnread(NotificationFilter notificationFilter, HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            notificationFilter = setPageCodition(notificationFilter, request);
            notificationFilter.setProcessModelName(Constants.PROCESS_MODELS);
            List<com.metarnet.core.common.workflow.NotificationInstance> list = null;
            list = WorkflowAdapter.getMyUnreadNotifications(notificationFilter, getAccountId(request));
            List<TaskInstance> list2 = new ArrayList<TaskInstance>();
            for (com.metarnet.core.common.workflow.NotificationInstance notificationInstance : list) {
                try {
                    TaskInstance taskInstance = WorkflowAdapter.getTaskInstanceObject(getAccountId(request), notificationInstance.getTaskinstid());
                    notificationInstance.setSenderID(AAAAAdapter.getInstence().findUserByUserName(notificationInstance.getSenderID().replace("P{", "").replace("}", "")).getTrueName());
                    notificationInstance.setJobCode(taskInstance.getJobCode());
                    notificationInstance.setJobTitle(taskInstance.getJobTitle());
                    notificationInstance.setStrColumn1(taskInstance.getStrColumn1());
                    notificationInstance.setStrColumn2(taskInstance.getStrColumn2());
                    notificationInstance.setStrColumn3(taskInstance.getStrColumn3());
                    notificationInstance.setStrColumn4(taskInstance.getStrColumn4());
                    notificationInstance.setStrColumn5(taskInstance.getStrColumn5());
                    notificationInstance.setStrColumn6(taskInstance.getStrColumn6());
                    notificationInstance.setStrColumn7(taskInstance.getStrColumn7());
//                    notificationInstance.setSenderID(AAAAAdapter.getInstence().findAccountByPortalAccountID(notificationInstance.getSenderID().replace("P{", "").replace("}", "")).getDisplay());
//                    if (notificationInstance.getJobCode().contains((String) request.getParameter("jobCode"))) {
//                        list2.add(notificationInstance);
//                    }
                    list2.add(taskInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            TablePageModel model = new TablePageModel();
//            JsonConfig jsonConfig = new JsonConfig();
//            jsonConfig.registerJsonValueProcessor(Date.class, new JsonTimestampToStringUtil());
//            model.setTotalCount(notificationFilter.getPageCondition().getCount());
//            model.setGlist(list2);
//            endHandle(request, response, JSONObject.fromObject(model, jsonConfig), "unread");
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager.setRecordCount(notificationFilter.getPageCondition().getCount());
            pager.setExhibitDatas(list);
            int pageSize = pager.getPageSize();
            int recordCount = pager.getRecordCount();
            int pageCount = pager.getPageCount();
            pageCount = recordCount / pageSize + (recordCount % pageSize > 0 ? 1 : 0);
            pager.setPageCount(pageCount);
//            pager.setExhibitDatas(temp);
//			设置查询成功
            pager.setIsSuccess(true);
            SerializeConfig ser = new SerializeConfig();
            ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "unread");

        } catch (Exception e) {
            throw new UIException("unread", e);
        }
    }


    /**
     * 修改待阅为已阅读
     *
     * @param response
     * @param request
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=setNotificationToRead")
    @ResponseBody
    public void setNotificationToRead(String notificationInstId, String taskInstID, HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            Map map = new HashMap();
            String accountId = getAccountId(request);
            TaskInstance taskInstance = WorkflowAdapter.getTaskInstanceObject(accountId, taskInstID);
            if (StringUtils.isNoneEmpty(notificationInstId) && StringUtils.isNoneEmpty(taskInstID)) {
                WorkflowAdapter.setNotificationToRead(notificationInstId, accountId);
            }

            map.put("success", true);
            map.put("taskInstance", taskInstance);
            response.setContentType("text/xml; charset=UTF-8");
            endHandle(request, response, JSON.toJSONString(map), "readed");
        } catch (AdapterException e) {
            throw new UIException("readed", e);
        }
    }

    /**
     * 已阅设置taskinstance
     *
     * @param response
     * @param request
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=setNotificationFromRead")
    @ResponseBody
    public void setNotificationFromRead(String taskInstID, HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            if (StringUtils.isEmpty(taskInstID)) {
                return;
            }
            String accountId = getAccountId(request);
            TaskInstance taskInstance = WorkflowAdapter.getTaskInstanceObject(accountId, taskInstID);
            Map map = new HashMap();
            map.put("success", true);
            map.put("taskInstance", taskInstance);
            response.setContentType("text/xml; charset=UTF-8");
            endHandle(request, response, JSON.toJSONString(map), "readed");
        } catch (AdapterException e) {
            throw new UIException("readed", e);
        }
    }

    /**
     * 获取分页信息
     */
    private NotificationFilter setPageCodition(NotificationFilter notificationFilter, HttpServletRequest request) {
        int start;// 开始行数
        int length;// 每页大小
        JSONObject dtGridPagerJson = null;
        String jobTitle = null;
        String jobCode = null;
        String startCreateDate = null;
        String endCreateDate = null;
        try {
            String dtGridPager = request.getParameter("dtGridPager");
            dtGridPagerJson = JSON.parseObject(dtGridPager);
            start = (Integer) dtGridPagerJson.get("startRecord");
            length = (Integer) dtGridPagerJson.get("pageSize");
            JSONObject fastQueryParameters = (JSONObject) dtGridPagerJson.get("fastQueryParameters");
            try {
                jobTitle = fastQueryParameters.getString("lk_jobTitle");
                jobCode = fastQueryParameters.getString("lk_jobCode");
                //到单时间查询
                startCreateDate = fastQueryParameters.getString("ge_deliveryDate");
                endCreateDate = fastQueryParameters.getString("le_deliveryDate");
            } catch (Exception e) {

            }
            if (jobTitle != null && !"".equals(jobTitle)) {
                notificationFilter.setJobTitle(jobTitle);
            }
            if (jobCode != null && !"".equals(jobCode)) {
                notificationFilter.setJobID(jobCode);
            }
            if ((!"".equals(startCreateDate) && startCreateDate != null) || (!"".equals(endCreateDate) && endCreateDate != null)) {
                String patter = "yyyy-MM-dd HH:mm:ss";
                SimpleDateFormat sdf = new SimpleDateFormat(patter);
                List<Object> ls = new ArrayList<Object>();
                if (!"".equals(endCreateDate) && endCreateDate != null) {
                    Date bdEndDate1 = (Date) sdf.parse(endCreateDate);
                    notificationFilter.setEndDeliveryDate(bdEndDate1);
                }
                if (!"".equals(startCreateDate) && startCreateDate != null) {
                    Date bdStartDate1 = (Date) sdf.parse(startCreateDate);
                    notificationFilter.setBeginDeliveryDate(bdStartDate1);
                }
            }
        } catch (Exception e) {
            start = 0;
            length = 10;
        }
        notificationFilter.setAppID(null);
//        notificationFilter.setSender(null);
//        notificationFilter.setSenderID(null);
//        notificationFilter.setSenderType(null);
//        PageCondition page = new PageCondition();
//        page.setBegin(start);
//        page.setLength(length);
//        page.setIsCount(true);
//        notificationFilter.setPageCondition(page);
        return notificationFilter;
    }
//    private JobFilter setPageCodition(List<LogicType> logicList , List<ExprType> tel, JobFilter jobFilter, HttpServletRequest request) {
//        String dtGridPager = request.getParameter("dtGridPager");
//        int start;// 开始行数
//        int length;// 每页大小
//        JSONObject dtGridPagerJson = null;
//        try {
//            dtGridPagerJson = JSON.parseObject(dtGridPager);
//            start = Integer.valueOf((String) dtGridPagerJson.get("startRecord"));
//            length = Integer.valueOf((String) dtGridPagerJson.get("pageSize"));
//        } catch (Exception e1){
//            start = 0;
//            length = 10;
//        }
//        try {
//            String fastQueryKeyWord = dtGridPagerJson.getString("fastQueryKeyWord");
//            //自定义过滤条件
//            //工单编号
//            if (!"".equals(fastQueryKeyWord) && fastQueryKeyWord != null) {
//
//                LogicType logicType1 = LogicType.FACTORY.create();
//                List<ExprType> exprList1 = new ArrayList<ExprType>();
//
//                ExprType ex_jobtitle = ExprType.FACTORY.create();
//                ex_jobtitle.set_opEnum(ExprType.OP.LIKE);
//                ex_jobtitle.set_value(fastQueryKeyWord);
//                ex_jobtitle.set_property(Global.JOB_TITLE.toLowerCase());
//                exprList1.add(ex_jobtitle);
//
//                ExprType ex_jobcode = ExprType.FACTORY.create();
//                ex_jobcode.set_opEnum(ExprType.OP.LIKE);
//                ex_jobcode.set_value(fastQueryKeyWord);
//                ex_jobcode.set_property(Global.JOB_CODE.toLowerCase());
//                exprList1.add(ex_jobcode);
//
//                ExprType ex_major = ExprType.FACTORY.create();
//                ex_major.set_opEnum(ExprType.OP.LIKE);
//                ex_major.set_value(fastQueryKeyWord);
//                ex_major.set_property(Global.BIZ_STRCOLUMN1.toLowerCase());
//                exprList1.add(ex_major);
//
//                ExprType ex_activity = ExprType.FACTORY.create();
//                ex_activity.set_opEnum(ExprType.OP.LIKE);
//                ex_activity.set_value(fastQueryKeyWord);
//                ex_activity.set_property("activityinstname");
//                exprList1.add(ex_activity);
//
//                logicType1.set_expr(exprList1);
//                logicList.add(logicType1);
//
////                LogicType logicType2 = LogicType.FACTORY.create();
////                List<ExprType> exprList2 = new ArrayList<ExprType>();
////                ExprType ex_jobcode = ExprType.FACTORY.create();
////                ex_jobcode.set_opEnum(ExprType.OP.LIKE);
////                ex_jobcode.set_value(fastQueryKeyWord);
////                ex_jobcode.set_property(Global.JOB_CODE.toLowerCase());
////                exprList2.add(ex_jobcode);
////                logicType2.set_expr(exprList2);
////                logicList.add(logicType2);
//            }
//        } catch (Exception e) {
//
//        }
//        try {
//            JSONObject fastQueryParameters = (JSONObject) dtGridPagerJson.get("highQueryParameters");
//            String jobCode = fastQueryParameters.getString("lk_jobCode");
//            String jobTitle = fastQueryParameters.getString("lk_jobTitle");
//            String major = fastQueryParameters.getString("lk_strColumn1");
//            //要求完成时间查询
//            String startDatColumn1 = fastQueryParameters.getString("ge_datColumn1");
//            String endDatColumn1 = fastQueryParameters.getString("le_datColumn1");
//            //到单时间查询
//            String startCreateDate = fastQueryParameters.getString("ge_createDate");
//            String endCreateDate = fastQueryParameters.getString("le_createDate");
//            //自定义过滤条件
//            //工单编号
//            if (!"".equals(jobCode) && jobCode != null) {
//                ExprType ls = ExprType.FACTORY.create();
//                ls.set_opEnum(ExprType.OP.LIKE);
//                ls.set_value(jobCode);
//                ls.set_property(Global.JOB_CODE.toLowerCase());
//                tel.add(ls);
//            }
//
//            //工单主题
//            if (!"".equals(jobTitle) && jobTitle != null) {
//                ExprType ls = ExprType.FACTORY.create();
//                ls.set_opEnum(ExprType.OP.LIKE);
//                ls.set_value(jobTitle);
//                ls.set_property(Global.JOB_TITLE.toLowerCase());
//                tel.add(ls);
//            }
//
//            //专业
//            if (!"".equals(major) && major != null) {
//                ExprType ls = ExprType.FACTORY.create();
//                ls.set_opEnum(ExprType.OP.LIKE);
//                ls.set_value(major);
//                ls.set_property(Global.BIZ_STRCOLUMN1.toLowerCase());
//                tel.add(ls);
//            } else {
//                major = fastQueryParameters.getString("eq_strColumn1");
//                if (!"".equals(major) && major != null) {
//                    ExprType ls = ExprType.FACTORY.create();
//                    ls.set_opEnum(ExprType.OP.EQ);
//                    ls.set_value(major);
//                    ls.set_property(Global.BIZ_STRCOLUMN1.toLowerCase());
//                    tel.add(ls);
//                }
//            }
//
//            String patter = "yyyy-MM-dd HH:mm:ss";
//            SimpleDateFormat sdf = new SimpleDateFormat(patter);
//            if ((!"".equals(startDatColumn1) && startDatColumn1 != null) || (!"".equals(endDatColumn1) && endDatColumn1 != null)) {
//                ExprType ls = ExprType.FACTORY.create();
//                ls.set_opEnum(ExprType.OP.BETWEEN);
//                ls.set_property(Global.BIZ_DATCOLUMN1.toLowerCase());
//                ls.set_pattern(patter);
//                if (!"".equals(endDatColumn1) && endDatColumn1 != null) {
//                    Date bdEndDate1 = (Date) sdf.parse(endDatColumn1);
//                    ls.set_max(sdf.format(bdEndDate1));
//                }
//                if (!"".equals(startDatColumn1) && startDatColumn1 != null) {
//                    Date bdStartDate1 = (Date) sdf.parse(startDatColumn1);
//                    ls.set_min(sdf.format(bdStartDate1));
//                }
//                tel.add(ls);
//            }
//            if ((!"".equals(startCreateDate) && startCreateDate != null) || (!"".equals(endCreateDate) && endCreateDate != null)) {
//                ExprType ls = ExprType.FACTORY.create();
//                ls.set_opEnum(ExprType.OP.BETWEEN);
//                ls.set_property("createtime");
//                ls.set_pattern(patter);
//                if (!"".equals(endCreateDate) && endCreateDate != null) {
//                    Date bdEndDate1 = (Date) sdf.parse(endCreateDate);
//                    ls.set_max(sdf.format(bdEndDate1));
//                }
//                if (!"".equals(startCreateDate) && startCreateDate != null) {
//                    Date bdStartDate1 = (Date) sdf.parse(startCreateDate);
//                    ls.set_min(sdf.format(bdStartDate1));
//                }
//                tel.add(ls);
//            }
//        } catch (Exception e) {
//
//        }
//
//        if (!"".equals(Constants.PROCESS_MODELS) && Constants.PROCESS_MODELS != null) {
//            ExprType ls9 = ExprType.FACTORY.create();
//            ls9.set_opEnum(ExprType.OP.IN);
//            ls9.set_value(Constants.PROCESS_MODELS);
//            ls9.set_property("processdefname");
//            tel.add(ls9);
//        }
//
//        PageCondition page = new PageCondition();
//        page.setBegin(start);
//        page.setLength(length);
//        page.setIsCount(true);
//        jobFilter.setPageCondition(page);
//        return jobFilter;
//    }

    /**
     * 初始化审核表单
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=initApprovalForm")
    @ResponseBody
    public void initApprovalForm(HttpServletResponse response, HttpServletRequest request) throws UIException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("signName", "admin");
        jsonObject.put(CREATIONTIME, new Date());
        endHandle(request, response, jsonObject, "approvalInit");
    }

    /**
     * 初始化转派、回执等页面信息
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=initGeneralForm")
    @ResponseBody
    public void initGeneralForm(HttpServletResponse response, HttpServletRequest request) throws UIException {
        JSONObject jsonObject = new JSONObject();
        // 添加处理的枚举类型，来自于后台数据库查询
        // jsonObject.put("processType",ProcessTypeEnum.getProcessType("MS_TURN_DIS").code());
        jsonObject.put("createdBy", getUserEntity(request).getUserId());
        endHandle(request, response, jsonObject, "initGeneralForm");
    }

    /**
     * 初始化调度详情页面(老版工单详情)
     *
     * @param request
     * @param response
     * @param processInstanceId
     * @return
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=getDisDetailDept")
    public ModelAndView doGetDispatchDetailInfo(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "processInstanceId") String processInstanceId) throws UIException {
        Integer shardingId = null;
        try {
            shardingId = Integer.parseInt(request.getParameter("shardingId"));
        } catch (NumberFormatException e) {
            shardingId = null;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            // LogAdapter.getInstence().writeOperLog("调度详情按部门查看",getUserEntity(request),"初始化","初始化",
            // "根据一级调度ID初始化调度详情按部门查看");
            jsonObject.put("processInstanceId", processInstanceId);
            jsonObject.put("shardingId", shardingId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UIException("getDispatchTree", e);
        }
        return new ModelAndView("frame/disDeptDetail", "initParameters", jsonObject);
    }

    /**
     * 获取相关数据
     *
     * @param request
     * @param response
     * @param processInstanceId
     * @param keys
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=getRelativeData")
    public void getRelativeData(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "processInstanceId") String processInstanceId, @RequestParam(value = "keys") String keys) throws UIException {
        Map m = new HashMap<String, String>();
        try {
            m = WorkflowAdapter.getRelativeData(processInstanceId, JSONArray.toList(JSONArray.fromObject(keys.split(","))), getUserEntity(request).getUserName());
        } catch (AdapterException e) {
            throw new UIException("getRelativeData", e);
        }
        endHandle(request, response, JSON.toJSONString(m), "getRelativeData");
    }


    /**
     * 根据活动环节ID获取横向扩展信息
     *
     * @param response
     * @param request
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=getTransverseInfo")
    @ResponseBody
    public void getTransverseInfo(String shard, String activityDefID, String processInstID, HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            List list = workflowBaseService.getTransverseInfo(activityDefID, processInstID, shard);
            JSONArray jsonArray = JSONArray.fromObject(list);

            endHandle(request, response, jsonArray.toString(), activityDefID);
        } catch (Exception e) {
            throw new UIException(activityDefID, e);
        }
    }

    /**
     * 平台待办入口
     *
     * @param taskID
     * @param response
     * @param request
     * @return
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=plaTodo")
    public void plaTodo(String taskID, String notificationInstID, HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            String taskStr = "";
            if (taskID != null && taskID.length() > 0) {
                TaskInstance task = WorkflowAdapter.getTaskInstanceObject(getAccountId(request), taskID);

                if (task == null) {
                    logger.error("平台待办/已办接入失败！taskID=" + taskID);
                }
                JSONObject json = (JSONObject) JSON.toJSON(task);
                json.remove("participants");
                json.remove("processMessage");
                json.remove("processConditionMessage");
                json.remove("subprocessMessageList");
                json.remove("fileds");
                taskStr = json.toString();
                // 待办、待领取
                if ("1".equals(task.getCurrentState()) || "4".equals(task.getCurrentState())) {
                    // request.getRequestDispatcher("frame/formFrame.jsp?type=waiting&method=build&taskInstance="
                    // +
                    // URLEncoder.encode(taskStr,"utf-8").forward(request,
                    // response);
                    response.sendRedirect("frame/formFrame.jsp?type=waiting&method=build&taskInstance=" + URLEncoder.encode(taskStr, "utf-8"));
                } else if ("2".equals(task.getCurrentState())) {// 已办
                    request.getRequestDispatcher("frame/formFrame.jsp?method=build&taskInstance=" + taskStr).forward(request, response);
                }

            } else if (notificationInstID != null && notificationInstID.length() > 0) {
                NotificationFilter notificationFilter = new NotificationFilter();
                notificationFilter.setNotificationInstID(notificationInstID);
                List<NotificationInstance> list = null;
//                WorkflowAdapter.getMyUnreadNotifications(notificationFilter, getAccountId(request));
                NotificationInstance notificationInstance = null;
                if (null != list && list.size() > 0) {
                    notificationInstance = list.get(0);
                } else {
                    logger.error("平台待阅/已阅接入失败！--没有NotificationInstance--notificationInstID=" + notificationInstID);
                    return;
                }
                TaskInstance task = WorkflowAdapter.getTaskInstanceObject(getAccountId(request), notificationInstance.getTaskinstid());
                if (task == null) {
                    logger.error("平台待阅/已阅接入失败！--没有TaskInstance--notificationInstID=" + notificationInstID);
                    return;
                }
                JSONObject json = (JSONObject) JSON.toJSON(task);
                json.remove("participants");
                json.remove("processMessage");
                json.remove("processConditionMessage");
                json.remove("subprocessMessageList");
                json.remove("fileds");
                taskStr = json.toString();
                response.sendRedirect("frame/formFrame.jsp?method=build&taskInstance=" + URLEncoder.encode(taskStr, "utf-8"));
                //将该待阅设置为已阅
                this.setNotificationToRead(notificationInstID, task.getTaskInstID(), response, request);
            }

        } catch (Exception e) {
            throw new UIException(taskID, e);
        }
    }

    /**
     * 查询用户信息
     *
     * @param userId
     * @param response
     * @param request
     * @return
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=getUserInfo")
    public void getUserInfo(String userId, HttpServletResponse response, HttpServletRequest request) throws UIException {
        JSONObject jsonObject = new JSONObject();
        try {
            if (userId != null) {
                jsonObject.put("userEntity", JSONArray.fromObject(AAAAAdapter.getInstence().findUserbyUserID(Integer.parseInt(userId))));
            }
        } catch (Exception e) {
            throw new UIException("用户查找失败", e);
        } finally {
            endHandle(request, response, jsonObject, "");
        }
    }

    /**
     * 控制撤销按钮是否显示
     *
     * @param taskInstance
     * @param response
     * @param request
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=isShowRecallFormLink")
    public void isShowRecallFormLink(TaskInstance taskInstance, HttpServletResponse response, HttpServletRequest request) throws UIException {
//        try {
        Boolean flag = false;
           /* ActivityInstance activityInstance = WorkflowAdapter.findActivityInstByActivityDefID(taskInstance
                    .getProcessInstID(), taskInstance.getActivityDefID(), getAccountId(request));
            ProcessInstance processInstance = WorkflowAdapter.getProcessInstance(getAccountId(request), taskInstance
                    .getProcessInstID());
            if (processInstance.getParentProcessInstID().equals("0") && activityInstance != null) {
                flag = true;
            }*/
        //流程发起人可以撤单
//            ProcessInstance processInstance = WorkflowAdapter.getProcessInstance(getAccountId(request), taskInstance.getProcessInstID());
//            if (processInstance.getParentProcessInstID().equals("0") && processInstance.getProcessInstStatus().equals(Constants.PIS_RUN) && processInstance.getStartAccount().equals(getAccountId(request))) {
//                flag = true;
//            }
//            JSONObject json = new JSONObject();
//            json.put("flag", flag);
//            endHandle(request, response, json, "");
//        } catch (AdapterException e) {
//            throw new UIException("判断撤销按钮是否显示失败", e);
//        }
    }

    /**
     * 获取流程信息
     *
     * @param response
     * @param request
     * @param processInstID
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=getProcessInfo")
    public void getProcessInfo(HttpServletResponse response, HttpServletRequest request, String processInstID) throws UIException {
        try {
            ProcessInstance processInstance = WorkflowAdapter.getProcessInstance(getAccountId(request), processInstID);
            JSONObject jsonObject = new JSONObject();
            if (processInstance == null) {
                jsonObject.put("processModelId", "null");
                jsonObject.put("processModelName", "null");
                jsonObject.put("rootProcessInstId", "null");
                Logger.getLogger(this.getClass().getName()).error(processInstID + ":流程实例在流程引擎中未找到，可能已被删除(脏数据)##################################################");
                endHandle(request, response, jsonObject, "获取流程信息");
            } else {
                jsonObject.put("processModelId", processInstance.getProcessModelID());
                jsonObject.put("processModelName", processInstance.getProcessModelName());
                jsonObject.put("rootProcessInstId", WorkflowAdapter.getRootProcessInstance(getAccountId(request), processInstID).getProcessInstID());
                if (Constants.IS_SHARDING) {
                    List list = new ArrayList<String>();
                    list.add("shard");
                    Map map = WorkflowAdapter.getRelativeData(processInstID, list, getAccountId(request));
                    if (null != map && map.size() > 0) {
                        jsonObject.put("shard", map.get("shard"));
                    }
                }
            }
            endHandle(request, response, jsonObject, "获取流程信息");
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).error("##################################################getProcessInfo" + e.getMessage());
            throw new UIException("获取流程信息", e);
        }
    }

//    @RequestMapping(value = "/workBaseController.do", params = "method=pageForwardController")
//    private ModelAndView pageForwardController(HttpServletResponse response, HttpServletRequest request, TaskInstance taskInstance, String type, String buildMethod, String fromPage) throws UIException {
//        try {
//            if ("draft".equals(fromPage)) {
//                ModelAndView modelAndView = new ModelAndView();
//                TaskFilter taskFilter = new TaskFilter();
//                taskFilter.setJobID(taskInstance.getJobID());
//                taskFilter.setActivityDefId(taskInstance.getActivityDefID());
//                PageCondition pageCon = new PageCondition();
//                pageCon.setLength(10000);
//                pageCon.setBegin(0);
//                pageCon.setIsCount(false);
//                taskFilter.setPageCondition(pageCon);
//                TaskInstance currentTask = null;
//                List<TaskInstance> instanceList = WorkflowAdapter.getMyWaitingTasks(taskFilter, getAccountId(request));
//                Assert.notEmpty(instanceList, "****未找到合并代办;jobId:" + taskInstance.getJobID() + ";ActivityDefId:" + taskInstance.getActivityDefID());
//                Date createDate = taskInstance.getCreateDate();
//                /*JsonConfig jsonConfig = new JsonConfig();
//                jsonConfig.setIgnoreDefaultExcludes(false);
//                jsonConfig.registerJsonValueProcessor(Date.class, new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));*/
//                Map<String, TaskInstance> taskMap = new HashMap<String, TaskInstance>();
//                for (TaskInstance instance : instanceList) {
//                    taskMap.put(instance.getProcessInstID(), instance);
//                    if (instance.getCreateDate().equals(createDate)) {
//                        currentTask = instance;
//                    }
//                }
//                if (currentTask == null) {
//                    currentTask = instanceList.get(0);
//                }
////                jsonConfig.setExcludes(new String[]{"participants", "processConditionMessage", "processMessage"});
//                modelAndView.addObject("taskinstance", JSON.toJSONString(currentTask));
//                modelAndView.addObject("taskMap", JSON.toJSONString(taskMap));
//                modelAndView.addObject("type", type);
//                modelAndView.addObject("shard", currentTask.getShard());
//                modelAndView.addObject("buildMethod", buildMethod);
//                if (StringUtils.isNotEmpty(Constants.suCheckActivityDefID) && Constants.suCheckActivityDefID.contains(taskInstance.getActivityDefID())) {
//                    //跳转反馈单上级部门审核页面
//                    modelAndView.setViewName("/frame/baseFrame");
////                    modelAndView.addObject("pageBuild", workflowBaseService.queryAppAndDisByFdbkTask(currentTask, getUserEntity(request)));
//                    modelAndView.addObject("links", JSONArray.fromObject(((PageBuildController) SpringContextUtils.getBean("pageBuildController")).getLinks(request, currentTask, "edit")));
//                } else {
//                    modelAndView.setViewName("/frame/newFrame");
//                    modelAndView.addObject("pageBuild", ((PageBuildController) SpringContextUtils.getBean("pageBuildController")).build(currentTask, type, request));
//                }
//                return modelAndView;
//            } else {
//                request.getRequestDispatcher("/frame/formFrame.jsp").forward(request, response);
//            }
//        } catch (Exception e) {
//            throw new UIException("***页面跳转***", e);
//        }
//        return null;
//    }

    @RequestMapping(value = "/workBaseuuController.do", params = "method=buildFormDetail")
    private ModelAndView buildFormDetail(HttpServletResponse response, HttpServletRequest request, TaskInstance taskInstance) throws UIException {
        try {
            ModelAndView modelAndView = new ModelAndView();
            /*JsonConfig jsonConfig = new JsonConfig();
            jsonConfig.setIgnoreDefaultExcludes(false);
            jsonConfig.registerJsonValueProcessor(Date.class, new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));*/
            modelAndView.addObject("taskinstance", JSON.toJSONString(taskInstance));
            modelAndView.addObject("taskMap", "{}");
            modelAndView.setViewName("/frame/dispatchDetail");
//            modelAndView.addObject("pageBuild", workflowBaseService.queryAppAndDisByRootProcess(taskInstance, getUserEntity(request)));
//            modelAndView.addObject("links", JSONArray.fromObject(((PageBuildController) SpringContextUtils.getBean("pageBuildController")).getLinks(request, taskInstance, "show")));
            return modelAndView;
        } catch (Exception e) {
            throw new UIException("***页面跳转***", e);
        }
    }

    @RequestMapping(value = "/workBaseController.do", params = "method=generateDisWorkOrderCode")
    private void generateDisWorkOrderCode(HttpServletResponse response, HttpServletRequest request, Long sequenceValue, String speciality, String businessType) throws UIException {
        try {
            UserEntity userEntity = getUserEntity(request);
            if (StringUtils.isNotEmpty(speciality)) {
                speciality = EnumConfigAdapter.getInstence().getEnumValueById(Integer.valueOf(speciality)).getEnumValueName();
            }
            endHandle(request, response, workflowBaseService.generateDisWorkOrderCode(userEntity, speciality, businessType, sequenceValue), null, false);
        } catch (Exception e) {
            throw new UIException("***生产工单编号***", e);
        }
    }


    @RequestMapping(value = "/workBaseController.do", params = "method=getWaitDoNumber")
    public void getWaitDoNumber(HttpServletRequest request, HttpServletResponse response, String userName) {
        JSONObject json = new JSONObject();
        try {
            if ("true".equals(Constants.IS_GROUPBY)) {
                getWaitDoNumber2(request, response, userName);
                return;
            }

            TaskFilter taskFilter = new TaskFilter();
            PageCondition page = new PageCondition();
            page.setBegin(0);
            page.setLength(1);
            page.setIsCount(true);
            taskFilter.setPageCondition(page);
            workflowBaseService.getMyWaitingTasks(taskFilter, userName);
            json.put("count", taskFilter.getPageCondition().getCount());
            endHandle(request, response, "callback(" + json.toJSONString() + ")", "", false);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                endHandle(request, response, json.toJSONString(), "", false);
            } catch (UIException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    @RequestMapping(value = "/workBaseController.do", params = "method=getComingTimeoutCount")
    public void getComingTimeoutCount(HttpServletRequest request, HttpServletResponse response, String userName) throws Exception {
        logger.info("即将超时待办数量查询。。。");

//        JSONObject json = new JSONObject();
//
//        String sql = "select count(0) count from bps.wfwait_view where participant=? and datcolumn1 < ?";
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.HOUR_OF_DAY , 2);
//        List list = baseDAO.findNativeSQL(sql, new Object[]{userName, new java.sql.Date(calendar.getTimeInMillis())});
//
//        if(list != null && list.size() > 0){
//            Map map = (Map) list.get(0);
//            json.put("count", map.get("count"));
//        } else {
//            json.put("count", 0);
//        }

//        BPMServiceFactory factory = BPMServiceFactory.getInstance();
//        List<TaskInstance> list = new ArrayList<TaskInstance>();
//
//        JobFilter jf = new JobFilter();
//        List<ExprType> tel = new ArrayList<ExprType>();
//        List<LogicType> logicList = new ArrayList<LogicType>();
//        jf = setPageCodition(logicList, tel, jf, request);
//
//        CriteriaType ct = CriteriaType.FACTORY.create();
//
//        SelectType selT = SelectType.FACTORY.create();
//        List<String> sfl = new ArrayList<String>();
//        sfl.add(Global.JOB_CODE.toLowerCase());
//        sfl.add(Global.JOB_TITLE.toLowerCase());
//
//        selT.set_field(sfl);
//        ct.set_select(selT);
////        ct.set_group(Constants.GROUPBY_COLUMN.toLowerCase());
//
//
//        if (!"".equals(Constants.PROCESS_MODELS) && Constants.PROCESS_MODELS != null) {
//            ExprType ls9 = ExprType.FACTORY.create();
//            ls9.set_opEnum(ExprType.OP.IN);
//            ls9.set_value(Constants.PROCESS_MODELS);
//            ls9.set_property("processdefname");
//            tel.add(ls9);
//        }
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Calendar calendar = Calendar.getInstance();
//
//        ExprType ls9 = ExprType.FACTORY.create();
//        ls9.set_opEnum(ExprType.OP.BETWEEN);
//        ls9.set_min(sdf.format(calendar.getTime()));
//        calendar.add(Calendar.HOUR_OF_DAY , 2);
//        ls9.set_max(sdf.format(calendar.getTime()));
//        ls9.set_property(Constants.BIZ_DATCOLUMN1.toLowerCase());
//        tel.add(ls9);
//
//        //设置自定义查询过滤列表
//        ct.set_expr(tel);
//        //设置自定义查询到JobFilter
//        jf.setObject(ct);
//        Date start_get_todoNumber = new Date();
//        logger.info("getMyWaitingTasksDistinctJobId ...number start");
//        list = WorkflowAdapter.getWorkflowService(userName).getMyWaitingTasksDistinctJobId(jf);
//        logger.info("getMyWaitingTasksDistinctJobId...number end  /COST = " + (new Date().getTime() - start_get_todoNumber.getTime()));
//        JSONObject json = new JSONObject();
//        json.put("count", jf.getPageCondition().getCount());
//        endHandle(request, response, "callback(" + json.toJSONString() + ")", "", false);
    }

    @RequestMapping(value = "/workBaseController.do", params = "method=getTimeoutCount")
    public void getTimeoutCount(HttpServletRequest request, HttpServletResponse response, String userName) throws Exception {
        logger.info("超时待办数量查询。。。");

//        JSONObject json = new JSONObject();
//
//        String sql = "select count(0) count from bps.wfwait_view where participant=? and datcolumn1 < ?";
//        List list = baseDAO.findNativeSQL(sql, new Object[]{userName, new java.sql.Date(new Date().getTime())});
//
//        if(list != null && list.size() > 0){
//            Map map = (Map) list.get(0);
//            json.put("count", map.get("count"));
//        } else {
//            json.put("count", 0);
//        }

//        BPMServiceFactory factory = BPMServiceFactory.getInstance();
//        List<TaskInstance> list = new ArrayList<TaskInstance>();
//
//        JobFilter jf = new JobFilter();
//        List<ExprType> tel = new ArrayList<ExprType>();
//        List<LogicType> logicList = new ArrayList<LogicType>();
//        jf = setPageCodition(logicList, tel, jf, request);
//
//        CriteriaType ct = CriteriaType.FACTORY.create();
//
//        SelectType selT = SelectType.FACTORY.create();
//        List<String> sfl = new ArrayList<String>();
//        sfl.add(Global.JOB_CODE.toLowerCase());
//        sfl.add(Global.JOB_TITLE.toLowerCase());
//
//        selT.set_field(sfl);
//        ct.set_select(selT);
////        ct.set_group(Constants.GROUPBY_COLUMN.toLowerCase());
//
//
//        if (!"".equals(Constants.PROCESS_MODELS) && Constants.PROCESS_MODELS != null) {
//            ExprType ls9 = ExprType.FACTORY.create();
//            ls9.set_opEnum(ExprType.OP.IN);
//            ls9.set_value(Constants.PROCESS_MODELS);
//            ls9.set_property("processdefname");
//            tel.add(ls9);
//        }
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        ExprType ls9 = ExprType.FACTORY.create();
//        ls9.set_opEnum(ExprType.OP.LE);
//        ls9.set_value(sdf.format(new Date()));
//        ls9.set_property(Constants.BIZ_DATCOLUMN1.toLowerCase());
//        tel.add(ls9);
//
//        //设置自定义查询过滤列表
//        ct.set_expr(tel);
//        //设置自定义查询到JobFilter
//        jf.setObject(ct);
//        Date start_get_todoNumber = new Date();
//        logger.info("getMyWaitingTasksDistinctJobId ...number start");
//        list = WorkflowAdapter.getWorkflowService(userName).getMyWaitingTasksDistinctJobId(jf);
//        logger.info("getMyWaitingTasksDistinctJobId...number end  /COST = " + (new Date().getTime() - start_get_todoNumber.getTime()));
//        JSONObject json = new JSONObject();
//        json.put("count", jf.getPageCondition().getCount());
//        endHandle(request, response, "callback(" + json.toJSONString() + ")", "", false);
    }


    @RequestMapping(value = "/workBaseController.do", params = "method=getAlreadyCount")
    public void getAlreadyCount(HttpServletRequest request, HttpServletResponse response, String userName) throws Exception {

        Pager pager = new Pager();
        pager.setPageSize(1);
        pager.getFastQueryParameters().put("in_processmodelname", Constants.PROCESS_MODELS);
        pager = WFServiceClient.getInstance().getMyCompletedTasks(userName, pager);
        JSONObject json = new JSONObject();
        json.put("count", pager.getRecordCount());
        endHandle(request, response, "callback(" + json.toJSONString() + ")", "", false);
    }

    @RequestMapping(value = "/workBaseController.do", params = "method=getWaitDoNumber2")
    public void getWaitDoNumber2(HttpServletRequest request, HttpServletResponse response, String userName) throws Exception {
        logger.info("待办数量查询。。。");
//        BPMServiceFactory factory = BPMServiceFactory.getInstance();
//        List<TaskInstance> list = new ArrayList<TaskInstance>();
//
//        JobFilter jf = new JobFilter();
//        List<ExprType> tel = new ArrayList<ExprType>();
//        List<LogicType> logicList = new ArrayList<LogicType>();
//        jf = setPageCodition(logicList, tel, jf, request);
//
//        CriteriaType ct = CriteriaType.FACTORY.create();
//
//        SelectType selT = SelectType.FACTORY.create();
//        List<String> sfl = new ArrayList<String>();
//        sfl.add(Global.JOB_CODE.toLowerCase());
//        sfl.add(Global.JOB_TITLE.toLowerCase());
//
//        selT.set_field(sfl);
//        ct.set_select(selT);
//        ct.set_group(Constants.GROUPBY_COLUMN.toLowerCase());
//
//
//        if (!"".equals(Constants.PROCESS_MODELS) && Constants.PROCESS_MODELS != null) {
//            ExprType ls9 = ExprType.FACTORY.create();
//            ls9.set_opEnum(ExprType.OP.IN);
//            ls9.set_value(Constants.PROCESS_MODELS);
//            ls9.set_property("processdefname");
//            tel.add(ls9);
//        }
//
//        ExprType ls9 = ExprType.FACTORY.create();
//        ls9.set_opEnum(ExprType.OP.GE);
//        ls9.set_value("2017-04-17 16:58:00");
//        ls9.set_property(Constants.BIZ_DATCOLUMN1.toLowerCase());
//        tel.add(ls9);
//
//        //设置自定义查询过滤列表
//        ct.set_expr(tel);
//        //设置自定义查询到JobFilter
//        jf.setObject(ct);
//        Date start_get_todoNumber = new Date();
//        logger.info("getMyWaitingTasksDistinctJobId ...number start");
//        list = WorkflowAdapter.getWorkflowService(userName).getMyWaitingTasksDistinctJobId(jf);
//        logger.info("getMyWaitingTasksDistinctJobId...number end  /COST = " + (new Date().getTime() - start_get_todoNumber.getTime()));
//        JSONObject json = new JSONObject();
//        json.put("count", jf.getPageCondition().getCount());
//        endHandle(request, response, "callback(" + json.toJSONString() + ")", "", false);
    }

    /**
     * 查询日历
     *
     * @param request
     * @param response
     * @param userName
     * @throws com.metarnet.core.common.exception.UIException
     */
//    @RequestMapping(value = "/workBaseController.do", params = "method=queryForCalendar")
//    public void queryForCalendar(HttpServletRequest request, HttpServletResponse response, String userName) {
//        HashMap map1 = new HashMap();
//        List dataList = new ArrayList();
//        HashMap jsonMap = null;
//        List<TaskInstance> list = null;
//        try {
//            TaskFilter taskFilter = new TaskFilter();
//            PageCondition page = new PageCondition();
//            page.setBegin(0);
//            page.setLength(100000);
//            page.setIsCount(false);
//            taskFilter.setPageCondition(page);
//            list = queryTodoList(taskFilter, response, request, userName);
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//            Integer num;
//            if (list != null) {
//                for (TaskInstance taskInstance : list) {
//                    if (taskInstance.getDatColumn1() != null && !"".equals(taskInstance.getDatColumn1())) {
//                        String stime = formatter.format(taskInstance.getDatColumn1().getTime()).toString();
//                        if (map1.containsKey(stime)) {
////                            num = (Integer)map1.get(stime) + 1;
////                            map1.remove(stime);
//                            map1.put(stime, (Integer) map1.get(stime) + 1);
//                        } else {
//                            map1.put(stime, 1);
//                        }
//                    } else {
//                        continue;
//                    }
//                }
//            }
//            Iterator iter = map1.entrySet().iterator();
//            while (iter.hasNext()) {
//                Map.Entry entry = (Map.Entry) iter.next();
//                jsonMap = new HashMap();
//                jsonMap.put("count", entry.getValue());
//                jsonMap.put("date", entry.getKey());
//                dataList.add(jsonMap);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        try {
//            endHandle(request, response, JSON.toJSONString(dataList), "", false);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    /**
     * 审核操作
     *
     * @param generalInfoModel 通用信息
     * @param taskInstance     待办信息
     * @throws com.metarnet.core.common.exception.UIException
     */
    @RequestMapping(value = "/workBaseController.do", params = "method=generalProcess")
    @ResponseBody
    public void generalProcess(GeneralInfoModel generalInfoModel, TaskInstance taskInstance, HttpServletRequest request, HttpServletResponse response, String participant) throws UIException {
        String msg = "";
        try {
            generalInfoModel.setIpAddress(HttpRequestUtil.getRemoteAddr(request));
            UserEntity userEntity = getUserEntity(request);
            String[] processInstIDArray = taskInstance.getProcessInstID().split(",");
            if (processInstIDArray.length > 1) {
                for (int i = 0; i < processInstIDArray.length; i++) {
                    String processInstID = processInstIDArray[i];
                    TaskFilter taskFilter = new TaskFilter();
                    taskFilter.setProcessInstID(processInstID);
                    List<TaskInstance> currTodoList = WorkflowAdapter.getMyWaitingTasks(taskFilter, userEntity.getUserName());
                    if (currTodoList != null && currTodoList.size() > 0) {
                        TaskInstance currTaskInst = currTodoList.get(0);
                        Map<String, Object> relativeData = WorkflowAdapter.getRelativeData(currTaskInst.getProcessInstID(), Arrays.asList(Constants.OBJECT_ID, Constants.OBJECT_TABLE, Constants.CREATE_USER), userEntity.getUserName());
                        Object objectId = relativeData.get(Constants.OBJECT_ID);
                        Object objectTable = relativeData.get(Constants.OBJECT_TABLE);
                        Object createUser = relativeData.get(Constants.CREATE_USER);
                        if (objectId == null) {
                            msg = "未设置OBJECT_ID[processInstID:" + processInstID + "]";
                        } else if (objectTable == null) {
                            msg = "未设置OBJECT_TABLE[processInstID:" + processInstID + "]";
                        } else {
                            GeneralInfoModel generalInfoModelNew = new GeneralInfoModel();
                            BeanUtils.copyProperties(generalInfoModel, generalInfoModelNew);
                            generalInfoModelNew.setProcessingObjectID(Long.valueOf(String.valueOf(objectId)));
                            generalInfoModelNew.setProcessingObjectTable(String.valueOf(objectTable));
                            if (createUser != null) {
                                Map map = new HashMap();
                                map.put(Constants.FIRST_STEP_USER, createUser.toString());
                                WorkflowAdapter.setRelativeData(currTaskInst.getProcessInstID(), map, userEntity.getUserName());
                            }
                            msg = generalProcess(generalInfoModelNew, currTaskInst, userEntity, participant);
                        }
                    }
                }
            } else {
                msg = generalProcess(generalInfoModel, taskInstance, userEntity, participant);
            }
        } catch (Exception e) {
            msg = "审核失败，请重试";
            throw new UIException("", e);
        } finally {
            JSONObject jsonObject = new JSONObject();
            if (StringUtils.isBlank(msg)) {
                jsonObject.put("success", true);
            } else {
                jsonObject.put("success", false);
                jsonObject.put("msg", msg);
            }
            endHandle(request, response, jsonObject, taskInstance.getTaskInstID());
        }
    }

    public String generalProcess(GeneralInfoModel generalInfoModel, TaskInstance taskInstance, UserEntity userEntity, String participant) throws UIException {
        String msg = "";
        try {
            Map<String, Object> relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList("processInstID", "nextStep", Constants.CREATE_USER), userEntity.getUserName());
            Map<String, Object> relateMap2 = new HashMap<String, Object>();
            if (generalInfoModel.getOperTypeEnumId() == 40050227) {
                //审核
                //流程新建，变更，废止审核通过和驳回都是走到流程启动者。任务派发没用到这块通用审核。
                relateMap2.put(Constants.CANDIDATEUSERS, relativeData.get(Constants.CREATE_USER));
                String nextCandidateUsers = relativeData.get(Constants.CREATE_USER).toString();
                String nextCandidateUserNames = "";
                if (nextCandidateUsers != null && !"".equals(nextCandidateUsers)) {
                    for (int i = 0; i < nextCandidateUsers.split(",").length; i++) {
                        String nextUser = nextCandidateUsers.split(",")[i].replace(":MEMBER", "");
                        try {
                            nextCandidateUserNames += AAAAAdapter.getInstence().findUserByUserName(nextUser).getTrueName() + ",";
                        } catch (PaasAAAAException e) {
                            e.printStackTrace();
                        }
                    }
                    if (nextCandidateUserNames.length() > 0)
                        nextCandidateUserNames = nextCandidateUserNames.substring(0, nextCandidateUserNames.length() - 1);
                }
                participant = nextCandidateUserNames;
                if ("Y".equals(generalInfoModel.getProcessingStatus())) {
                    relateMap2.put("nextStep", "非二级流程");
                } else {
                    relateMap2.put("nextStep", "退回");
                    relateMap2.put(Constants.ACTION_TYPE, "dispatch1");
                }
//                if ("Y".equals(generalInfoModel.getProcessingStatus())) {
//                    if (StringUtils.isNotBlank(taskInstance.getActivityInstID())) {
//                        Map<String, Object> map = new HashMap<String, Object>();
//                        if (relativeData.get("processInstID") != null) {
////                            if (relativeData.get("nextStep") != null && "parentStepUser".equals(relativeData.get("nextStep").toString())) {
////                                ProcessInstance processInstance = WorkflowAdapter.getProcessInstance(userEntity.getUserName(), relativeData.get("processInstID").toString());
////                                Map<String, Object> relativeDataParent = WorkflowAdapter.getRelativeData(processInstance.getParentProcessInstID(), Arrays.asList(Constants.FIRST_STEP_USER), userEntity.getUserName());
////                                map.put(Constants.FIRST_STEP_USER, relativeDataParent.get(Constants.FIRST_STEP_USER));
////                                WorkflowAdapter.setRelativeData(relativeData.get("processInstID").toString(), map, userEntity.getUserName());
////                            } else {
////                                OrgEntity orgEntity = null;
////                                try {
////                                    orgEntity = AAAAAdapter.getInstence().findOrgByOrgCode(relativeData.get(Constants.ORG_CODE).toString());
////                                } catch (PaasAAAAException e) {
////                                    e.printStackTrace();
////                                }
////                                String orgID = "";
////                                if ("sumAudit".equals(relativeData.get("nextStep").toString())) {
////                                    OrgEntity pOrg = null;
////                                    try {
////                                          pOrg = AAAAAdapter.getParentCompany(orgEntity.getOrgId().intValue());
////                                    } catch (PaasAAAAException e) {
////                                        e.printStackTrace();
////                                    }
////                                    orgID = pOrg.getOrgId().toString();
////                                } else {
////                                    orgID = orgEntity.getOrgId().toString();
////                                }
////                                if (relativeData.get("actionType") != null && "apply".equals(relativeData.get("actionType").toString()) && relativeData.get("up") != null) {
//////                                    OrgEntity orgEntity1 = AAAAAdapter.getInstence().findOrgByOrgCode(relativeData.get(Constants.ORG_CODE).toString());
////                                    OrgEntity pOrg = AAAAAdapter.getCompany(userEntity.getOrgID().intValue());
////                                    orgID = pOrg.getOrgId().toString();
////                                    map.put(Constants.ORG_CODE, pOrg.getOrgCode());
////                                    map.put(Constants.ORG_NAME, pOrg.getOrgName());
//////                                        map.put("up", "N");
////                                }
////                                List<Participant> participantList = AAAAAdapter.getInstence().findNextParticipants(relativeData.get("processName").toString(), relativeData.get("nextStep").toString(), relativeData.get(Constants.DIMENSION_MAJOR_CODE).toString(), orgID);
////                                if (participantList == null || participantList.size() == 0) {
////                                    msg = "下一步参与者未设置";
////                                } else {
////                                    map.put(Constants.FIRST_STEP_USER, "{" +
////                                            "'areacode':[]," +
////                                            "'majorcode':[]," +
////                                            "'orgcode':[]," +
////                                            "'productcode':[]," +
////                                            "'participant':" + com.alibaba.fastjson.JSONArray.toJSONString(participantList) +
////                                            "}");
////                                    WorkflowAdapter.setRelativeData(relativeData.get("processInstID").toString(), map, userEntity.getUserName());
////                                }
////                            }
//                        }
//                    }
//                } else {
//                    Map<String, Object> relateMap2 = new HashMap<String, Object>();
//                    relateMap2.put(Constants.FIRST_STEP_USER, relativeData.get(Constants.CREATE_USER));
//                    WorkflowAdapter.setRelativeData(relativeData.get("processInstID").toString(), relateMap2, userEntity.getUserName());
//                }
            } else if (generalInfoModel.getOperTypeEnumId() == 40050439) {
                relateMap2.put("nextStep", "归档");
            }
            WorkflowAdapter.setRelativeData(relativeData.get("processInstID").toString(), relateMap2, userEntity.getUserName());
            if (StringUtils.isBlank(msg)) {
                Map<String, Object> relateMap = new HashMap<String, Object>();
                relateMap.put("approvalStatus", generalInfoModel.getProcessingStatus());
                if (generalInfoModel.getOperTypeEnumId() == 40050227 && Constants.Y.equals(generalInfoModel.getProcessingStatus())) {
                    relateMap.put("PROCESS_STATUS", "lose");
                }
                WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relateMap, userEntity.getUserName());
                workflowBaseService.saveGeneralProcess(generalInfoModel, taskInstance, userEntity, participant);
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (AdapterException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    @RequestMapping(value = "/workBaseController.do", params = "method=saveAudit")
    @ResponseBody
    public void saveAudit(String levelType, GeneralInfoModel generalInfoModel, TaskInstance taskInstance, HttpServletRequest request, HttpServletResponse response) throws UIException {
        JSONObject jsonObject = new JSONObject();
        Boolean flag = false;
        String result = "";
        String specialty = "";
        UserEntity userEntity = getUserEntity(request);
        try {
            Map<String, Object> relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList("processInstID", "processName"), userEntity.getUserName());
            if (relativeData.get("processInstID") != null) {
//                specialty = String.valueOf(relativeData.get(Constants.DIMENSION_MAJOR_CODE));
//                if (StringUtils.isBlank(specialty) || "null".equals(specialty)) {
//                    specialty = "ALL";
//                }
//                if (StringUtils.isNotBlank(specialty) && !"null".equals(specialty)) {
                Map<String, Object> relateMap = new HashMap<String, Object>();
//                    relateMap.put("routeActivity", "sub");
                //generalInfoModel.setProcessingStatus("Y");
                List<Participant> participantList = new ArrayList<Participant>();
                if ("peer".equals(levelType)) {
                    Participant participant = new Participant();
                    participant.setParticipantID(generalInfoModel.getParticipantID());
                    participant.setParticipantName(generalInfoModel.getParticipantTrueName());
                    participant.setParticipantType("1");
                    participantList.add(participant);
//                    } else if ("up".equals(levelType)) {
//                        OrgEntity orgEntityCompany = AAAAAdapter.getCompany(userEntity.getOrgID().intValue());
//                        OrgEntity orgEntityParentC = null;
//                        if (orgEntityCompany.getOrgCode().length() == 3) {
//                            if (!orgEntityCompany.getOrgCode().equals("120")) {
//                                orgEntityParentC = AAAAAdapter.getInstence().findOrgByOrgCode("120");
//                            }
//                        } else if (orgEntityCompany.getOrgCode().length() == 5) {
//                            orgEntityParentC = AAAAAdapter.getInstence().findOrgByOrgCode(orgEntityCompany.getOrgCode().substring(0, 3));
//                        } else {
//                            orgEntityParentC = AAAAAdapter.getParentCompany(orgEntityCompany.getOrgId().intValue());
//                        }
//                        if (orgEntityParentC == null) {
//                            flag = true;
//                            result = "无上级组织，不能提交上级审核";
//                        } else {
//                            participantList = AAAAAdapter.getInstence().findNextParticipants(Constants.PROCESS_MODEL_NAME + ".apply", "audit", specialty, orgEntityParentC.getOrgId().toString());
//                            relateMap.put("up", "Y");
//                            Map<String, Object> relateMap2 = new HashMap<String, Object>();
//                            relateMap2.put("up", "Y");
//                            WorkflowAdapter.setRelativeData(relativeData.get("processInstID").toString(), relateMap2, userEntity.getUserName());
//                        }
                }
                if (participantList == null || participantList.size() == 0) {
                    flag = true;
                    if (StringUtils.isBlank(result)) {
                        result = "审核人未设置";
                    }
                } else {
                    jsonObject.put("success", true);
//                        relateMap.put(Constants.FIRST_STEP_USER,
//                                "{" +
//                                        "'areacode':[]," +
//                                        "'majorcode':[]," +
//                                        "'orgcode':[]," +
//                                        "'productcode':[]," +
//                                        "'participant':" + com.alibaba.fastjson.JSONArray.toJSONString(participantList) +
//                                        "}");
                    relateMap.put(Constants.CANDIDATEUSERS, generalInfoModel.getParticipantID());
                    WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relateMap, getUserEntity(request).getUserName());
                    generalInfoModel.setIpAddress(HttpRequestUtil.getRemoteAddr(request));
                    workflowBaseService.saveGeneralProcess(generalInfoModel, taskInstance, getUserEntity(request), generalInfoModel.getParticipantTrueName());
                }
//                } else {
//                    result = "未获取到专业数据";
//                }
//            } else {
//                result = "获取下一步参与者出错";
            }
        } catch (Exception e) {
            throw new UIException("", e);
        } finally {
            if (flag) {
                jsonObject.put("success", false);
                jsonObject.put("msg", result);
            } else {
                jsonObject.put("success", true);
            }
            endHandle(request, response, jsonObject, taskInstance.getTaskInstID());
        }
    }

    @RequestMapping(value = "/workBaseController.do", params = "method=queryWorkOrder")
    @ResponseBody
    public void queryWorkOrder(HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            Pager pager = PagerPropertyUtils.copy(request.getParameter("dtGridPager"));
            pager = workflowBaseService.queryWorkOrderList(pager, getUserEntity(request));
            SerializeConfig ser = new SerializeConfig();
            ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
            endHandle(request, response, JSON.toJSONString(pager, ser, SerializerFeature.WriteNullListAsEmpty), "todo");
        } catch (Exception e) {
            throw new UIException("todo", e);
        }
    }

    @RequestMapping(value = "/workBaseController.do", params = "method=LookFormDetail")
    private ModelAndView LookFormDetail(HttpServletResponse response, HttpServletRequest request, String processInstID) throws UIException {
        String appId = "";
        String isRoot = request.getParameter("isRoot");
        request.setAttribute("isRoot", isRoot);
        request.setAttribute("appMethod", Constants.APP__METHOD);
        request.setAttribute("disMethod", Constants.DIS__METHOD);
        request.setAttribute("feedBackMethod", Constants.FEEDBACK__METHOD);
        try {
            TaskInstance taskInstance1 = workflowBaseService.getTaskInstance2(processInstID, getAccountId(request));
            appId = taskInstance1.getJobID();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
        try {
            if (Constants.IS_SHOW_FORM) {
                iCommEntityService.queryAppAndDisByRootProcess(request, processInstID);
            } else {
                workflowBaseService.queryAppAndDisByRootProcess(request, processInstID);
            }
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (DAOException e) {
            e.printStackTrace();
        }
        String show = request.getParameter("show");
        if (show != null && "show".equals(show)) {
            show = "show";
        } else {
            show = "no";
        }
        return new ModelAndView(new InternalResourceView("/base/page/feedbackDetail.jsp?processInstID=" + processInstID + "&show=" + show + "&appId=" + appId + "&rootId=" + processInstID));
    }

    @RequestMapping(value = "/workBaseController.do", params = "method=test")
    private void test(HttpServletResponse response, HttpServletRequest request) throws UIException {
        try {
            WorkflowAdapter.addAndStartProcessWithParentActivityInstID("test", "", "415", "1681", null, "root-jx");
        } catch (AdapterException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/workBaseController.do", params = "method=backActivity")
    private void backActivity(GeneralInfoModel generalInfoModel, String processInstID, String taskInstID, HttpServletResponse response, HttpServletRequest request) throws UIException {
        JSONObject jsonObject = new JSONObject();
        String result = "";
        try {
            UserEntity userEntity = getUserEntity(request);
            Map<String, Object> relative = WorkflowAdapter.getRelativeData(processInstID, Arrays.asList(Constants.CANDIDATEUSERS), userEntity.getUserName());
//            relativeData.put(Constants.CANDIDATEUSERS, relativeData.get(Constants.CREATE_USER));
            Map<String, Object> relativeData = new HashMap<String, Object>();
            relativeData.put(Constants.CANDIDATEUSERS, userEntity.getUserName());
            relativeData.put(Constants.ACTION_TYPE, "dispatch1");
            relativeData.put("isWithdrawaudit", "N");
            WorkflowAdapter.setRelativeData(processInstID, relativeData, userEntity.getUserName());
            TaskInstance taskInstance = workflowBaseService.getTaskInstance(processInstID, "");
            WorkflowAdapter.backActivity(getAccountId(request), taskInstance.getTaskInstID(), taskInstID);

            if (generalInfoModel == null)
                generalInfoModel = new GeneralInfoModel();
            generalInfoModel.setOperTypeEnumId(40050444);
            generalInfoModel.setProcessingStatus("GETBACK");
            generalInfoModel.setIpAddress(HttpRequestUtil.getRemoteAddr(request));
            workflowBaseService.saveGeneralInfo(generalInfoModel, taskInstance, userEntity, userEntity.getTrueName());
            ActivityModel activityModel = ExtendNodeCofnig.parseActivity(userEntity.getUserName(),
                    taskInstance.getProcessModelName(), taskInstance.getActivityDefID());
            if (null != activityModel && activityModel.getPostProcessorList().size() > 0) {
                for (ProcessorModel postProcessor : activityModel.getPostProcessorList()) {
                    ((IWorkflowProcessor) SpringContextUtils.getBean(postProcessor.getName())).execute(taskInstance,
                            null, generalInfoModel, userEntity, postProcessor.getParams());
                }
            }

            try {
                final TaskInstance taskInst = taskInstance;
                final UserEntity user = userEntity;
                List<UserEntity> userEntityList = new ArrayList<UserEntity>();
                String candidateusers = relative.get(Constants.CANDIDATEUSERS).toString();
                UserEntity user1 = new UserEntity();
                user1.setUserName(candidateusers);
                userEntityList.add(user1);
                final List<UserEntity> userList = userEntityList;
                List<UserEntity> userEntityList1 = new ArrayList<UserEntity>();
                userEntityList1.add(userEntity);
                TaskInstance t = workflowBaseService.getTaskInstance(taskInstance.getProcessInstID(), "");
                final TaskInstance tt = t;
                final List<UserEntity> userList1 = userEntityList1;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_COMPLETE, taskInst, userList, user);
                        remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_CREATE, tt, userList1, user);
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            result = "回退失败";
            e.printStackTrace();
        }
        if (StringUtils.isBlank(result)) {
            jsonObject.put("success", true);
        } else {
            jsonObject.put("success", false);
            jsonObject.put("msg", result);
        }
        endHandle(request, response, jsonObject, "");
    }
}
