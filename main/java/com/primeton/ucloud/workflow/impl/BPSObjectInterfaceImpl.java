//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.primeton.ucloud.workflow.impl;

import com.eos.das.entity.DASManager;
import com.eos.das.entity.ExpressionHelper;
import com.eos.das.entity.IDASCriteria;
import com.eos.das.entity.criteria.CriteriaType;
import com.eos.das.entity.criteria.ExprType;
import com.eos.das.entity.criteria.OrderbyType;
import com.eos.das.entity.criteria.SelectType;
import com.eos.das.entity.criteria.ExprType.OP;
import com.eos.data.datacontext.DataContextManager;
import com.eos.foundation.data.DataObjectUtil;
import com.eos.workflow.api.BPSLoginManager;
import com.eos.workflow.api.BPSServiceClientFactory;
import com.eos.workflow.api.IBPSServiceClient;
import com.eos.workflow.api.IWFDefinitionQueryManager;
import com.eos.workflow.api.IWFProcessInstManager;
import com.eos.workflow.api.IWFQueryManager;
import com.eos.workflow.api.IWFWorkItemManager;
import com.eos.workflow.api.ext.BPSServiceClientFactoryExt;
import com.eos.workflow.api.ext.IBPSServiceManagerExt;
import com.eos.workflow.api.ext.bizdataset.WFBusiinfo;
import com.eos.workflow.data.NotificationOption;
import com.eos.workflow.data.WFActivityDefine;
import com.eos.workflow.data.WFActivityInst;
import com.eos.workflow.data.WFProcessDefine;
import com.eos.workflow.data.WFProcessInst;
import com.eos.workflow.data.WFTimePeriod;
import com.eos.workflow.data.WFWorkItem;
import com.eos.workflow.data.WFNotificationInst.State;
import com.eos.workflow.helper.ResultList;
import com.eos.workflow.omservice.WFParticipant;
import com.eos.workflow.omservice.WIParticipantInfo;
import com.primeton.bps.component.manager.api.BPSMgrServiceClientFactory;
import com.primeton.ucloud.workflow.impl.BPSObject;
import com.primeton.ucloud.workflow.impl.DataConvertor;
import com.primeton.ucloud.workflow.impl.FindWorkItem;
import com.primeton.ucloud.workflow.util.CalendarUtil;
import com.primeton.workflow.api.PageCond;
import com.primeton.workflow.api.WFReasonableException;
import com.primeton.workflow.api.WFServiceException;
import com.ucloud.paas.proxy.aaaa.AAAAService;
//import com.ucloud.paas.proxy.aaaa.entity.AccountEntity;
//import com.ucloud.paas.proxy.aaaa.entity.RoleEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import com.unicom.ucloud.workflow.exceptions.WFException;
import com.unicom.ucloud.workflow.filters.JobFilter;
import com.unicom.ucloud.workflow.filters.NotificationFilter;
import com.unicom.ucloud.workflow.filters.ProcessInstanceFilter;
import com.unicom.ucloud.workflow.filters.TaskFilter;
import com.unicom.ucloud.workflow.interfaces.WorkflowObjectInterface;
import com.unicom.ucloud.workflow.objects.ActivityDef;
import com.unicom.ucloud.workflow.objects.ActivityInstance;
import com.unicom.ucloud.workflow.objects.NotificationInstance;
import com.unicom.ucloud.workflow.objects.PageCondition;
import com.unicom.ucloud.workflow.objects.Participant;
import com.unicom.ucloud.workflow.objects.ProcessInstance;
import com.unicom.ucloud.workflow.objects.ProcessModel;
import com.unicom.ucloud.workflow.objects.ProcessModelParams;
import com.unicom.ucloud.workflow.objects.TaskInstance;
import commonj.sdo.DataObject;
import commonj.sdo.helper.DataFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class BPSObjectInterfaceImpl extends BPSObject implements WorkflowObjectInterface {
    private static Logger logger = Logger.getLogger(FindWorkItem.class);
    boolean isMultiTenantMode = false;

    public BPSObjectInterfaceImpl() {
    }

    public BPSObjectInterfaceImpl(String accountID, String appID, String appPassword) throws WFException {
        super(accountID, appID, appPassword);

        try {
            this.isMultiTenantMode = BPSMgrServiceClientFactory.getDefaultClient().getBPSWSManager().isMultiTenantMode();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        println("多租户：" + this.isMultiTenantMode);
        if(this.isMultiTenantMode && (appID == null || appID.length() == 0)) {
            throw new WFException("租户ID不能为空！");
        } else {
            BPSLoginManager loginManager = BPSServiceClientFactory.getLoginManager();
            loginManager.setCurrentUser(this.accountID, this.accountName, this.appID, this.appPassword);
        }
    }

    public String startProcess(String processModelId, ProcessModelParams processModelParams, Map<String, Object> bizModelParams, Participant participant, String processInstDesc) throws WFException {
        if(bizModelParams != null && bizModelParams.containsKey("jobID")) {
            long id = 0L;

            try {
                IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
                if(processModelParams == null) {
                    throw new WFException("startProcess方法中流程模型对象参数不能为空");
                } else {
                    Map params = processModelParams.getParameters();
                    id = e.getProcessInstManager().createProcessInstance(processModelParams.getProcessModelName(), processModelParams.getProcessInstName(), processInstDesc);
                    e.getRelativeDataManager().setRelativeData(id, "nextParticipant", DataConvertor.convert2WFParticipant(participant));
                    if(params != null && params.size() > 0) {
                        Object e1;
                        Date bizMap;
                        Long objParams;
                        Date majorcode;
                        if(params.containsKey("datColumn1") && params.get("datColumn1") != null) {
                            e1 = params.get("datColumn1");
                            if(e1 instanceof Date) {
                                bizMap = (Date)e1;
                                objParams = Long.valueOf(bizMap.getTime());
                                majorcode = new Date(objParams.longValue());
                                params.put("datColumn1", "" + majorcode.getTime());
                            }
                        }

                        if(params.containsKey("datColumn2") && params.get("datColumn2") != null) {
                            e1 = params.get("datColumn2");
                            if(e1 instanceof Date) {
                                bizMap = (Date)e1;
                                objParams = Long.valueOf(bizMap.getTime());
                                majorcode = new Date(objParams.longValue());
                                params.put("datColumn2", "" + majorcode.getTime());
                            }
                        }

                        e.getRelativeDataManager().setRelativeDataBatch(id, params);
                    }

                    this.setSenderIDToRelativeData(e, id);

                    try {
                        String e2 = "";
                        HashMap bizMap1 = new HashMap();
                        if(bizModelParams != null) {
                            if(bizModelParams.get("bizTableName") != null) {
                                e2 = (String)bizModelParams.get("bizTableName");
                                bizMap1.putAll(bizModelParams);
                                bizMap1.remove("bizTableName");
                            } else {
                                bizMap1.putAll(bizModelParams);
                            }
                        }

                        Date objParams1;
                        if(bizMap1.containsKey("jobStarttime") && bizMap1.get("jobStarttime") != null) {
                            objParams1 = (Date)bizMap1.get("jobStarttime");
                            bizMap1.put("jobStarttime", CalendarUtil.getTimeString(objParams1.getTime()));
                        }

                        if(bizMap1.containsKey("jobEndtime") && bizMap1.get("jobEndtime") != null) {
                            objParams1 = (Date)bizMap1.get("jobEndtime");
                            bizMap1.put("jobEndtime", CalendarUtil.getTimeString(objParams1.getTime()));
                        }

                        if(params != null && params.size() > 0) {
                            String objParams2 = (String)params.get("productcode");
                            String majorcode1 = (String)params.get("majorcode");
                            if(objParams2 != null && !"".equals(objParams2)) {
                                bizMap1.put("productcode", objParams2);
                            }

                            if(majorcode1 != null && !"".equals(majorcode1)) {
                                bizMap1.put("majorcode", majorcode1);
                            }
                        }

                        if(e2 == null || e2.equals("")) {
                            e2 = "Job";
                        }

                        Object[] objParams3 = new Object[params == null?0:params.size()];
                        if(params != null && params.size() > 0) {
                            objParams3 = params.values().toArray(objParams3);
                        }

                        e.getProcessInstManager().startProcessInstanceWithBizInfo(id, false, objParams3, e2, bizMap1);
                    } catch (Exception var14) {
                        if(id != 0L) {
                            ;
                        }

                        throw new WFException(var14);
                    }

                    return id + "";
                }
            } catch (WFServiceException var15) {
                throw new WFException(var15);
            }
        } else {
            throw new WFException("startProcess方法中bizModelParams对象jobID参数不能为空");
        }
    }

    private void setSenderIDToRelativeData(IBPSServiceClient client, long id) throws WFServiceException {
        String hisWfSenderID = "";
        Object his = client.getRelativeDataManager().getRelativeData(id, "wfSenderID");
        if(his != null) {
            hisWfSenderID = his.toString();
        }

        client.getRelativeDataManager().setRelativeData(id, "wfSenderID", this.accountID);
        client.getRelativeDataManager().setRelativeData(id, "hisWfSenderID", hisWfSenderID);
    }

    public void suspendProcessInstance(String processInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getProcessInstManager().suspendProcessInstance(Long.valueOf(processInstID).longValue());
        } catch (WFServiceException var3) {
            throw new WFException(var3);
        }
    }

    public void suspendActivityInstance(String activityInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getActivityInstManager().suspendActivityInstance(Long.valueOf(activityInstID).longValue());
        } catch (WFServiceException var3) {
            throw new WFException(var3);
        }
    }

    public void resumeProcessInstance(String processInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getProcessInstManager().resumeProcessInstance(Long.valueOf(processInstID).longValue());
        } catch (WFServiceException var3) {
            throw new WFException(var3);
        }
    }

    public void terminateProcessInstance(String processInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            Long id = Long.valueOf(processInstID);
            this.setSenderIDToRelativeData(e, id.longValue());
            e.getProcessInstManager().terminateProcessInstance(id.longValue());
        } catch (WFServiceException var4) {
            throw new WFException(var4);
        }
    }

    public void resumeActivityInstance(String activityInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getActivityInstManager().resumeActivityInstance(Long.valueOf(activityInstID).longValue());
        } catch (WFServiceException var3) {
            throw new WFException(var3);
        }
    }

    public String addProcesInstance(String parentProcessInstID, String actInstID, Participant participant, ProcessModelParams processModelParams) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            WFActivityInst activityInst = e.getActivityInstManager().findActivityInstByActivityInstID(Long.valueOf(actInstID).longValue());
            WFProcessInst processInst = e.getProcessInstManager().queryProcessInstDetail(Long.valueOf(parentProcessInstID).longValue());
            String processDefName = e.getDefinitionQueryManager().getActivity(processInst.getProcessDefID(), activityInst.getActivityDefID()).getSubProcessDefName();
            Object[] params = new Object[0];
            if(processModelParams != null && processModelParams.getParameters() != null && processModelParams.getParameters().size() > 0) {
                params = processModelParams.getParameters().values().toArray(params);
            }

            long subProcess = e.getProcessInstManager().addAndStartProcessWithParentActivityInstID(processDefName, processModelParams.getProcessInstName(), processModelParams.getProcessInstName(), Long.valueOf(parentProcessInstID).longValue(), Long.valueOf(actInstID).longValue(), false, params);
            return subProcess + "";
        } catch (WFServiceException var12) {
            throw new WFException(var12);
        }
    }

    public List<ProcessInstance> getSubProcessInstance(String processInstId) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            IDASCriteria criteria = DASManager.createCriteria("com.eos.workflow.data.WFProcessInst");
            criteria.add(ExpressionHelper.eq("parentProcID", processInstId));
            List list = e.getCommonQueryManage().queryProcessInstancesCriteria(criteria, new PageCond(2147483647));
            return DataConvertor.convert2ProcessInstList(list);
        } catch (WFServiceException var5) {
            throw new WFException(var5);
        } catch (ParseException var6) {
            throw new WFException(var6);
        }
    }

    public List<ActivityInstance> getActivityInstances(String processInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            List list = e.getActivityInstManager().queryActivityInstsByProcessInstID(Long.valueOf(processInstID).longValue(), new PageCond(2147483647));
            List activityInstList = DataConvertor.convert2ActivityInstList(list);
            Comparator compare = new Comparator() {

                public int compare(Object o1, Object o2) {
                    ActivityInstance a = (ActivityInstance) o1;
                    ActivityInstance b = (ActivityInstance) o2;
                    boolean flag = false;
                    long timecompare = 0L;
                    if(a.getStartTime() != null && b.getStartTime() != null) {
                        timecompare = a.getStartTime().getTime() - b.getStartTime().getTime();
                    }

                    long idcompare = Long.parseLong(a.getActivityInstID()) - Long.parseLong(b.getActivityInstID());
                    byte flag1;
                    if(timecompare == 0L) {
                        if(idcompare > 0L) {
                            flag1 = 1;
                        } else {
                            flag1 = -1;
                        }
                    } else {
                        int flag2 = (int)timecompare;
                        if(timecompare > 0L) {
                            flag1 = 1;
                        } else {
                            flag1 = -1;
                        }
                    }

                    return flag1;
                }
            };
            Collections.sort(activityInstList, compare);
            return activityInstList;
        } catch (WFServiceException var6) {
            throw new WFException(var6);
        } catch (ParseException var7) {
            throw new WFException(var7);
        }
    }

    public void backActivity(String currentActivityInstId, String targetActivityInstId) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getBackActivityManager().backActivity(Long.parseLong(currentActivityInstId), Long.parseLong(targetActivityInstId), "simple");
        } catch (WFServiceException var4) {
            throw new WFException(var4);
        }
    }

    public List<TaskInstance> getTaskInstancesByActivityID(String activityInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            List list = e.getWorkItemManager().queryWorkItemsByActivityInstID(Long.valueOf(activityInstID).longValue(), new PageCond(2147483647));
            ArrayList insts = new ArrayList();

            for(int i = 0; i < list.size(); ++i) {
                TaskInstance obj = this.getTaskInstanceObject(String.valueOf(((WFWorkItem)list.get(i)).getWorkItemID()));
                if(obj != null) {
                    insts.add(obj);
                }
            }

            return insts;
        } catch (Exception var7) {
            throw new WFException(var7);
        }
    }

    public List<TaskInstance> getMyWaitingTasks(TaskFilter taskfilter) throws WFException {
        taskfilter.setTaskType("1");
        return FindWorkItem.getMyTasks(taskfilter, this);
    }

    public List<TaskInstance> getMyCompletedTasks(TaskFilter taskfilter) throws WFException {
        taskfilter.setTaskType("2");
        return FindWorkItem.getMyTasks(taskfilter, this);
    }

    public int getTaskStatistics(TaskFilter taskfilter) throws WFException {
        try {
            PageCondition e = new PageCondition();
            e.setLength(1);
            e.setBegin(0);
            e.setIsCount(Boolean.valueOf(true));
            taskfilter.setPageCondition(e);
            FindWorkItem.getMyTasksForCount(taskfilter, this);
            return taskfilter.getPageCondition().getCount();
        } catch (WFException var3) {
            throw new WFException(var3);
        }
    }

    public TaskInstance getTaskInstanceObject(String taskInstID) throws WFException {
        TaskInstance taskInsts = null;

        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            WFWorkItem wi = e.getWorkItemManager().queryWorkItemDetail(Long.valueOf(taskInstID).longValue());
            TaskFilter taskfilter = new TaskFilter();
            taskfilter.setTaskInstID(String.valueOf(wi.getWorkItemID()));
            taskInsts = DataConvertor.convert2TaskInstance(wi);
            DataObject criteriaEntity = DataObjectUtil.createDataObject("com.primeton.das.criteria.criteriaType");
            criteriaEntity.set("_entity", "com.eos.workflow.api.ext.bizdataset.WFBusiinfo");
            criteriaEntity.set("/_expr[1]/workItemID", taskInstID);
            DataObject pageCond = DataFactory.INSTANCE.create("com.eos.foundation", "PageCond");
            PageCond pc = new PageCond(2147483647);
            pageCond.set("begin", Integer.valueOf(pc.getBegin()));
            pageCond.set("length", Integer.valueOf(pc.getLength()));
            pageCond.set("isCount", Boolean.valueOf(pc.getIsCount()));
            DataObject[] datas = ((IBPSServiceManagerExt)BPSServiceClientFactoryExt.getDefaultClient().getService(IBPSServiceManagerExt.class)).queryEntitiesByCriteriaEntity(criteriaEntity, pageCond);
            if(datas != null) {
                for(int i = 1; i < datas.length; ++i) {
                    if(datas[i].getString("jobid") != null) {
                        taskInsts.setJobID(datas[i].getString("jobid"));
                    }

                    if(datas[i].getString("jobcode") != null) {
                        taskInsts.setJobCode(datas[i].getString("jobcode"));
                    }

                    if(datas[i].getString("jobtype") != null) {
                        taskInsts.setJobtype(datas[i].getString("jobtype"));
                    }

                    if(datas[i].getString("jobtitle") != null) {
                        taskInsts.setJobTitle(datas[i].getString("jobtitle"));
                    }

                    if(datas[i].getString("shard") != null) {
                        taskInsts.setShard(datas[i].getString("shard"));
                    }

                    if(datas[i].getString("businessid") != null) {
                        taskInsts.setBusinessId(datas[i].getString("businessid"));
                    }

                    if(datas[i].getString("productcode") != null) {
                        taskInsts.setPRODUCT_ID(datas[i].getString("productcode"));
                    }

                    if(datas[i].getString("majorcode") != null) {
                        taskInsts.setMAJOR_ID(datas[i].getString("majorcode"));
                    }

                    String rc2;
                    if(datas[i].get("jobstarttime") != null) {
                        rc2 = datas[i].get("jobstarttime").toString();
                        taskInsts.setJobStarttime(new Date(CalendarUtil.getTime(rc2)));
                    }

                    if(datas[i].get("jobendtime") != null) {
                        rc2 = datas[i].get("jobendtime").toString();
                        taskInsts.setJobEndtime(new Date(CalendarUtil.getTime(rc2)));
                    }

                    if(datas[i].getString("rootvccolumn1") != null) {
                        taskInsts.setRootvcColumn1(datas[i].getString("rootvccolumn1"));
                    }

                    if(datas[i].getString("rootvccolumn2") != null) {
                        taskInsts.setRootvcColumn2(datas[i].getString("rootvccolumn2"));
                    }

                    int var14;
                    if(datas[i].get("rootnmcolumn1") != null && !datas[i].get("rootnmcolumn1").toString().equals("0.0000")) {
                        var14 = Integer.parseInt(datas[i].get("rootnmcolumn1").toString());
                        taskInsts.setRootnmColumn1(var14);
                    }

                    if(datas[i].get("rootnmcolumn2") != null && !datas[i].get("rootnmcolumn2").toString().equals("0.0000")) {
                        var14 = Integer.parseInt(datas[i].get("rootnmcolumn2").toString());
                        taskInsts.setRootnmColumn2(var14);
                    }

                    if(datas[i].getString("senderid") != null) {
                        taskInsts.setSenderID(datas[i].getString("senderid"));
                    }

                    if(datas[i].getString("receiverid") != null) {
                        taskInsts.setReceiverID(datas[i].getString("receiverid"));
                    }

                    if(datas[i].getDate("rebacktime") != null) {
                        taskInsts.setReBacktime(datas[i].getDate("rebacktime"));
                    }

                    if(datas[i].getString("strcolumn1") != null) {
                        taskInsts.setStrColumn1(datas[i].getString("strcolumn1"));
                    }

                    if(datas[i].getString("strcolumn2") != null) {
                        taskInsts.setStrColumn2(datas[i].getString("strcolumn2"));
                    }

                    if(datas[i].getString("strcolumn3") != null) {
                        taskInsts.setStrColumn3(datas[i].getString("strcolumn3"));
                    }

                    if(datas[i].getString("strcolumn4") != null) {
                        taskInsts.setStrColumn4(datas[i].getString("strcolumn4"));
                    }

                    if(datas[i].getString("strcolumn5") != null) {
                        taskInsts.setStrColumn5(datas[i].getString("strcolumn5"));
                    }

                    if(datas[i].getString("strcolumn6") != null) {
                        taskInsts.setStrColumn6(datas[i].getString("strcolumn6"));
                    }

                    if(datas[i].getString("strcolumn7") != null) {
                        taskInsts.setStrColumn7(datas[i].getString("strcolumn7"));
                    }

                    if(datas[i].getDate("datcolumn1") != null) {
                        taskInsts.setDatColumn1(datas[i].getDate("datcolumn1"));
                    }

                    if(datas[i].getDate("datcolumn2") != null) {
                        taskInsts.setDatColumn2(datas[i].getDate("datcolumn2"));
                    }

                    if(datas[i].get("numcolumn1") != null && !datas[i].get("numcolumn1").toString().equals("0.0000")) {
                        taskInsts.setNumColumn1(datas[i].getInt("numcolumn1"));
                    }

                    if(datas[i].get("numcolumn2") != null && !datas[i].get("numcolumn2").toString().equals("0.0000")) {
                        taskInsts.setNumColumn2(datas[i].getInt("numcolumn2"));
                    }
                }
            }

            return taskInsts;
        } catch (WFServiceException var12) {
            throw new WFException(var12);
        } catch (Exception var13) {
            throw new WFException(var13);
        }
    }

    public void submitTask(TaskInstance taskInstance, List<Participant> participants) throws WFException {
        try {
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();
            WFParticipant[] e = null;
            if(participants != null) {
                e = new WFParticipant[participants.size()];

                for(int wi = 0; wi < participants.size(); ++wi) {
                    e[wi] = DataConvertor.convert2WFParticipant((Participant)participants.get(wi));
                }

                List var12 = client.getProcessInstManager().getNextActivitiesMaybeArrived(Long.parseLong(taskInstance.getActivityInstID()));
                if(var12.size() != 1 || !((WFActivityDefine)var12.get(0)).getType().equals("manual")) {
                    throw new RuntimeException("多个后继活动无法处理或者不是人工活动");
                }

                client.getAppointActivityManager().appointActivityParticipant(Long.parseLong(taskInstance.getTaskInstID()), ((WFActivityDefine)var12.get(0)).getId(), e);
            }

            WFWorkItem var13 = client.getWorkItemManager().queryWorkItemDetail(Long.valueOf(taskInstance.getTaskInstID()).longValue());
            int state = var13.getCurrentState();
            if(state != 4 && state != 10) {
                throw new RuntimeException("任务状态必须为运行态才能提交");
            } else if(taskInstance.getTaskInstID() != null && taskInstance.getTaskInstID().length() != 0) {
                try {
                    long e1 = var13.getProcessInstID();
                    this.setSenderIDToRelativeData(client, e1);
                } catch (NumberFormatException var9) {
                    throw new WFException("流程实例ID错误", var9);
                }

                client.getWorkItemManager().finishWorkItem(Long.valueOf(taskInstance.getTaskInstID()).longValue(), false);
            } else {
                throw new WFException("任务实例ID错误");
            }
        } catch (WFServiceException var10) {
            throw new WFException(var10);
        } catch (WFReasonableException var11) {
            throw new WFException(var11);
        }
    }

    public void forwardTask(String workItemId, List<Participant> participants) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            WFParticipant[] parts = new WFParticipant[participants.size()];
            if(participants != null) {
                for(int i = 0; i < participants.size(); ++i) {
                    parts[i] = DataConvertor.convert2WFParticipant((Participant)participants.get(i));
                }
            }

            e.getWorkItemManager().reassignWorkItemEx(Long.valueOf(workItemId).longValue(), parts);
        } catch (WFServiceException var6) {
            throw new WFException(var6);
        }
    }

    public void claimTask(String taskInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getWorkItemManager().assignWorkItemToSelf(Long.valueOf(taskInstID).longValue());
        } catch (WFServiceException var3) {
            throw new WFException(var3);
        }
    }

    public void revokeClaimTask(String taskInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getWorkItemManager().withdrawWorkItem(Long.valueOf(taskInstID).longValue());
        } catch (WFServiceException var3) {
            throw new WFException(var3);
        }
    }

    public void delegateTask(String taskInstID, List<Participant> participants) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            WFParticipant[] parts = new WFParticipant[participants.size()];
            if(participants != null) {
                for(int i = 0; i < participants.size(); ++i) {
                    parts[i] = DataConvertor.convert2WFParticipant((Participant)participants.get(i));
                }
            }

            e.getDelegateManager().delegateWorkItem(Long.valueOf(taskInstID).longValue(), parts, "DELEG");
        } catch (WFServiceException var6) {
            throw new WFException(var6);
        }
    }

    public void resetTaskTimeOut(String taskInstID, Date date) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            long id = Long.parseLong(taskInstID);
            WFWorkItem wi = e.getWorkItemManager().queryWorkItemDetail(id);
            WFTimePeriod timeLimit = new WFTimePeriod();
            long startTime = CalendarUtil.getTime(wi.getStartTime());
            long finalTime = date.getTime();
            long time = finalTime - startTime;
            timeLimit.setMinute(Long.valueOf(time / 1000L / 60L).intValue());
            int r = e.getWorkItemManager().setWorkItemTimeLimit(id, timeLimit, (WFTimePeriod)null);
            if(r != 1) {
                throw new WFException("超时限制设置失败");
            }
        } catch (Exception var15) {
            throw new WFException(var15);
        }
    }

    public void setRelativeData(String processInstID, Map<String, Object> map) throws WFException {
        if(map == null) {
            throw new WFException("setRelativeData方法中设置更新内容不能为空");
        } else if(processInstID == null) {
            throw new WFException("setRelativeData方法中流程实例ID不能为空");
        } else {
            Date e;
            if(map.containsKey("reBacktime") && map.get("reBacktime") != null) {
                e = (Date)map.get("reBacktime");
                map.put("reBacktime", "" + e.getTime());
            }

            if(map.containsKey("datColumn1") && map.get("datColumn1") != null) {
                e = (Date)map.get("datColumn1");
                map.put("datColumn1", "" + e.getTime());
            }

            if(map.containsKey("datColumn2") && map.get("datColumn2") != null) {
                e = (Date)map.get("datColumn2");
                map.put("datColumn2", "" + e.getTime());
            }

            try {
                IBPSServiceClient var12 = BPSServiceClientFactory.getDefaultClient();
                String[] keys = (String[])map.keySet().toArray(new String[map.size()]);

                for(int i = 0; i < keys.length; ++i) {
                    Object o = map.get(keys[i]);
                    if(o != null) {
                        if(o instanceof Participant) {
                            map.put(keys[i], DataConvertor.convert2WFParticipant((Participant)o));
                        } else {
                            int j;
                            Object item;
                            if(o instanceof List) {
                                List var14 = (List)o;

                                for(j = 0; j < var14.size(); ++j) {
                                    item = var14.get(j);
                                    if(item != null && item instanceof Participant) {
                                        var14.set(j, DataConvertor.convert2WFParticipant((Participant)item));
                                    }
                                }

                                map.put(keys[i], var14);
                            } else if(o instanceof Participant[]) {
                                Participant[] var13 = (Participant[])((Participant[])o);
                                WFParticipant[] var15 = new WFParticipant[var13.length];

                                for(int var16 = 0; var16 < var13.length; ++var16) {
                                    var15[var16] = DataConvertor.convert2WFParticipant(var13[var16]);
                                }

                                map.put(keys[i], var15);
                            } else if(o instanceof Object[]) {
                                Object[] objs = (Object[])((Object[])o);

                                for(j = 0; j < objs.length; ++j) {
                                    item = objs[j];
                                    if(item != null && item instanceof Participant) {
                                        Participant part = (Participant)item;
                                        objs[j] = DataConvertor.convert2WFParticipant(part);
                                    }
                                }

                                map.put(keys[i], objs);
                            }
                        }
                    }
                }

                var12.getRelativeDataManager().setRelativeDataBatch(Long.parseLong(processInstID), map);
            } catch (WFServiceException var11) {
                throw new WFException(var11);
            }
        }
    }

    public Map<String, Object> getRelativeData(String processInstID, List<String> keys) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            if(keys == null) {
                throw new WFException("getRelativeData方法中关键字不能为空");
            } else if(processInstID == null) {
                throw new WFException("getRelativeData方法中流程实例ID不能为空");
            } else {
                String[] array = new String[keys.size()];
                List values = e.getRelativeDataManager().getRelativeDataBatch(Long.parseLong(processInstID), (String[])keys.toArray(array));
                HashMap map = new HashMap();

                for(int i = 0; i < keys.size(); ++i) {
                    if(values.get(i) != null) {
                        if(values.get(i) instanceof WFParticipant) {
                            map.put(keys.get(i), DataConvertor.convert2Participant((WFParticipant)values.get(i)));
                        } else {
                            int j;
                            if(values.get(i) instanceof List) {
                                List var13 = (List)values.get(i);

                                for(j = 0; j < var13.size(); ++j) {
                                    Object var15 = var13.get(j);
                                    if(var15 != null && var15 instanceof WFParticipant) {
                                        var13.set(j, DataConvertor.convert2Participant((WFParticipant)var15));
                                    }
                                }

                                map.put(keys.get(i), var13);
                            } else if(values.get(i) instanceof WFParticipant[]) {
                                WFParticipant[] var12 = (WFParticipant[])((WFParticipant[])values.get(i));
                                Participant[] var14 = new Participant[var12.length];

                                for(int j1 = 0; j1 < var14.length; ++j1) {
                                    var14[j1] = DataConvertor.convert2Participant(var12[j1]);
                                }

                                map.put(keys.get(i), var14);
                            } else if(!(values.get(i) instanceof Object[])) {
                                map.put(keys.get(i), values.get(i));
                            } else {
                                Object[] objs = (Object[])((Object[])values.get(i));

                                for(j = 0; j < objs.length; ++j) {
                                    if(objs[j] != null && objs[j] instanceof WFParticipant) {
                                        objs[j] = DataConvertor.convert2Participant((WFParticipant)objs[j]);
                                    }
                                }

                                map.put(keys.get(i), objs);
                            }
                        }
                    }
                }

                return map;
            }
        } catch (WFServiceException var11) {
            throw new WFException(var11);
        }
    }

    public void appointActivityParticipant(String taskInstId, String activityID, List<Participant> participants) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            WFParticipant[] parts = new WFParticipant[participants.size()];
            if(participants != null) {
                for(int i = 0; i < participants.size(); ++i) {
                    parts[i] = DataConvertor.convert2WFParticipant((Participant)participants.get(i));
                }
            }

            e.getAppointActivityManager().appointActivityParticipant(Long.parseLong(taskInstId), activityID, parts);
        } catch (WFServiceException var7) {
            throw new WFException(var7);
        }
    }

    public List<ActivityDef> getActivitDefLists(String processModelID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            PageCond pagec = new PageCond(2147483647);
            List listWpd = e.getDefinitionQueryManager().queryProcessesByName(processModelID, "published", pagec);
            WFProcessDefine wfd1 = (WFProcessDefine)listWpd.get(0);
            ArrayList activityDefs = new ArrayList();
            List list = e.getDefinitionQueryManager().queryActivitiesOfProcess(wfd1.getProcessDefID().longValue());
            Iterator i$ = list.iterator();

            while(i$.hasNext()) {
                WFActivityDefine define = (WFActivityDefine)i$.next();
                activityDefs.add(DataConvertor.convert2WFActivityDefine(define));
            }

            return activityDefs;
        } catch (WFServiceException var10) {
            throw new WFException(var10);
        }
    }

    public String getActivityExtendAttributes(String taskInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            WFWorkItem wi = e.getWorkItemManager().queryWorkItemDetail(Long.valueOf(taskInstID).longValue());
            String extendAttributes = e.getDefinitionQueryManager().getExtendAttribute(wi.getProcessDefID(), wi.getActivityDefID());
            return extendAttributes;
        } catch (WFServiceException var5) {
            throw new WFException(var5);
        }
    }

    public List<ActivityDef> getNextActivitiesMaybeArrived(String activityInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            ArrayList activityDefs = new ArrayList();
            List list = e.getProcessInstManager().getNextActivitiesMaybeArrived(Long.parseLong(activityInstID));
            Iterator i$ = list.iterator();

            while(i$.hasNext()) {
                WFActivityDefine define = (WFActivityDefine)i$.next();
                activityDefs.add(DataConvertor.convert2WFActivityDefine(define));
            }

            return activityDefs;
        } catch (WFServiceException var7) {
            throw new WFException(var7);
        }
    }

    public List<ProcessModel> getProcessModeLists(String appid) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            IWFDefinitionQueryManager query = e.getDefinitionQueryManager();
            List defines = query.queryPublishedProcesses((PageCond)null);
            ArrayList models = new ArrayList();
            Iterator i$ = defines.iterator();

            while(i$.hasNext()) {
                WFProcessDefine define = (WFProcessDefine)i$.next();
                models.add(DataConvertor.convert2ProcessModel(define));
            }

            return models;
        } catch (WFServiceException var8) {
            throw new WFException(var8);
        }
    }

    public void ccTask(String workItemID, List<Participant> parts, String url) throws WFException {
        try {
            if(parts != null && parts.size() != 0) {
                WFParticipant[] e = new WFParticipant[parts.size()];

                for(int client = 0; client < parts.size(); ++client) {
                    e[client] = DataConvertor.convert2WFParticipant((Participant)parts.get(client));
                }

                IBPSServiceClient var15 = BPSServiceClientFactory.getDefaultClient();
                WFWorkItem workItem = var15.getWorkItemManager().queryWorkItemDetail(Long.parseLong(workItemID));
                NotificationOption option = new NotificationOption();
                option.setExpandParticipant(true);
//                option.setSpecifyURL(url);
                TaskFilter taskfilter = new TaskFilter();
                taskfilter.setTaskInstID(workItemID);
                taskfilter.setTaskType("1");
                PageCondition pageCon = new PageCondition();
                pageCon.setLength(1);
                pageCon.setBegin(0);
                pageCon.setIsCount(Boolean.valueOf(false));
                taskfilter.setPageCondition(pageCon);
                List s = FindWorkItem.getMyTasks(taskfilter, this);
                String jobID;
                String jobTitle;
                TaskInstance ss;
                if(s != null && s.size() == 1) {
                    ss = (TaskInstance)s.get(0);
                    jobID = ss.getJobID();
                    jobTitle = ss.getJobTitle();
                } else {
                    jobTitle = null;
                    jobID = null;
                }

                if(jobID == null) {
                    taskfilter.setTaskInstID(workItemID);
                    taskfilter.setTaskType("2");
                    s = FindWorkItem.getMyTasks(taskfilter, this);
                    if(s != null && s.size() == 1) {
                        ss = (TaskInstance)s.get(0);
                        jobID = ss.getJobID();
                        jobTitle = ss.getJobTitle();
                    } else {
                        jobTitle = null;
                        jobID = null;
                    }
                }

                var15.getNotificationManager().sendTaskNotification(this.accountID, e, jobTitle, jobID, workItem, option);
            } else {
                throw new WFException("被抄送的参数者为空");
            }
        } catch (WFServiceException var14) {
            throw new WFException(var14);
        }
    }

    public String getProcessView(String processInstID) throws WFException {
        return "普元流程引擎通过页面标签实现";
    }

    public List<NotificationInstance> getNotfInstancesByActivityID(String activityInstID) throws WFException {
        ArrayList result = new ArrayList();

        try {
            NotificationFilter e = new NotificationFilter();
            List rnotis = this.getMyReadNotifications(e);
            List unotis = this.getMyUnreadNotifications(e);
            ArrayList notis = new ArrayList();
            if(rnotis != null && rnotis.size() > 0) {
                notis.addAll(rnotis);
            }

            if(unotis != null && unotis.size() > 0) {
                notis.addAll(unotis);
            }

            for(int i = 0; i < notis.size(); ++i) {
                if(notis.get(i) != null && String.valueOf(((NotificationInstance)notis.get(i)).getActivitiInstID()).equals(String.valueOf(activityInstID))) {
                    result.add(notis.get(i));
                }
            }

            return result;
        } catch (Exception var8) {
            throw new WFException(var8);
        }
    }

    public List<NotificationInstance> getMyUnreadNotifications(NotificationFilter notificationfilter) throws WFException {
        State state = State.UNVIEWED;
        return this.getNotifications(notificationfilter, state);
    }

    public List<NotificationInstance> getMyReadNotifications(NotificationFilter notificationfilter) throws WFException {
        State state = State.VIEWED;
        return this.getNotifications(notificationfilter, state);
    }

    private List<NotificationInstance> getNotifications(NotificationFilter notificationfilter, State state) throws WFException {
        List notis = null;

        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            PageCond cond = notificationfilter != null && notificationfilter.getPageCondition() != null?DataConvertor.convert2PageCond(notificationfilter.getPageCondition()):new PageCond(2147483647);
            IDASCriteria AccountCriteria = DASManager.createCriteria("com.eos.workflow.data.WFNotificationInst");
            String ProcessModelID = notificationfilter.getProcessModelID();
            if(ProcessModelID != null && ProcessModelID.length() != 0) {
                AccountCriteria.add(ExpressionHelper.eq("procDefID", ProcessModelID));
            }

            String processModelName = notificationfilter.getProcessModelName();
            if(processModelName != null && processModelName.length() != 0) {
                if(processModelName.contains(",")) {
                    AccountCriteria.add(ExpressionHelper.in("procDefName", processModelName.split(",")));
                } else {
                    AccountCriteria.add(ExpressionHelper.eq("procDefName", processModelName));
                }
            }

            String appID = notificationfilter.getAppID();
            if(appID != null && appID.length() != 0) {
                throw new WFException("appID属性不支持查询");
            } else {
                String notificationInstID = notificationfilter.getNotificationInstID();
                if(notificationInstID != null && notificationInstID.length() != 0) {
                    AccountCriteria.add(ExpressionHelper.eq("notificationID", notificationInstID));
                }

                String processInstID = notificationfilter.getProcessInstID();
                if(processInstID != null && processInstID.length() != 0) {
                    AccountCriteria.add(ExpressionHelper.eq("procInstID", processInstID));
                }

                String activityID = notificationfilter.getActivityID();
                if(activityID != null && activityID.length() != 0) {
                    AccountCriteria.add(ExpressionHelper.eq("actInstID", activityID));
                }

                String ActivityName = notificationfilter.getActivityName();
                if(ActivityName != null && ActivityName.length() != 0) {
                    AccountCriteria.add(ExpressionHelper.like("actInstName", "%" + ActivityName + "%"));
                }

                String JobTitle = notificationfilter.getJobTitle();
                if(JobTitle != null && JobTitle.length() != 0) {
                    AccountCriteria.add(ExpressionHelper.like("title", "%" + JobTitle + "%"));
                }

                String JobID = notificationfilter.getJobID();
                if(JobID != null && JobID.length() != 0) {
                    AccountCriteria.add(ExpressionHelper.like("message", "%" + JobID + "%"));
                    AccountCriteria.desc("message");
                } else {
                    AccountCriteria.desc("createTime");
                }

                String SenderType = notificationfilter.getSenderType();
                if(SenderType != null && SenderType.length() != 0) {
                    throw new WFException("SenderType属性不支持查询");
                } else {
                    String Sender = notificationfilter.getSender();
                    if(Sender != null && Sender.length() != 0) {
                        AccountCriteria.add(ExpressionHelper.eq("sender", Sender));
                    }

                    String currentUserID = DataContextManager.current().getMUODataContext().getUserObject().getUserId();
                    if(currentUserID != null && !"".equals(currentUserID)) {
                        AccountCriteria.add(ExpressionHelper.eq("recipient", "P{" + currentUserID + "}"));
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date beginStartDate = notificationfilter.getBeginDeliveryDate();
                        Date endStartDate = notificationfilter.getEndDeliveryDate();
                        if(beginStartDate != null && endStartDate != null) {
                            AccountCriteria.add(ExpressionHelper.between("createTime", sdf.format(beginStartDate), sdf.format(endStartDate)));
                        }

                        AccountCriteria.add(ExpressionHelper.eq("state", state));
                        List wfnotifis = e.getNotificationManager().queryNotificationsCriteria(AccountCriteria, cond);
                        notis = DataConvertor.convert2NotificationList(wfnotifis);

                        try {
                            ResultList e1 = (ResultList)wfnotifis;
                            PageCond pageCond = e1.getPageCond();
                            PageCondition pageCondition = notificationfilter.getPageCondition();
                            if(pageCondition != null) {
                                DataConvertor.convert2PageCond(pageCond, pageCondition);
                            }

                            return notis;
                        } catch (Exception var26) {
                            throw new WFException("通知的分页信息转换异常", var26);
                        }
                    } else {
                        throw new WFException("未获取到消息处理人");
                    }
                }
            }
        } catch (WFServiceException var27) {
            throw new WFException(var27);
        } catch (ParseException var28) {
            throw new WFException(var28);
        }
    }

    public void setNotificationToRead(String notifyInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getNotificationManager().confirmNotification(Long.parseLong(notifyInstID));
        } catch (WFServiceException var3) {
            throw new WFException(var3);
        }
    }

    public int getNotifyStatistics(NotificationFilter filter) throws WFException {
        if(filter == null) {
            filter = new NotificationFilter();
        }

        PageCondition con = new PageCondition();
        con.setBegin(0);
        con.setIsCount(Boolean.valueOf(true));
        con.setLength(2147483647);
        filter.setPageCondition(con);
        List unotis;
        if(filter.getNotifyType() == null) {
            unotis = this.getMyReadNotifications(filter);
            List unotis1 = this.getMyUnreadNotifications(filter);
            int size = (unotis == null?0:unotis.size()) + (unotis1 == null?0:unotis1.size());
            return size;
        } else if(filter.getNotifyType().equals("1")) {
            unotis = this.getMyReadNotifications(filter);
            return unotis == null?0:unotis.size();
        } else if(filter.getNotifyType().equals("2")) {
            unotis = this.getMyUnreadNotifications(filter);
            return unotis == null?0:unotis.size();
        } else {
            return 0;
        }
    }

    public ProcessInstance getProcessInstance(String processInstId) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            IWFProcessInstManager wim = e.getProcessInstManager();
            WFProcessInst is = wim.queryProcessInstDetail(Long.valueOf(processInstId).longValue());
            if(is == null) {
                return null;
            } else {
                ArrayList list = new ArrayList(0);
                list.add(is);
                List convert2ProcessInstList = DataConvertor.convert2ProcessInstList(list);
                if(convert2ProcessInstList != null && convert2ProcessInstList.size() != 0) {
                    ProcessInstance pi = (ProcessInstance)convert2ProcessInstList.get(0);
                    return pi;
                } else {
                    return null;
                }
            }
        } catch (WFServiceException var8) {
            throw new WFException(var8);
        } catch (ParseException var9) {
            throw new WFException(var9);
        }
    }

    public String getActivityExtendAttributes(String processModelName, String activityDefID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            PageCond pagec = new PageCond(2147483647);
            List listWpd = e.getDefinitionQueryManager().queryProcessesByName(processModelName, "published", pagec);
            if(listWpd != null && listWpd.size() != 0) {
                WFProcessDefine wfd1 = (WFProcessDefine)listWpd.get(0);
                String extendAttributes = e.getDefinitionQueryManager().getExtendAttribute(wfd1.getProcessDefID().longValue(), activityDefID);
                return extendAttributes;
            } else {
                throw new WFException("未找到已发布的流程定义" + processModelName + "!");
            }
        } catch (WFServiceException var8) {
            throw new WFException(var8);
        }
    }

    public List<TaskInstance> getMySuspendTasks(TaskFilter arg0) throws WFException {
        throw new WFException("暂不提供getMySuspendTasks查询挂起，可通过查询待办查询出挂起");
    }

    public List<TaskInstance> getNextTaskInstByFinTaskInstID(String taskInstId) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            IWFWorkItemManager wim = e.getWorkItemManager();
            List list = wim.queryNextWorkItemsByWorkItemID(Long.valueOf(taskInstId).longValue(), true);
            List listT = DataConvertor.convert2TaskInstList(list);
            return listT;
        } catch (WFServiceException var6) {
            throw new WFException(var6);
        } catch (NumberFormatException var7) {
            throw new WFException(var7);
        }
    }

    public String getExtendAttribute(String processDefName) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            IWFDefinitionQueryManager iqm = e.getDefinitionQueryManager();
            List list = iqm.queryProcessesByName(processDefName);
            if(list != null && list.size() != 0) {
                long processDefID = ((WFProcessDefine)list.get(0)).getProcessDefID().longValue();
                return iqm.getExtendAttribute(processDefID, (String)null);
            } else {
                throw new WFException(processDefName + "没有找到");
            }
        } catch (WFServiceException var7) {
            throw new WFException(var7);
        } catch (NumberFormatException var8) {
            throw new WFException(var8);
        }
    }

    public List<Participant> getProbableParticipants(String processInstID, String currentActivityInstID, String activityID, LinkedHashMap<String, Object> parameters) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            List Participants = e.getProcessInstManager().getProbableParticipants(Long.parseLong(processInstID), activityID);
            List ProbableParticipants = DataConvertor.convert2PartticipantList(Participants);
            return ProbableParticipants;
        } catch (WFServiceException var8) {
            throw new WFException(var8);
        }
    }

    public String getProcessExtendAttributes(String arg0) throws WFException {
        if(arg0 != null) {
            throw new WFException("该方法是否实现未确认");
        } else {
            return null;
        }
    }

    public List<ProcessInstance> getProcessInstanceList(ProcessInstanceFilter pif) throws WFException {
        String appid = pif.getAppId();
        Date beginStartDate = pif.getBeginStartDate();
        Date beginEndDate = pif.getBeginEndDate();
        Date endStartDate = pif.getEndStartDate();
        Date endEndDate = pif.getEndEndDate();
        PageCondition pageCondition = pif.getPageCondition();
        String processInstStatus = pif.getProcessInstStatus();
        String processModelID = pif.getProcessModelID();
        String processModelName = pif.getProcessModelName();
        String startAccountID = pif.getStartAccountID();
        if(appid != null && appid.length() != 0) {
            throw new WFException("getProcessInstanceList查询无需传入APPID");
        } else {
            appid = this.appID;
            if((beginStartDate != null || endStartDate == null) && (beginStartDate == null || endStartDate != null)) {
                if((beginEndDate != null || endEndDate == null) && (beginEndDate == null || endEndDate != null)) {
                    IDASCriteria criteria = DASManager.createCriteria("com.eos.workflow.data.WFProcessInst");
                    criteria.add(ExpressionHelper.eq("appid", appid));
                    if(beginStartDate != null && endStartDate != null) {
                        criteria.add(ExpressionHelper.between("createTime", beginStartDate, endStartDate));
                    }

                    if(beginEndDate != null && endEndDate != null) {
                        criteria.add(ExpressionHelper.between("finalTime", beginEndDate, endEndDate));
                    }

                    if(processInstStatus != null && processInstStatus.length() != 0) {
                        Integer cond = Integer.valueOf(processInstStatus);
                        boolean primetonState = true;
                        byte primetonState1;
                        switch(cond.intValue()) {
                            case 1:
                                primetonState1 = 2;
                                break;
                            case 2:
                            default:
                                throw new WFException("无法识别的流程实例状态：" + processInstStatus);
                            case 3:
                                primetonState1 = 3;
                                break;
                            case 4:
                                primetonState1 = 7;
                                break;
                            case 5:
                                primetonState1 = 8;
                        }

                        criteria.add(ExpressionHelper.eq("currentState", Integer.valueOf(primetonState1)));
                    }

                    if(processModelName != null && processModelName.length() != 0) {
                        if(processModelName.indexOf(",") > 0) {
                            String[] cond1 = processModelName.split(",");
                            criteria.add(ExpressionHelper.in("processDefName", cond1));
                        } else {
                            criteria.add(ExpressionHelper.eq("processDefName", processModelName));
                        }
                    }

                    if(processModelID != null && processModelID.length() != 0) {
                        criteria.add(ExpressionHelper.eq("processDefID", processModelID));
                    }

                    if(startAccountID != null && startAccountID.length() != 0) {
                        criteria.add(ExpressionHelper.eq("creator", startAccountID));
                    }

                    PageCond cond2 = pageCondition == null?null:DataConvertor.convert2PageCond(pageCondition);
                    criteria.desc("processInstID");
                    return this.queryProcessInstancesCriteria(criteria, cond2, pif);
                } else {
                    throw new WFException("期望完成时间必须成对出现，不支持单一条件查询");
                }
            } else {
                throw new WFException("创建时间必须成对出现，不支持单一条件查询");
            }
        }
    }

    private List<ProcessInstance> queryProcessInstancesCriteria(IDASCriteria criteria, PageCond cond, ProcessInstanceFilter pif) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            List list = e.getCommonQueryManage().queryProcessInstancesCriteria(criteria, cond);
            ResultList rl = (ResultList)list;
            PageCond pageCond = rl.getPageCond();
            PageCondition pageCondition = pif.getPageCondition();
            if(pageCondition != null) {
                DataConvertor.convert2PageCond(pageCond, pageCondition);
            }

            return DataConvertor.convert2ProcessInstList(list);
        } catch (WFServiceException var9) {
            throw new WFException(var9);
        } catch (ParseException var10) {
            throw new WFException(var10);
        }
    }

    public void init(Map<String, String> m) throws WFException {
        throw new WFException("该方法是否实现未确认");
    }

    public String addAndStartProcessWithParentActivityInstID(String processDefName, String processInstName, String parentProcessInstID, String actInstID, LinkedHashMap<String, Object> params) throws WFException {
        long id = 0L;

        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            Object[] objParams = new Object[params == null?0:params.size()];
            if(params != null && params.size() > 0) {
                objParams = params.values().toArray(objParams);
            }

            id = e.getProcessInstManager().addAndStartProcessWithParentActivityInstID(processDefName, processInstName, (String)null, Long.parseLong(parentProcessInstID), Long.parseLong(actInstID), false, objParams);
            return id + "";
        } catch (WFServiceException var10) {
            throw new WFException(var10);
        }
    }

    public ActivityInstance findActivityInstByActivityDefID(String processInstID, String activityDefID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            List activityInst = e.getActivityInstManager().queryActivityInstsByActivityID(Long.parseLong(processInstID), activityDefID, new PageCond(2147483647));
            if(activityInst != null && !activityInst.isEmpty()) {
                Iterator i$ = activityInst.iterator();

                WFActivityInst t;
                do {
                    if(!i$.hasNext()) {
                        return null;
                    }

                    t = (WFActivityInst)i$.next();
                } while(t.getCurrentState() != 2);

                ActivityInstance a = DataConvertor.convert2ActivityInst(t);
                return a;
            } else {
                return null;
            }
        } catch (WFServiceException var8) {
            throw new WFException(var8);
        } catch (ParseException var9) {
            throw new WFException(var9);
        }
    }

    public List<Participant> getProbableCCParticipants(String arg0, String arg1, String arg2, LinkedHashMap<String, Object> arg3) throws WFException {
        throw new WFException("该方法是讨论后，4A不提供抄送维度故不实现");
    }

    public void finishActivityInstance(String activityInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getActivityInstManager().finishActivityInstance(Long.parseLong(activityInstID));
        } catch (WFServiceException var3) {
            throw new WFException("完成活动实例失败", var3);
        }
    }

    public List<Participant> getActivityParticipants(String processModelName, String activityDefID, LinkedHashMap<String, Object> Parameters) throws WFException {
        Object parts = new ArrayList();
        ArrayList wfpars = new ArrayList();
        AAAAService a4Service = new AAAAService();

        try {
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();
            PageCond e = new PageCond(2147483647);
            List listWpd = client.getDefinitionQueryManager().queryProcessesByName(processModelName, "published", e);
            if(listWpd != null && listWpd.size() != 0) {
                WFProcessDefine wfd1 = (WFProcessDefine)listWpd.get(0);
                String extendAttributes = client.getDefinitionQueryManager().getExtendAttribute(wfd1.getProcessDefID().longValue(), activityDefID);
                if(extendAttributes == null) {
                    throw new WFException("未找到扩展属性!");
                } else {
                    Document document = DocumentHelper.parseText(extendAttributes);
                    Element root = document.getRootElement();
                    List jiedian = root.elements();
                    Element et = null;
                    String roleclass = "";

                    String productcode;
                    for(int params = 0; params < jiedian.size(); ++params) {
                        et = (Element)jiedian.get(params);
                        productcode = et.elementText("key");
                        if(productcode.equals("abstractRoleXml")) {
                            roleclass = et.elementText("value");
                        }
                    }

                    HashMap var31 = new HashMap();
                    productcode = Parameters.get("productcode") + "";
                    String areacode = Parameters.get("areacode") + "";
                    String majorcode = Parameters.get("majorcode") + "";
                    String orgcode = Parameters.get("orgcode") + "";
//                    HashSet roleEntities;
                    String[] i$;
                    String[] entity;
                    int wfpar;
                    int i$1;
                    String r;
                    if(productcode != null && !"".equals(productcode) && !"null".equals(productcode)) {
//                        roleEntities = new HashSet();
                        i$ = productcode.split(",");
                        if(i$.length == 1) {
//                            roleEntities.add(productcode);
                        } else {
                            entity = i$;
                            wfpar = i$.length;

                            for(i$1 = 0; i$1 < wfpar; ++i$1) {
                                r = entity[i$1];
//                                roleEntities.add(r);
                            }
                        }

//                        if(roleEntities != null) {
//                            var31.put("PRODUCT_ID", roleEntities);
//                        }
                    }

                    if(areacode != null && !"".equals(areacode) && !"null".equals(areacode)) {
//                        roleEntities = new HashSet();
                        i$ = areacode.split(",");
                        if(i$.length == 1) {
//                            roleEntities.add(areacode);
                        } else {
                            entity = i$;
                            wfpar = i$.length;

                            for(i$1 = 0; i$1 < wfpar; ++i$1) {
                                r = entity[i$1];
//                                roleEntities.add(r);
                            }
                        }

//                        if(roleEntities != null) {
//                            var31.put("AREA_ID", roleEntities);
//                        }
                    }

                    if(majorcode != null && !"".equals(majorcode) && !"null".equals(majorcode)) {
//                        roleEntities = new HashSet();
                        i$ = majorcode.split(",");
                        if(i$.length == 1) {
//                            roleEntities.add(majorcode);
                        } else {
                            entity = i$;
                            wfpar = i$.length;

                            for(i$1 = 0; i$1 < wfpar; ++i$1) {
                                r = entity[i$1];
//                                roleEntities.add(r);
                            }
                        }

//                        if(roleEntities != null) {
//                            var31.put("MAJOR_ID", roleEntities);
//                        }
                    }

                    if(orgcode != null && !"".equals(orgcode) && !"null".equals(orgcode)) {
//                        roleEntities = new HashSet();
                        i$ = orgcode.split(",");
                        if(i$.length == 1) {
//                            roleEntities.add(orgcode);
                        } else {
                            entity = i$;
                            wfpar = i$.length;

                            for(i$1 = 0; i$1 < wfpar; ++i$1) {
                                r = entity[i$1];
//                                roleEntities.add(r);
                            }
                        }

//                        if(roleEntities != null) {
//                            var31.put("ORG_ID", roleEntities);
//                        }
                    }

                    if(roleclass != null && !"".equals(roleclass) && !"null".equals(roleclass)) {
//                        roleEntities = new HashSet();
                        i$ = roleclass.split(",");
                        if(i$.length == 1) {
//                            roleEntities.add(roleclass);
                        } else {
                            entity = i$;
                            wfpar = i$.length;

                            for(i$1 = 0; i$1 < wfpar; ++i$1) {
                                r = entity[i$1];
//                                roleEntities.add(r);
                            }
                        }

//                        if(roleEntities != null) {
//                            var31.put("ABSTRACT_ROLE_ID", roleEntities);
//                        }
                    }

//                    roleEntities = null;
//                    List var32 = a4Service.findRoleListByDimensions(var31);
//                    if(var32 == null) {
//                        return (List)parts;
//                    } else {
//                        for(Iterator var33 = var32.iterator(); var33.hasNext(); parts = DataConvertor.convert2PartticipantList(wfpars)) {
//                            RoleEntity var34 = (RoleEntity)var33.next();
//                            WFParticipant var35 = new WFParticipant();
//                            var35.setId(var34.getCloudRoleId() + "");
//                            var35.setName(var34.getRoleName());
//                            var35.setTypeCode("role");
//                            wfpars.add(var35);
//                        }
//
                        return (List)parts;
//                    }
                }
            } else {
                throw new WFException("未找到已发布的流程定义" + processModelName + "!");
            }
        } catch (WFServiceException var28) {
            throw new WFException(var28);
        } catch (DocumentException var29) {
            throw new WFException(var29);
        }
//        catch (PaasAAAAException var30) {
//            throw new WFException(var30);
//        }
    }

    public int getProcessInstanceCount(ProcessInstanceFilter pif) throws WFException {
        PageCondition pc = new PageCondition();
        pc.setLength(1);
        pc.setBegin(0);
        pc.setIsCount(Boolean.valueOf(true));
        pif.setPageCondition(pc);
        this.getProcessInstanceList(pif);
        int count = pc.getCount();
        return count;
    }

    public List<Participant> findDoingParticipant(String processInstID) throws WFException {
        ArrayList allusers = new ArrayList();
        ArrayList allParticipants = new ArrayList();

        try {
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();
            IWFQueryManager e = client.getCommonQueryManage();
            IDASCriteria criteria = DASManager.createCriteria("com.eos.workflow.data.WFActivityInst");
            criteria.add(ExpressionHelper.eq("processInstID", processInstID));
            criteria.add(ExpressionHelper.eq("currentState", Integer.valueOf(2)));
            PageCond pageCond = new PageCond(2147483647);
            List queryActivityIns = e.queryActivityInstancesCriteria(criteria, pageCond);
            if(queryActivityIns != null) {
                ArrayList nameSet = new ArrayList();

                List partid;
                String var36;
                for(int it = 0; it < queryActivityIns.size(); ++it) {
                    String l = ((WFActivityInst)queryActivityIns.get(it)).getActivityType();
                    Long partictype = Long.valueOf(((WFActivityInst)queryActivityIns.get(it)).getActivityInstID());
                    if(l.equals("manual")) {
                        partid = client.getWorkItemManager().queryWorkItemsByActivityInstID(partictype.longValue(), pageCond);

                        for(int partname = 0; partname < partid.size(); ++partname) {
                            Long parttype = Long.valueOf(((WFWorkItem)partid.get(partname)).getWorkItemID());
                            List participant = client.getWorkItemManager().queryWorkItemParticipantInfo(parttype.longValue());

                            for(int partid1 = 0; partid1 < participant.size(); ++partid1) {
                                WIParticipantInfo partname1 = (WIParticipantInfo)participant.get(partid1);
                                WFParticipant participant1 = new WFParticipant();
                                participant1.setId(partname1.getId());
                                participant1.setName(partname1.getName());
                                participant1.setTypeCode(partname1.getTypeCode());
                                Participant Participants = DataConvertor.convert2Participant(participant1);
                                nameSet.add(Participants);
                            }
                        }
                    } else if(l.equals("subflow")) {
                        long[] var28 = client.getProcessInstManager().querySubProcessInstIDsByActivityInstID(partictype.longValue());
                        ArrayList var31 = new ArrayList();

                        int var33;
                        for(var33 = 0; var33 < var28.length; ++var33) {
                            var36 = Long.toString(var28[var33]);
                            List var42 = this.findDoingParticipant(var36);

                            for(int var44 = 0; var44 < var42.size(); ++var44) {
                                var31.add(var42.get(var44));
                            }
                        }

                        for(var33 = 0; var33 < var31.size(); ++var33) {
                            nameSet.add(var31.get(var33));
                        }
                    }
                }

                AAAAService var24 = new AAAAService();

//                for(int var26 = 0; var26 < nameSet.size(); ++var26) {
//                    String var27 = ((Participant)nameSet.get(var26)).getParticipantType();
//                    String var43;
//                    if(var27.equals("3")) {
//                        partid = var24.findUserListByOrgID(Integer.parseInt(((Participant)nameSet.get(var26)).getParticipantID()));
//                        if(partid != null) {
//                            Iterator var32 = partid.iterator();
//
//                            while(var32.hasNext()) {
//                                UserEntity var37 = (UserEntity)var32.next();
//                                var36 = var37.getPortalUserCode();
//                                var43 = var37.getEmpName();
//                                Participant var45 = new Participant();
//                                var45.setParticipantID(var36);
//                                var45.setParticipantName(var43);
//                                var45.setParticipantType("1");
//                                allParticipants.add(var45);
//                            }
//                        }
//                    } else if(var27.equals("2")) {
//                        Integer var29 = Integer.valueOf(Integer.parseInt(((Participant)nameSet.get(var26)).getParticipantID()));
//                        List var34 = var24.findAccountListByRoleID(var29.intValue());
//                        if(var34 != null) {
//                            Iterator var38 = var34.iterator();
//
//                            while(var38.hasNext()) {
//                                AccountEntity var39 = (AccountEntity)var38.next();
//                                var43 = var39.getAccountId();
//                                String var46 = var39.getDisplay();
//                                Participant var47 = new Participant();
//                                var47.setParticipantID(var43);
//                                var47.setParticipantName(var46);
//                                var47.setParticipantType("1");
//                                allParticipants.add(var47);
//                            }
//                        }
//                    } else if(var27.equals("1")) {
//                        String var30 = ((Participant)nameSet.get(var26)).getParticipantID();
//                        String var35 = ((Participant)nameSet.get(var26)).getParticipantName();
//                        String var40 = ((Participant)nameSet.get(var26)).getParticipantType();
//                        Participant var41 = new Participant();
//                        var41.setParticipantID(var30);
//                        var41.setParticipantName(var35);
//                        var41.setParticipantType(var40);
//                        allParticipants.add(var41);
//                    }
//                }
            }

            HashSet var23 = new HashSet();
            var23.addAll(allParticipants);
            Iterator var25 = var23.iterator();

            while(var25.hasNext()) {
                allusers.add(var25.next());
            }

            return allusers;
        } catch (WFServiceException var21) {
            throw new WFException(var21);
        }
//        catch (PaasAAAAException var22) {
//            throw new WFException("根据角色ID查询帐号信息失败", var22);
//        }
    }

    public void clearAppointedActivityParticipants(long workItemID, String activityDefID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getAppointActivityManager().clearAppointedActivityParticipants(workItemID, activityDefID);
        } catch (WFServiceException var5) {
            throw new WFException("\t删除某个后续活动已被指定参与者workItemID:" + workItemID + "失败", var5);
        }
    }

    public void drawbackWorkItem(long workItemID, boolean isRestartAllWI, boolean isCallRollbackEvent) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getWorkItemDrawbackManager().drawbackWorkItem(workItemID, isRestartAllWI, isCallRollbackEvent);
        } catch (WFServiceException var6) {
            throw new WFException("\t找回工作项workItemID:" + workItemID + "失败", var6);
        }
    }

    public boolean isDrawbackEnable(long workItemID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            return e.getWorkItemDrawbackManager().isDrawbackEnable(workItemID);
        } catch (WFServiceException var4) {
            throw new WFException("\t判断工作项是否可以拽回workItemID:" + workItemID + "失败", var4);
        }
    }

    public List<ActivityDef> queryNextActivities(String processModelName, String activityDefID) throws WFException {
        try {
            ArrayList e = new ArrayList();
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();
            List processes = client.getDefinitionQueryManager().queryProcessesByName(processModelName);

            for(int j = 0; j < processes.size(); ++j) {
                Long processDefID = ((WFProcessDefine)processes.get(j)).getProcessDefID();
                List wfactivityDefs = client.getDefinitionQueryManager().queryNextActivities(processDefID.longValue(), activityDefID);

                for(int i = 0; i < wfactivityDefs.size(); ++i) {
                    ActivityDef activityDef = DataConvertor.convert2WFActivityDefine((WFActivityDefine)wfactivityDefs.get(i));
                    e.add(activityDef);
                }
            }

            return e;
        } catch (WFServiceException var11) {
            throw new WFException("根据流程模板名称和活动定义ID查询后续活动失败", var11);
        }
    }

    public void addWorkItemParticipant(long workItemID, List<Participant> participant) throws WFException {
        try {
            List e = DataConvertor.convert2WFParticipantList(participant);

            for(int i = 0; i < e.size(); ++i) {
                IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();
                client.getWorkItemManager().addWorkItemParticipant(workItemID, (WFParticipant)e.get(i));
            }

        } catch (WFServiceException var7) {
            throw new WFException(var7);
        }
    }

    public List<TaskInstance> getMyCompletedTasksDistinctJobId(TaskFilter tf) throws WFException {
        ArrayList tl = new ArrayList();

        try {
            if(tf == null) {
                throw new WFException("查询条件不能为空");
            } else {
                HashMap e = new HashMap();
                String appid = null;
                if(this.isMultiTenantMode) {
                    appid = this.appID;
                }

                e.put("appid", appid);
                String userid = this.accountID;
                String tfpmname = tf.getProcessModelName();
                String processmodelname = null;
                if(tfpmname != null) {
                    processmodelname = "";
                    if(tfpmname.indexOf(",") != -1) {
                        String[] searchMap = tfpmname.split(",");

                        for(int jobStarttimeStrart = 0; jobStarttimeStrart < searchMap.length - 1; ++jobStarttimeStrart) {
                            String jobStarttimeEnd = "\'" + searchMap[jobStarttimeStrart] + "\',";
                            processmodelname = processmodelname + jobStarttimeEnd;
                        }

                        processmodelname = processmodelname + "\'" + searchMap[searchMap.length - 1] + "\'";
                    } else {
                        processmodelname = "\'" + tfpmname + "\'";
                    }
                }

                e.put("deilman", userid);
                e.put("processmodelname", processmodelname);
                System.out.println("userid---------" + userid);
                System.out.println("appid---------" + appid);
                e.put("jobid", tf.getJobID());
                e.put("jobcode", tf.getJobCode());
                e.put("jobtitle", tf.getJobTitle());
                e.put("productcode", tf.getProductID());
                e.put("activitydefid", "activitydefid");
                e.put("strcolumn1", tf.getStrColumn1());
                e.put("strcolumn2", tf.getStrColumn2());
                e.put("strcolumn3", tf.getStrColumn3());
                e.put("datstarttime1", tf.getDatColumn1StartTime());
                e.put("datendtime1", tf.getDatColumn1EndTime());
                e.put("datstarttime2", tf.getDatColumn2StartTime());
                e.put("datendtime2", tf.getDatColumn2EndTime());
                e.put("rootvccolumn1", tf.getRootvcColumn1());
                e.put("rootvccolumn2", tf.getRootvcColumn2());
                e.put("rootnmcolumn2", Integer.valueOf(tf.getRootnmColumn2()));
                e.put("jobtype", tf.getJobType());
                Map var23 = tf.getSearchMap();
                new Date();
                new Date();
                String businessId = "";
                if(null != var23) {
                    Set sortMap = var23.keySet();
                    Iterator pageCond = sortMap.iterator();

                    while(pageCond.hasNext()) {
                        String datas = (String)pageCond.next();
                        e.put(datas, var23.get(datas));
                    }
                }

                Map var24 = tf.getSortMap();
                if(null != var24) {
                    Set var25 = var24.keySet();
                    Iterator var27 = var25.iterator();
                    if(var27.hasNext()) {
                        e.put("sortString", var27.next());
                    }
                }

                DataObject var26 = DataFactory.INSTANCE.create("com.eos.foundation", "PageCond");
                if(tf.getPageCondition() == null) {
                    throw new WFException("分页条件不能为空");
                } else {
                    var26.set("begin", Integer.valueOf(tf.getPageCondition().getBegin()));
                    var26.set("length", Integer.valueOf(tf.getPageCondition().getLength()));
                    var26.set("isCount", tf.getPageCondition().getIsCount());
                    DataObject[] var28 = ((IBPSServiceManagerExt)BPSServiceClientFactoryExt.getDefaultClient().getService(IBPSServiceManagerExt.class)).getnamingsql(e, var26);
                    if(var28 != null && var28.length >= 1) {
                        DataObject pc = var28[0];
                        PageCond pcNew = new PageCond();
                        pcNew.setBegin(pc.getInt("begin"));
                        pcNew.setLength(pc.getInt("length"));
                        pcNew.setIsCount(pc.getBoolean("isCount"));
                        pcNew.setCount(pc.getInt("count"));
                        pcNew.setCurrentPage(pc.getInt("currentPage"));
                        pcNew.setFirst(pc.getBoolean("isFirst"));
                        pcNew.setLast(pc.getBoolean("isLast"));
                        pcNew.setTotalPage(pc.getInt("totalPage"));
                        PageCondition pcOld = null;
                        if(tf != null) {
                            pcOld = tf.getPageCondition();
                        }

                        if(pcOld != null) {
                            DataConvertor.convert2PageCond(pcNew, pcOld);
                        }

                        for(int i = 1; i < var28.length; ++i) {
                            DataObject d = var28[i];
                            TaskInstance taskInsts = new TaskInstance();
                            taskInsts.setTaskInstID(d.getString("taskInstID"));
                            taskInsts.setProcessModelName(d.getString("ProcessModelName"));
                            taskInsts.setActivityDefID(d.getString("activitydefid"));
                            taskInsts.setActivityInstName(d.getString("activityInstName"));
                            taskInsts.setCurrentState("2");
                            taskInsts.setRootProcessInstId(d.getString("rootProcessInstId"));
                            if(d.getDate("completionDate") != null) {
                                taskInsts.setCompletionDate(new Date(d.getDate("completionDate").getTime()));
                            }

                            if(d.getDate("createTime") != null) {
                                taskInsts.setCreateDate(new Date(d.getDate("createTime").getTime()));
                            }

                            taskInsts.setShard(d.getString("shard"));
                            taskInsts.setBusinessId(d.getString("businessId"));
                            taskInsts.setPRODUCT_ID(d.getString("productcode"));
                            taskInsts.setMAJOR_ID(d.getString("majorcode"));
                            taskInsts.setJobID(d.getString("jobid"));
                            taskInsts.setJobCode(d.getString("jobcode"));
                            taskInsts.setJobTitle(d.getString("jobtitle"));
                            taskInsts.setStrColumn1(d.getString("strcolumn1"));
                            taskInsts.setStrColumn2(d.getString("strcolumn2"));
                            taskInsts.setStrColumn3(d.getString("strcolumn3"));
                            if(null != d.getDate("jobstarttime")) {
                                taskInsts.setJobStarttime(new Date(d.getDate("jobstarttime").getTime()));
                            }

                            taskInsts.setRootvcColumn1(d.getString("rootvccolumn1"));
                            taskInsts.setRootvcColumn2(d.getString("rootvccolumn2"));
                            taskInsts.setRootnmColumn2(d.getInt("rootnmcolumn2"));
                            taskInsts.setJobtype(d.getString("jobtype"));
                            if(null != d.getDate("datcolumn1")) {
                                taskInsts.setDatColumn2(new Date(d.getDate("datcolumn1").getTime()));
                            }

                            if(null != d.getDate("datcolumn2")) {
                                taskInsts.setDatColumn2(new Date(d.getDate("datcolumn2").getTime()));
                            }

                            tl.add(taskInsts);
                        }

                        return tl;
                    } else {
                        return null;
                    }
                }
            }
        } catch (WFServiceException var21) {
            throw new WFException(var21);
        } catch (Exception var22) {
            throw new WFException(var22);
        }
    }

    public List<Participant> getParentWorkflowNextParticipant(String processInstID, String activityID) throws WFException {
        ArrayList ParentProParts = new ArrayList();

        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            WFProcessInst ProcessInst = e.getProcessInstManager().queryProcessInstDetail(Long.parseLong(processInstID));
            long ParentProcID = ProcessInst.getParentProcID();
            long ParentActID = ProcessInst.getParentActID();
            WFProcessInst Parentproinst = e.getProcessInstManager().queryProcessInstDetail(ParentProcID);
            long ParentProcessDefID = Parentproinst.getProcessDefID();
            WFActivityInst ParentAct = e.getActivityInstManager().findActivityInstByActivityInstID(ParentActID);
            String ParentActDefID = ParentAct.getActivityDefID();
            List ParentProActs = e.getDefinitionQueryManager().queryNextActivities(ParentProcessDefID, ParentActDefID);
            if(ParentProActs != null && ParentProActs.size() >= 1) {
                for(int i = 0; i < ParentProActs.size(); ++i) {
                    if(!((WFActivityDefine)ParentProActs.get(i)).getType().equals("manual")) {
                        throw new WFException("后续活动为非人工活动!");
                    }

                    String ParentProActId = ((WFActivityDefine)ParentProActs.get(i)).getId();
                    List ParentWFParts = e.getProcessInstManager().getProbableParticipants(ParentProcID, ParentProActId);
                    List ParentProPart = DataConvertor.convert2PartticipantList(ParentWFParts);
                    ParentProParts.addAll(ParentProPart);
                }

                return ParentProParts;
            } else {
                throw new WFException("没有后继活动");
            }
        } catch (WFServiceException var20) {
            throw new WFException(var20);
        }
    }

    public void updateJobTitleInfo(String processInstID, String jobTilt) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            HashMap map = new HashMap();
            map.put("jobTitle", jobTilt);
            String rootProcInstID = FindWorkItem.getRootProcInstID(processInstID);
            if(rootProcInstID != null && rootProcInstID.length() != 0) {
                e.getCommonManager().updateBizInfo(Long.parseLong(rootProcInstID), map);
                BPSServiceClientFactory.getLoginManager().setCurrentUser(this.accountID, this.accountID, this.appID, (String)null);
                IBPSServiceManagerExt service = (IBPSServiceManagerExt)BPSServiceClientFactoryExt.getDefaultClient().getService(IBPSServiceManagerExt.class);
                WFBusiinfo data = (WFBusiinfo)WFBusiinfo.FACTORY.create();
                data.setJobtitle(jobTilt);
                WFBusiinfo temp = (WFBusiinfo)WFBusiinfo.FACTORY.create();
                temp.setProcessinstid(Long.parseLong(processInstID));
                service.updateEntityByTemplate(data, temp);
            }
        } catch (WFServiceException var9) {
            throw new WFException("修改流程" + processInstID + "流程的工单标题失败", var9);
        }
    }

    public void updateJobCodeInfo(String processInstID, String jobID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            HashMap map = new HashMap();
            map.put("jobCode", jobID);
            String rootProcInstID = FindWorkItem.getRootProcInstID(processInstID);
            if(rootProcInstID != null && rootProcInstID.length() != 0) {
                e.getCommonManager().updateBizInfo(Long.parseLong(rootProcInstID), map);
                BPSServiceClientFactory.getLoginManager().setCurrentUser(this.accountID, this.accountID, this.appID, (String)null);
                IBPSServiceManagerExt service = (IBPSServiceManagerExt)BPSServiceClientFactoryExt.getDefaultClient().getService(IBPSServiceManagerExt.class);
                WFBusiinfo data = (WFBusiinfo)WFBusiinfo.FACTORY.create();
                data.setJobcode(jobID);
                WFBusiinfo temp = (WFBusiinfo)WFBusiinfo.FACTORY.create();
                temp.setProcessinstid(Long.parseLong(processInstID));
                service.updateEntityByTemplate(data, temp);
            }
        } catch (WFServiceException var9) {
            throw new WFException("修改流程" + processInstID + "流程的工单CODE失败", var9);
        }
    }

    protected static void println(Object o) {
        logger.debug(o.toString());
    }

    public List<TaskInstance> getCollectiveTask(List<Participant> pl, String processDefName, PageCondition pageCondition) throws WFException {
        ArrayList tl = new ArrayList();
        CriteriaType ct = (CriteriaType)CriteriaType.FACTORY.create();
        ct.set_entity("com.eos.workflow.api.ext.extdataset.WfWorkItemView");
        DataObject pageCond = DataFactory.INSTANCE.create("com.eos.foundation", "PageCond");
        pageCond.set("begin", Integer.valueOf(pageCondition.getBegin()));
        pageCond.set("length", Integer.valueOf(pageCondition.getLength()));
        pageCond.set("isCount", pageCondition.getIsCount());
        ArrayList el = new ArrayList();
        Object var19 = ct.get_expr() == null?el:ct.get_expr();
        if(this.isMultiTenantMode) {
            ExprType property = (ExprType)ExprType.FACTORY.create();
            property.set_opEnum(OP.EQ);
            property.set_value(this.appID);
            property.set_property("appid");
            ((List)var19).add(property);
        }

        String var20 = "processDefName".toLowerCase();
        ExprType e;
        if(processDefName != null && processDefName.length() != 0) {
            if(processDefName.indexOf(",") > 0) {
                e = (ExprType)ExprType.FACTORY.create();
                e.set_opEnum(OP.IN);
                e.set_value(processDefName);
                e.set_property(var20);
                ((List)var19).add(e);
            } else {
                e = (ExprType)ExprType.FACTORY.create();
                e.set_opEnum(OP.EQ);
                e.set_value(processDefName);
                e.set_property(var20);
                ((List)var19).add(e);
            }
        }

        e = (ExprType)ExprType.FACTORY.create();
        e = (ExprType)ExprType.FACTORY.create();
        e.set_opEnum(OP.EQ);
        e.set_value("12");
        e.set_property("currentstate");
        ((List)var19).add(e);
        e = (ExprType)ExprType.FACTORY.create();
        e.set_opEnum(OP.EQ);
        e.set_value("EXE");
        e.set_property("partiintype");
        ((List)var19).add(e);
        if(pl != null && !pl.isEmpty()) {
            List var22 = DataConvertor.convert2WFParticipantList(pl);
            ArrayList datas = new ArrayList();
            Iterator pc = var22.iterator();

            while(pc.hasNext()) {
                WFParticipant pcNew = (WFParticipant)pc.next();
                if(pcNew.getTypeCode().equals("person")) {
                    datas.add("P{" + pcNew.getId() + "}");
                } else {
                    if(!pcNew.getTypeCode().equals("role")) {
                        throw new WFException("暂不支持的组织参与者类型");
                    }

                    List i = this.getPersonByRole(pcNew.getId());
                    if(i != null) {
                        Iterator d = i.iterator();

                        while(d.hasNext()) {
                            WFParticipant wi = (WFParticipant)d.next();
                            datas.add("P{" + wi.getId() + "}");
                        }
                    }
                }
            }

            String var24 = "";

            String var29;
            for(Iterator var25 = datas.iterator(); var25.hasNext(); var24 = var24 + "," + var29) {
                var29 = (String)var25.next();
            }

            var24 = var24.substring(1);
            ExprType var27 = (ExprType)ExprType.FACTORY.create();
            var27.set_opEnum(OP.IN);
            var27.set_value(var24);
            var27.set_property("globalid");
            ((List)var19).add(var27);
        }

        ct.set_expr((List)var19);
        ct.set("_orderby[1]/_property", "endtime");
        ct.set("_orderby[1]/_sort", "desc");

        try {
            SelectType var23 = ct.get_select();
            if(var23 != null) {
                if(var23.get_avg() != null) {
                    throw new Exception("此API不支持avg函数");
                }

                if(var23.get_sum() != null) {
                    throw new Exception("此API不支持sum函数");
                }
            }

            BPSServiceClientFactory.getLoginManager().setCurrentUser(this.accountID, this.accountName, this.appID, (String)null);
            DataObject[] var21 = ((IBPSServiceManagerExt)BPSServiceClientFactoryExt.getDefaultClient().getService(IBPSServiceManagerExt.class)).queryEntitiesByCriteriaEntity(ct, pageCond);
            if(var21 == null) {
                return null;
            } else {
                DataObject var26 = var21[0];
                PageCond var28 = new PageCond();
                var28.setBegin(var26.getInt("begin"));
                var28.setLength(var26.getInt("length"));
                var28.setIsCount(var26.getBoolean("isCount"));
                var28.setCount(var26.getInt("count"));
                var28.setCurrentPage(var26.getInt("currentPage"));
                var28.setFirst(var26.getBoolean("isFirst"));
                var28.setLast(var26.getBoolean("isLast"));
                var28.setTotalPage(var26.getInt("totalPage"));
                DataConvertor.convert2PageCond(var28, pageCondition);

                for(int var30 = 1; var30 < var21.length; ++var30) {
                    DataObject var31 = var21[var30];
                    TaskInstance taskInsts;
                    WFWorkItem var32;
                    if(var23 == null) {
                        var32 = WfwaitView2WorkItem(var31);
                        taskInsts = DataConvertor.convert2TaskInstanceNoList(var32);
                        tl.add(taskInsts);
                    } else {
                        var32 = dataObject2WorkItem2(var31, var23);
                        taskInsts = DataConvertor.convert2TaskInstanceNoList(var32);
                        tl.add(taskInsts);
                    }
                }

                return tl;
            }
        } catch (WFServiceException var17) {
            throw new WFException("getCollectiveTask 处理" + pl + "失败", var17);
        } catch (Exception var18) {
            throw new WFException("getCollectiveTask 处理" + pl + "失败", var18);
        }
    }

    public List<WFParticipant> getPersonByRole(String roleId) {
        List list = null;

        try {
            IBPSServiceClient e1 = BPSServiceClientFactory.getDefaultClient();
            list = e1.getOMService().getAllChildParticipants("role", roleId);
        } catch (WFServiceException var4) {
            var4.printStackTrace();
        }

        return list;
    }

    public List<TaskInstance> getMyWaitingTasksDistinctJobId(JobFilter jf) throws WFException {
        ArrayList tl = new ArrayList();
        CriteriaType ct = (CriteriaType)CriteriaType.FACTORY.create();

        try {
            if(jf == null) {
                throw new WFException("JobFilter不能为空");
            } else {
                Object e = jf.getObject();
                PageCondition pageCondition = jf.getPageCondition();
                if(pageCondition == null) {
                    throw new WFException("JobFilter的分页条件不能为空");
                } else if(e == null) {
                    throw new WFException("JobFilter的查询条件不能为空");
                } else if(!(e instanceof CriteriaType)) {
                    throw new WFException("JobFilter的查询条件必须是" + CriteriaType.class.getName());
                } else {
                    ct = (CriteriaType)e;
                    ct.set_entity("com.eos.workflow.api.ext.extdataset.WfwaitView");
                    DataObject pageCond = DataFactory.INSTANCE.create("com.eos.foundation", "PageCond");
                    pageCond.set("begin", Integer.valueOf(pageCondition.getBegin()));
                    pageCond.set("length", Integer.valueOf(pageCondition.getLength()));
                    pageCond.set("isCount", pageCondition.getIsCount());
                    IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();
                    List list = client.getOMService().getParticipantScope("person", this.accountID);
                    String roles = null;
                    Iterator ll;
                    if(list != null && !list.isEmpty()) {
                        StringBuffer el = new StringBuffer();
                        ll = list.iterator();

                        while(ll.hasNext()) {
                            WFParticipant and = (WFParticipant)ll.next();
                            el.append(",");
                            el.append(and.getId());
                        }

                        roles = el.toString();
                        roles = roles.substring(1);
                    }

                    ArrayList var26 = new ArrayList();
                    Object var27 = ct.get_expr() == null?var26:ct.get_expr();
                    ll = null;
                    if(this.isMultiTenantMode) {
                        ExprType var28 = (ExprType)ExprType.FACTORY.create();
                        var28.set_opEnum(OP.EQ);
                        var28.set_value(this.appID);
                        var28.set_property("appid");
                        ((List)var27).add(var28);
                    }

                    List var29 = ct.get_and();
                    int index = 1;
                    if(var29 != null && !var29.isEmpty()) {
                        index = var29.size() + 1;
                    }

                    ct.set("_and[" + index + "]/_or[1]/_and[1]/_expr[1]/participanttype", "person");
                    ct.set("_and[" + index + "]/_or[1]/_and[1]/_expr[1]/_op", "=");
                    ct.set("_and[" + index + "]/_or[1]/_and[1]/_expr[2]/participant", this.accountID);
                    ct.set("_and[" + index + "]/_or[1]/_and[1]/_expr[2]/_op", "=");
                    if(roles != null) {
                        ct.set("_and[" + index + "]/_or[1]/_and[2]/_expr[1]/participanttype", "role");
                        ct.set("_and[" + index + "]/_or[1]/_and[2]/_expr[1]/_op", "=");
                        ct.set("_and[" + index + "]/_or[1]/_and[2]/_expr[2]/participant", roles);
                        ct.set("_and[" + index + "]/_or[1]/_and[2]/_expr[2]/_op", "in");
                    }

                    ct.set_expr((List)var27);
                    SelectType sel = ct.get_select();
                    if(sel != null) {
                        if(sel.get_avg() != null) {
                            throw new Exception("此API不支持avg函数");
                        }

                        if(sel.get_sum() != null) {
                            throw new Exception("此API不支持sum函数");
                        }
                    }

                    List ob = ct.get_orderby();
                    if(ob != null) {
                        Iterator datas = ob.iterator();

                        while(datas.hasNext()) {
                            OrderbyType pc = (OrderbyType)datas.next();
                            String pcNew = pc.get_property();
                            if(sel != null && sel.get_count() != null && sel.get_count().contains(pcNew)) {
                                pc.set_property("count_" + pcNew);
                            }

                            if(sel != null && sel.get_min() != null && sel.get_min().contains(pcNew)) {
                                pc.set_property("min_" + pcNew);
                            }

                            if(sel != null && sel.get_max() != null && sel.get_max().contains(pcNew)) {
                                pc.set_property("max_" + pcNew);
                            }
                        }
                    }

                    BPSServiceClientFactory.getLoginManager().setCurrentUser(this.accountID, this.accountName, this.appID, (String)null);
                    DataObject[] var30 = ((IBPSServiceManagerExt)BPSServiceClientFactoryExt.getDefaultClient().getService(IBPSServiceManagerExt.class)).queryEntitiesByCriteriaEntity(ct, pageCond);
                    if(var30 == null) {
                        return null;
                    } else {
                        DataObject var31 = var30[0];
                        PageCond var32 = new PageCond();
                        var32.setBegin(var31.getInt("begin"));
                        var32.setLength(var31.getInt("length"));
                        var32.setIsCount(var31.getBoolean("isCount"));
                        var32.setCount(var31.getInt("count"));
                        var32.setCurrentPage(var31.getInt("currentPage"));
                        var32.setFirst(var31.getBoolean("isFirst"));
                        var32.setLast(var31.getBoolean("isLast"));
                        var32.setTotalPage(var31.getInt("totalPage"));
                        DataConvertor.convert2PageCond(var32, pageCondition);

                        for(int i = 1; i < var30.length; ++i) {
                            DataObject d = var30[i];
                            WFWorkItem wi;
                            TaskInstance taskInsts;
                            if(sel == null) {
                                wi = WfwaitView2WorkItem(d);
                                taskInsts = DataConvertor.convert2TaskInstance(wi);
                                tl.add(taskInsts);
                            } else {
                                wi = dataObject2WorkItem2(d, sel);
                                taskInsts = DataConvertor.convert2TaskInstance(wi);
                                tl.add(taskInsts);
                            }
                        }

                        return tl;
                    }
                }
            }
        } catch (WFException var23) {
            throw new WFException("getMyWaitingTasksDistinctJobId 处理" + jf + "失败", var23);
        } catch (WFServiceException var24) {
            throw new WFException("getMyWaitingTasksDistinctJobId 处理" + jf + "失败", var24);
        } catch (Exception var25) {
            throw new WFException("getMyWaitingTasksDistinctJobId 处理" + jf + "失败", var25);
        }
    }

    protected static WFWorkItem dataObject2WorkItem2(DataObject w, SelectType selType) {
        List sel_min = selType.get_min();
        List sel_max = selType.get_max();
        List sel_count = selType.get_count();
        List sel = selType.get_field();
        WFWorkItem wi = new WFWorkItem();
        HashMap bizMap = new HashMap();
        HashMap mapStingKeyList = new HashMap();
        HashMap mapIntKeyList = new HashMap();
        HashMap mapDateKeyList = new HashMap();
        mapStingKeyList.put("jobID".toLowerCase(), "jobID");
        mapStingKeyList.put("jobCode".toLowerCase(), "jobCode");
        mapStingKeyList.put("jobTitle".toLowerCase(), "jobTitle");
        mapStingKeyList.put("jobType".toLowerCase(), "jobType");
        mapDateKeyList.put("jobEndtime".toLowerCase(), "jobEndtime");
        mapDateKeyList.put("jobStarttime".toLowerCase(), "jobStarttime");
        mapDateKeyList.put("reBacktime".toLowerCase(), "reBacktime");
        mapStingKeyList.put("shard".toLowerCase(), "shard");
        mapStingKeyList.put("businessId".toLowerCase(), "businessId");
        mapStingKeyList.put("productcode".toLowerCase(), "productcode");
        mapStingKeyList.put("majorcode".toLowerCase(), "majorcode");
        mapDateKeyList.put("datColumn1".toLowerCase(), "datColumn1");
        mapDateKeyList.put("datColumn2".toLowerCase(), "datColumn2");
        mapIntKeyList.put("numColumn1".toLowerCase(), "numColumn1");
        mapIntKeyList.put("numColumn2".toLowerCase(), "numColumn2");
        mapIntKeyList.put("rootnmColumn1".toLowerCase(), "rootnmColumn1");
        mapIntKeyList.put("rootnmColumn2".toLowerCase(), "rootnmColumn2");
        mapStingKeyList.put("rootvcColumn1".toLowerCase(), "rootvcColumn1");
        mapStingKeyList.put("rootvcColumn2".toLowerCase(), "rootvcColumn2");
        mapStingKeyList.put("strColumn1".toLowerCase(), "strColumn1");
        mapStingKeyList.put("strColumn2".toLowerCase(), "strColumn2");
        mapStingKeyList.put("strColumn3".toLowerCase(), "strColumn3");
        mapStingKeyList.put("strColumn4".toLowerCase(), "strColumn4");
        mapStingKeyList.put("strColumn5".toLowerCase(), "strColumn5");
        mapStingKeyList.put("strColumn6".toLowerCase(), "strColumn6");
        mapStingKeyList.put("strColumn7".toLowerCase(), "strColumn7");
        mapStingKeyList.put("SenderID".toLowerCase(), "SenderID");
        mapStingKeyList.put("ReceiverID".toLowerCase(), "ReceiverID");
        processString("", sel, mapStingKeyList, bizMap, w);
        processString("min_", sel_min, mapStingKeyList, bizMap, w);
        processString("max_", sel_max, mapStingKeyList, bizMap, w);
        processString("count_", sel_count, mapStingKeyList, bizMap, w);
        processInt("", sel, mapIntKeyList, bizMap, w);
        processInt("min_", sel_min, mapIntKeyList, bizMap, w);
        processInt("max_", sel_max, mapIntKeyList, bizMap, w);
        processInt("count_", sel_count, mapIntKeyList, bizMap, w);
        processDate("", sel, mapDateKeyList, bizMap, w);
        processDate("min_", sel_min, mapDateKeyList, bizMap, w);
        processDate("max_", sel_max, mapDateKeyList, bizMap, w);
        processDate("count_", sel_count, mapDateKeyList, bizMap, w);
        wi.setBizObject(bizMap);
        processMethodValue("", sel, wi, w);
        processMethodValue("min_", sel_min, wi, w);
        processMethodValue("max_", sel_max, wi, w);
        processMethodValue("count_", sel_count, wi, w);
        return wi;
    }

    private static List<String> getMethodKeyList() {
        ArrayList methodKeyList = new ArrayList();
        String property = "";
        property = "Actionurl".toLowerCase();
        methodKeyList.add(property);
        property = "Activitydefid".toLowerCase();
        methodKeyList.add(property);
        property = "Activityinstid".toLowerCase();
        methodKeyList.add(property);
        property = "Activityinstname".toLowerCase();
        methodKeyList.add(property);
        property = "Createtime".toLowerCase();
        methodKeyList.add(property);
        property = "Currentstate".toLowerCase();
        methodKeyList.add(property);
        property = "Endtime".toLowerCase();
        methodKeyList.add(property);
        property = "Finaltime".toLowerCase();
        methodKeyList.add(property);
        property = "Istimeout".toLowerCase();
        methodKeyList.add(property);
        property = "Participant".toLowerCase();
        methodKeyList.add(property);
        property = "Processchname".toLowerCase();
        methodKeyList.add(property);
        property = "Processdefid".toLowerCase();
        methodKeyList.add(property);
        property = "Processinstid".toLowerCase();
        methodKeyList.add(property);
        property = "Processinstname".toLowerCase();
        methodKeyList.add(property);
        property = "Processdefname".toLowerCase();
        methodKeyList.add(property);
        property = "Remindtime".toLowerCase();
        methodKeyList.add(property);
        property = "Rootprocinstid".toLowerCase();
        methodKeyList.add(property);
        property = "workItemID";
        methodKeyList.add(property);
        return methodKeyList;
    }

    public static void processMethodValue(String pri, List<String> sel, WFWorkItem wi, DataObject w) {
        ArrayList methodKeyList = new ArrayList();
        String property = "";
        if(sel != null && sel.size() != 0) {
            property = "Actionurl".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setActionURL(w.getString(pri + property));
            }

            property = "Activitydefid".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setActivityDefID(w.getString(pri + property));
            }

            property = "Activityinstid".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setActivityInstID(w.getLong(property));
            }

            property = "Activityinstname".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setActivityInstName(w.getString(pri + property));
            }

            property = "Createtime".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setCreateTime(w.getDate(pri + property) == null?null:CalendarUtil.getSpitTimeString(w.getDate(pri + property).getTime()));
            }

            property = "Currentstate".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setCurrentState(w.getInt(pri + property));
            }

            property = "Endtime".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setEndTime(w.getDate(pri + property) == null?null:CalendarUtil.getSpitTimeString(w.getDate(pri + property).getTime()));
            }

            property = "Finaltime".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setFinalTime(w.getDate(pri + property) == null?null:CalendarUtil.getSpitTimeString(w.getDate(pri + property).getTime()));
            }

            property = "Istimeout".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setIsTimeOut(w.getString(pri + property));
            }

            property = "Participant".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setParticipant(w.getString(pri + property));
            }

            property = "Processchname".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setProcessChName(w.getString(pri + property));
            }

            property = "Processdefid".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setProcessDefID(w.getLong(pri + property));
            }

            property = "Processinstid".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setProcessInstID(w.getLong(pri + property));
            }

            property = "Processinstname".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setProcessInstName(w.getString(pri + property));
            }

            property = "Processdefname".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setProcessDefName(w.getString(pri + property));
            }

            property = "Remindtime".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setRemindTime(w.getDate(pri + property) == null?null:CalendarUtil.getSpitTimeString(w.getDate(pri + property).getTime()));
            }

            property = "Rootprocinstid".toLowerCase();
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setRootProcInstID(w.getLong(pri + property));
            }

            property = "workItemID";
            methodKeyList.add(property);
            if(sel.contains(property)) {
                wi.setWorkItemID(w.getLong(pri + property));
            }

        }
    }

    private static void processString(String pri, List<String> sel, Map<String, String> mapStingKeyList, Map<String, Object> bizMap, DataObject w) {
        if(sel != null && sel.size() != 0) {
            Iterator i$ = sel.iterator();

            while(i$.hasNext()) {
                String key = (String)i$.next();
                if(mapStingKeyList.containsKey(key)) {
                    String val = pri + key;
                    bizMap.put(mapStingKeyList.get(key), w.getString(val));
                }
            }

        }
    }

    private static void processInt(String pri, List<String> sel, Map<String, String> mapStingKeyList, Map<String, Object> bizMap, DataObject w) {
        if(sel != null && sel.size() != 0) {
            Iterator i$ = sel.iterator();

            while(i$.hasNext()) {
                String key = (String)i$.next();
                if(mapStingKeyList.containsKey(key)) {
                    String val = pri + key;
                    bizMap.put(mapStingKeyList.get(key), Integer.valueOf(w.getInt(val)));
                }
            }

        }
    }

    private static void processDate(String pri, List<String> sel, Map<String, String> mapStingKeyList, Map<String, Object> bizMap, DataObject w) {
        if(sel != null && sel.size() != 0) {
            Iterator i$ = sel.iterator();

            while(i$.hasNext()) {
                String key = (String)i$.next();
                if(mapStingKeyList.containsKey(key)) {
                    String val = pri + key;
                    bizMap.put(mapStingKeyList.get(key), w.getDate(val) == null?null:CalendarUtil.getSpitTimeString(w.getDate(val).getTime()));
                }
            }

        }
    }

    protected static WFWorkItem WfwaitView2WorkItem(DataObject w) {
        WFWorkItem wi = new WFWorkItem();
        HashMap bizMap = new HashMap();
        HashMap mapStingKeyList = new HashMap();
        HashMap mapIntKeyList = new HashMap();
        HashMap mapDateKeyList = new HashMap();
        mapStingKeyList.put("jobID".toLowerCase(), "jobID");
        mapStingKeyList.put("jobCode".toLowerCase(), "jobCode");
        mapStingKeyList.put("jobTitle".toLowerCase(), "jobTitle");
        mapStingKeyList.put("jobType".toLowerCase(), "jobType");
        mapDateKeyList.put("jobEndtime".toLowerCase(), "jobEndtime");
        mapDateKeyList.put("jobStarttime".toLowerCase(), "jobStarttime");
        mapDateKeyList.put("reBacktime".toLowerCase(), "reBacktime");
        mapStingKeyList.put("shard".toLowerCase(), "shard");
        mapStingKeyList.put("businessId".toLowerCase(), "businessId");
        mapStingKeyList.put("productcode".toLowerCase(), "productcode");
        mapStingKeyList.put("majorcode".toLowerCase(), "majorcode");
        mapDateKeyList.put("datColumn1".toLowerCase(), "datColumn1");
        mapDateKeyList.put("datColumn2".toLowerCase(), "datColumn2");
        mapIntKeyList.put("numColumn1".toLowerCase(), "numColumn1");
        mapIntKeyList.put("numColumn2".toLowerCase(), "numColumn2");
        mapIntKeyList.put("rootnmColumn1".toLowerCase(), "rootnmColumn1");
        mapIntKeyList.put("rootnmColumn2".toLowerCase(), "rootnmColumn2");
        mapStingKeyList.put("rootvcColumn1".toLowerCase(), "rootvcColumn1");
        mapStingKeyList.put("rootvcColumn2".toLowerCase(), "rootvcColumn2");
        mapStingKeyList.put("strColumn1".toLowerCase(), "strColumn1");
        mapStingKeyList.put("strColumn2".toLowerCase(), "strColumn2");
        mapStingKeyList.put("strColumn3".toLowerCase(), "strColumn3");
        mapStingKeyList.put("strColumn4".toLowerCase(), "strColumn4");
        mapStingKeyList.put("strColumn5".toLowerCase(), "strColumn5");
        mapStingKeyList.put("strColumn6".toLowerCase(), "strColumn6");
        mapStingKeyList.put("strColumn7".toLowerCase(), "strColumn7");
        mapStingKeyList.put("SenderID".toLowerCase(), "SenderID");
        mapStingKeyList.put("ReceiverID".toLowerCase(), "ReceiverID");
        ArrayList sel = null;
        sel = new ArrayList();
        Iterator i$ = mapStingKeyList.keySet().iterator();

        String a;
        while(i$.hasNext()) {
            a = (String)i$.next();
            sel.add(a);
        }

        processString("", sel, mapStingKeyList, bizMap, w);
        sel = new ArrayList();
        i$ = mapIntKeyList.keySet().iterator();

        while(i$.hasNext()) {
            a = (String)i$.next();
            sel.add(a);
        }

        processInt("", sel, mapIntKeyList, bizMap, w);
        sel = new ArrayList();
        i$ = mapDateKeyList.keySet().iterator();

        while(i$.hasNext()) {
            a = (String)i$.next();
            sel.add(a);
        }

        processDate("", sel, mapDateKeyList, bizMap, w);
        wi.setBizObject(bizMap);
        processMethodValue("", getMethodKeyList(), wi, w);
        return wi;
    }

    public List<TaskInstance> getSuspendTasks(TaskFilter taskfilter) throws WFException {
        taskfilter.setTaskType("3");
        return FindWorkItem.getMyTasks(taskfilter, this);
    }

    public long getFinalTime(String name, long arg1, long arg2) throws WFException {
        try {
            BPSServiceClientFactory.getLoginManager().setCurrentUser(this.accountID, this.accountID, this.appID, (String)null);
            IBPSServiceManagerExt e = (IBPSServiceManagerExt)BPSServiceClientFactoryExt.getDefaultClient().getService(IBPSServiceManagerExt.class);
            String id = e.queryCalendaruuid(this.appID, name);
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();
            long s = client.getCalendarManagerService().getFinalTime(id, arg1, arg2);
            return s;
        } catch (WFServiceException var11) {
            throw new WFException("getFinalTime处理失败", var11);
        }
    }

    public List<TaskInstance> getMyCompletedTasksDistinctProinstanceId(TaskFilter tf) throws WFException {
        ArrayList tl = new ArrayList();

        try {
            if(tf == null) {
                throw new WFException("查询条件不能为空");
            } else {
                HashMap e = new HashMap();
                String appid = null;
                if(this.isMultiTenantMode) {
                    appid = this.appID;
                }

                e.put("appid", appid);
                String userid = this.accountID;
                String tfpmname = tf.getProcessModelName();
                String processmodelname = null;
                if(tfpmname != null) {
                    processmodelname = "";
                    if(tfpmname.indexOf(",") != -1) {
                        String[] searchMap = tfpmname.split(",");

                        for(int jobStarttimeStrart = 0; jobStarttimeStrart < searchMap.length - 1; ++jobStarttimeStrart) {
                            String jobStarttimeEnd = "\'" + searchMap[jobStarttimeStrart] + "\',";
                            processmodelname = processmodelname + jobStarttimeEnd;
                        }

                        processmodelname = processmodelname + "\'" + searchMap[searchMap.length - 1] + "\'";
                    } else {
                        processmodelname = "\'" + tfpmname + "\'";
                    }
                }

                e.put("deilman", userid);
                e.put("processmodelname", processmodelname);
                System.out.println("userid---------" + userid);
                System.out.println("appid---------" + appid);
                e.put("jobid", tf.getJobID());
                e.put("jobcode", tf.getJobCode());
                e.put("jobtitle", tf.getJobTitle());
                e.put("productcode", tf.getProductID());
                e.put("act", "activitydefid");
                e.put("strcolumn1", tf.getStrColumn1());
                e.put("strcolumn2", tf.getStrColumn2());
                e.put("strcolumn3", tf.getStrColumn3());
                e.put("datstarttime1", tf.getDatColumn1StartTime());
                e.put("datendtime1", tf.getDatColumn1EndTime());
                e.put("datstarttime2", tf.getDatColumn2StartTime());
                e.put("datendtime2", tf.getDatColumn2EndTime());
                e.put("rootvccolumn1", tf.getRootvcColumn1());
                e.put("rootvccolumn2", tf.getRootvcColumn2());
                e.put("rootnmcolumn2", Integer.valueOf(tf.getRootnmColumn2()));
                e.put("jobtype", tf.getJobType());
                Map var23 = tf.getSearchMap();
                new Date();
                new Date();
                String businessId = "";
                if(null != var23) {
                    Set sortMap = var23.keySet();
                    Iterator pageCond = sortMap.iterator();

                    while(pageCond.hasNext()) {
                        String datas = (String)pageCond.next();
                        e.put(datas, var23.get(datas));
                    }
                }

                Map var24 = tf.getSortMap();
                if(null != var24) {
                    Set var25 = var24.keySet();
                    Iterator var27 = var25.iterator();
                    if(var27.hasNext()) {
                        e.put("sortString", var27.next());
                    }
                }

                DataObject var26 = DataFactory.INSTANCE.create("com.eos.foundation", "PageCond");
                if(tf.getPageCondition() == null) {
                    throw new WFException("分页条件不能为空");
                } else {
                    var26.set("begin", Integer.valueOf(tf.getPageCondition().getBegin()));
                    var26.set("length", Integer.valueOf(tf.getPageCondition().getLength()));
                    var26.set("isCount", tf.getPageCondition().getIsCount());
                    DataObject[] var28 = ((IBPSServiceManagerExt)BPSServiceClientFactoryExt.getDefaultClient().getService(IBPSServiceManagerExt.class)).getnamingsql(e, var26);
                    if(var28 != null && var28.length >= 1) {
                        DataObject pc = var28[0];
                        PageCond pcNew = new PageCond();
                        pcNew.setBegin(pc.getInt("begin"));
                        pcNew.setLength(pc.getInt("length"));
                        pcNew.setIsCount(pc.getBoolean("isCount"));
                        pcNew.setCount(pc.getInt("count"));
                        pcNew.setCurrentPage(pc.getInt("currentPage"));
                        pcNew.setFirst(pc.getBoolean("isFirst"));
                        pcNew.setLast(pc.getBoolean("isLast"));
                        pcNew.setTotalPage(pc.getInt("totalPage"));
                        PageCondition pcOld = null;
                        if(tf != null) {
                            pcOld = tf.getPageCondition();
                        }

                        if(pcOld != null) {
                            DataConvertor.convert2PageCond(pcNew, pcOld);
                        }

                        for(int i = 1; i < var28.length; ++i) {
                            DataObject d = var28[i];
                            TaskInstance taskInsts = new TaskInstance();
                            taskInsts.setTaskInstID(d.getString("taskInstID"));
                            taskInsts.setCurrentState("2");
                            taskInsts.setRootProcessInstId(d.getString("rootProcessInstId"));
                            if(d.getDate("completionDate") != null) {
                                taskInsts.setCompletionDate(new Date(d.getDate("completionDate").getTime()));
                            }

                            if(d.getDate("createTime") != null) {
                                taskInsts.setCreateDate(new Date(d.getDate("createTime").getTime()));
                            }

                            taskInsts.setShard(d.getString("shard"));
                            taskInsts.setBusinessId(d.getString("businessId"));
                            taskInsts.setPRODUCT_ID(d.getString("productcode"));
                            taskInsts.setMAJOR_ID(d.getString("majorcode"));
                            taskInsts.setJobID(d.getString("jobid"));
                            taskInsts.setJobCode(d.getString("jobcode"));
                            taskInsts.setJobTitle(d.getString("jobtitle"));
                            taskInsts.setStrColumn1(d.getString("strcolumn1"));
                            taskInsts.setStrColumn2(d.getString("strcolumn2"));
                            taskInsts.setStrColumn3(d.getString("strcolumn3"));
                            if(null != d.getDate("jobstarttime")) {
                                taskInsts.setJobStarttime(new Date(d.getDate("jobstarttime").getTime()));
                            }

                            taskInsts.setRootvcColumn1(d.getString("rootvccolumn1"));
                            taskInsts.setRootvcColumn2(d.getString("rootvccolumn2"));
                            taskInsts.setRootnmColumn2(d.getInt("rootnmcolumn2"));
                            taskInsts.setJobtype(d.getString("jobtype"));
                            if(null != d.getDate("datcolumn1")) {
                                taskInsts.setDatColumn2(new Date(d.getDate("datcolumn1").getTime()));
                            }

                            if(null != d.getDate("datcolumn2")) {
                                taskInsts.setDatColumn2(new Date(d.getDate("datcolumn2").getTime()));
                            }

                            tl.add(taskInsts);
                        }

                        return tl;
                    } else {
                        return null;
                    }
                }
            }
        } catch (WFServiceException var21) {
            throw new WFException(var21);
        } catch (Exception var22) {
            throw new WFException(var22);
        }
    }

    public String restartFinishedProcessInst(String processInstID, String activityDefID, List<Participant> parts) throws WFException {
        String rsProInID = null;

        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            long proInID = Long.parseLong(processInstID);
            WFParticipant[] wfparts = null;
            if(parts != null) {
                int partslengh = parts.size();
                wfparts = new WFParticipant[partslengh];

                for(int i = 0; i < partslengh; ++i) {
                    WFParticipant p = DataConvertor.convert2WFParticipant((Participant)parts.get(i));
                    wfparts[i] = p;
                }
            }

            rsProInID = String.valueOf(e.getProcessInstManager().restartFinishedProcessInst(proInID, activityDefID, wfparts));
            return rsProInID;
        } catch (WFServiceException var12) {
            throw new WFException("restartFinishedProcessInst处理失败", var12);
        } catch (WFReasonableException var13) {
            throw new WFException("restartFinishedProcessInst处理失败", var13);
        }
    }

    public List<ActivityInstance> getActivityInstances(String arg0, Map<String, String> arg1) throws WFException {
        throw new WFException("此方法普元不实现");
    }

    public void updateNoRootbizInfo(String processInstID, String activityInstID, Map<String, Object> bizModelParams) throws WFException {
        try {
            BPSServiceClientFactory.getLoginManager().setCurrentUser(this.accountID, this.accountID, this.appID, (String)null);
            IBPSServiceManagerExt e = (IBPSServiceManagerExt)BPSServiceClientFactoryExt.getDefaultClient().getService(IBPSServiceManagerExt.class);
            WFBusiinfo data = (WFBusiinfo)WFBusiinfo.FACTORY.create();
            Iterator temp = bizModelParams.keySet().iterator();

            while(temp.hasNext()) {
                String key = (String)temp.next();
                Object value = bizModelParams.get(key);
                data.set(key, value);
            }

            WFBusiinfo temp1 = (WFBusiinfo)WFBusiinfo.FACTORY.create();
            temp1.setProcessinstid(Long.parseLong(processInstID));
            temp1.setActivityinstid(Long.parseLong(activityInstID));
            e.updateEntityByTemplate(data, temp1);
        } catch (WFServiceException var9) {
            throw new WFException("updateNoRootbizInfo处理失败", var9);
        } catch (NumberFormatException var10) {
            throw new WFException("updateNoRootbizInfo处理失败", var10);
        }
    }

    public List<TaskInstance> getParentWorkflowNextActivityt(String processInstID) throws WFException {
        new ArrayList();

        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            WFProcessInst ProcessInst = e.getProcessInstManager().queryProcessInstDetail(Long.parseLong(processInstID));
            long ParentProcID = ProcessInst.getParentProcID();
            long ParentActID = ProcessInst.getParentActID();
            List ParentProActs = e.getProcessInstManager().getNextActivitiesMaybeArrived(ParentActID);
            if(ParentProActs == null) {
                return null;
            } else {
                ArrayList allActivityInsts = new ArrayList();
                Iterator WorkItems = ParentProActs.iterator();

                while(WorkItems.hasNext()) {
                    WFActivityDefine i$ = (WFActivityDefine)WorkItems.next();
                    List aac = e.getActivityInstManager().queryActivityInstsByActivityID(ParentProcID, i$.getId(), new PageCond(2147483647));
                    if(aac != null) {
                        Iterator i$1 = aac.iterator();

                        while(i$1.hasNext()) {
                            WFActivityInst ac = (WFActivityInst)i$1.next();
                            allActivityInsts.add(ac);
                        }
                    }
                }

                if(allActivityInsts == null) {
                    return null;
                } else {
                    Object WorkItems1 = new ArrayList();

                    WFActivityInst aac1;
                    for(Iterator i$2 = allActivityInsts.iterator(); i$2.hasNext(); WorkItems1 = e.getWorkItemManager().queryWorkItemsByActivityInstID(aac1.getActivityInstID(), new PageCond(2147483647))) {
                        aac1 = (WFActivityInst)i$2.next();
                    }

                    if(WorkItems1 == null) {
                        return null;
                    } else {
                        List ParentProTasks = DataConvertor.convert2TaskInstList((List)WorkItems1);
                        return ParentProTasks;
                    }
                }
            }
        } catch (WFServiceException var16) {
            throw new WFException("getParentWorkflowNextActivityt处理失败", var16);
        } catch (WFException var17) {
            throw new WFException("getParentWorkflowNextActivityt处理失败", var17);
        }
    }

    public List<TaskInstance> getMyWaitingTasksOrderFinaltime(TaskFilter taskfilter) throws WFException {
        taskfilter.setTaskType("1_ORDERFINALTIME");
        return FindWorkItem.getMyTasks(taskfilter, this);
    }

    public String createAndStartActivityInstance(String processInstID, String activityDefID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            long actId = e.getActivityInstManager().createAndStartActivityInstance(Long.parseLong(processInstID), activityDefID);
            String ActivityID = String.valueOf(actId);
            return ActivityID;
        } catch (WFServiceException var7) {
            throw new WFException("createAndStartActivityInstance处理失败", var7);
        }
    }

    public void terminateActivityInstance(String activityInstID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            e.getActivityInstManager().terminateActivityInstance(Long.parseLong(activityInstID));
        } catch (WFServiceException var3) {
            throw new WFException("terminateActivityInstance处理失败", var3);
        }
    }

    public ActivityInstance findLastActivityInstByActivityID(String procInstID, String activityDefID) throws WFException {
        try {
            IBPSServiceClient e = BPSServiceClientFactory.getDefaultClient();
            WFActivityInst actinst = e.getActivityInstManager().findLastActivityInstByActivityID(Long.parseLong(procInstID), activityDefID);
            ActivityInstance ActivityInst = DataConvertor.convert2ActivityInst(actinst);
            return ActivityInst;
        } catch (WFServiceException var6) {
            throw new WFException("findLastActivityInstByActivityID处理失败", var6);
        } catch (ParseException var7) {
            throw new WFException("findLastActivityInstByActivityID处理失败", var7);
        }
    }

    public void restartActivityInstance(String activityInstID) throws WFException {
        try {
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();
            client.getActivityInstManager().restartActivityInstance(Long.parseLong(activityInstID));
        } catch (WFServiceException var4) {
            throw new WFException("restartActivityInstance处理失败", var4);
        }
    }

    public List<Object> getAllActivityInstances(String arg0, Map<String, String> arg1) throws WFException {
        throw new WFException("此方法普元不实现");
    }

    public void backNoTaskActivity(String arg0, String arg1) throws WFException {
        throw new WFException("此方法普元不实现");
    }

    public List<TaskInstance> queryNextWorkItemsByProcessInstID(long ProcessInstID) throws WFException {
        try {
            IBPSServiceClient client = BPSServiceClientFactory.getDefaultClient();
            List e = client.getWorkItemManager().queryNextWorkItemsByProcessInstID(ProcessInstID, true);
            List TaskInstances = DataConvertor.convert2TaskInstList(e);
            return TaskInstances;
        } catch (WFServiceException var6) {
            throw new WFException("restartActivityInstance处理失败", var6);
        }
    }

    public String findDoingActivitys(String jobid) throws WFException {
        try {
            String result = ((IBPSServiceManagerExt)BPSServiceClientFactoryExt.getDefaultClient().getService(IBPSServiceManagerExt.class)).findDoingActivitys(jobid, this.appID);
            return result;
        } catch (WFServiceException var4) {
            throw new WFException("findDoingActivitys处理失败", var4);
        }
    }
}
