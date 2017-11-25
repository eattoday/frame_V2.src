package com.metarnet.core.common.service.impl;

/**
 * Created with IntelliJ IDEA.
 * User: wangzwty
 * Date: 16-4-10
 * Time: 下午4:10
 * 公共处理
 */

import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.EnumConfigAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter4Activiti;
import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.DAOException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.*;
import com.metarnet.core.common.service.IGeneOperateService;
import com.metarnet.core.common.service.IWorkflowBaseService;
import com.metarnet.core.common.utils.*;
import com.metarnet.core.common.workflow.*;
import com.sun.xml.internal.ws.util.UtilException;
import com.ucloud.paas.agent.PaasException;
import com.ucloud.paas.proxy.aaaa.entity.OrgEntity;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import javax.persistence.DiscriminatorValue;
import java.beans.IntrospectionException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.*;

/**
 * 保存申请单调度单
 */
public abstract class GeneOperateService implements IGeneOperateService {


    private Logger logger = LogManager.getLogger();

    private String participant = "'participant':[{'participantID':'#participantID','participantName':'','participantType':'1'}]";
    private String participantID = "#participantID";

    @Resource
    protected IWorkflowBaseService workflowBaseService;

    @Resource
    private IBaseDAO baseDAO;

    @Resource
    private NotifyInter remoteOAForXJ;


    public void initForm(BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        baseForm.setOperUserId(userEntity.getUserId());
        baseForm.setOperUserTrueName(userEntity.getTrueName());
        baseForm.setOperUserPhone(userEntity.getMobilePhone());
        baseForm.setOperOrgId(userEntity.getOrgID());
        baseForm.setOperOrgName(userEntity.getOrgEntity().getOrgName());
        baseForm.setOperFullOrgName(userEntity.getOrgEntity().getFullOrgName());
        baseForm.setOperTime(new Timestamp(new Date().getTime()));
    }


    public void saveProcessInfo(BaseForm baseForm, TaskInstance taskInstance) {
        //暂时没存父流程  启动流程会走这个方法，所以taskInstance是主流程
        baseForm.setRootProInstId(taskInstance.getProcessInstID());
        baseForm.setRootDisId(baseForm.getObjectId());
        baseForm.setProcessInstId(taskInstance.getProcessInstID());
        baseForm.setTaskInstId(taskInstance.getTaskInstID());
        baseForm.setActivityInstName(taskInstance.getActivityInstName());
        try {
            baseDAO.update(baseForm);
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 生成工单编号
     *
     * @param processCodeOrBusinessType 申请单时：业务编码 调度单时：业务类型
     * @param baseForm                  业务实体
     * @param userEntity                当前登录用户
     * @throws ServiceException
     */
    public abstract String geneworkOrderNumber(String processCodeOrBusinessType, BaseForm baseForm,
                                               UserEntity userEntity) throws ServiceException;

    /**
     * 保存业务信息
     */
    public void saveForm(BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        try {
            baseDAO.save(baseForm, userEntity);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public void saveOrUpdateForm(BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        try {
            baseDAO.saveOrUpdate(baseForm, userEntity);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * 通过注解解析附件等信息
     *
     * @param baseForm 实体
     */
    public void analyzeForm(BaseForm baseForm, List<Participant> participants, String route) throws UtilException,
            IntrospectionException, InvocationTargetException, IllegalAccessException {
        EntityBinder entityBinder = AnnotationReader.readEntity(baseForm.getClass());
        //处理objectId为0的情况
        if (baseForm.getObjectId() != null && baseForm.getObjectId() == 0) {
            baseForm.setObjectId(null);
        }

        //解析下一步处理人
        if (Constants.ROUTE_START_SUBMIT.equals(route) && null != entityBinder.getParticipantField()) {
            String _participants = (String) HibernateBeanUtils.getValue(baseForm,
                    entityBinder.getParticipantField().getName());
            if (StringUtils.isNotEmpty(_participants)) {
                if (participants == null) {
                    participants = new ArrayList<Participant>();
                }
                for (String str : _participants.split(",")) {
                    if (str.contains(":")) {
                        str = str.substring(0, str.indexOf(":"));
                        try {
                            str = AAAAAdapter.getInstence().findUserbyUserID(Integer.parseInt(str)).getUserName();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Participant _participant = new Participant();
                    _participant.setParticipantID(str);
                    _participant.setParticipantName(str);
                    _participant.setParticipantType("1");
                    participants.add(_participant);
                }
            }
        }
    }

    /**
     * 更新业务信息
     */
    public void saveOrUpdateForm(BaseForm baseForm, String processCode, UserEntity userEntity,
                                 TaskInstance taskInstance) throws
            ServiceException {
        try {
            Serializable entity = baseDAO.findById(baseForm);
            if (entity == null) {
                baseDAO.save(baseForm, userEntity);
                return;
            }
            BaseForm baseFormPO = (BaseForm) entity;
            //混合继承模式需先判断其对应的子表是否发生变化,如果变化需要先删除，再保存
            if (baseForm.getClass().isAnnotationPresent(DiscriminatorValue.class) && baseForm.getClass().isAnnotationPresent(
                    DiscriminatorValue.class) && !baseForm.getClass().equals(baseFormPO.getClass())) {
                baseDAO.delete(baseFormPO);
            }
            BeanUtils.copyProperties(baseForm, baseFormPO, false);
            baseDAO.update(baseFormPO, Integer.valueOf(userEntity.getUserId().toString()));
        } catch (Exception e) {
            e.printStackTrace();
            //注释了 by wangzwty@inspur.com
            //适配其他框架开发的代码，已有ID的注解以及赋值，此处就不需要保存了
//            throw new ServiceException(e);
        }
    }


    /**
     * 设置业务参数
     *
     * @param baseForm
     * @return
     */
    public Map<String, Object> getBizModleParams(BaseForm baseForm, UserEntity userEntity) throws AdapterException {
        Map<String, Object> bizModleParams = new HashMap<String, Object>();
        //工单主题
        bizModleParams.put(Constants.JOB_TITLE, baseForm.getTheme());
        //工单主键
        bizModleParams.put(Constants.JOB_ID, new Date().getTime() + baseForm.getObjectId().toString());
        //工单编号
        bizModleParams.put(Constants.JOB_CODE, baseForm.getDisOrderNumber() == null ? baseForm.getAppOrderNumber() : baseForm.getDisOrderNumber());
        //分片ID
        bizModleParams.put(Constants.SHARDING_ID, StringUtils.EMPTY);
        return bizModleParams;
    }


    /**
     * 获取业务参数集
     *
     * @return
     * @throws ServiceException
     */
    public ProcessModelParams getStartProcessModelParams(BaseForm baseForm, UserEntity userEntity) throws PaasAAAAException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        ProcessModelParams processModelParams = new ProcessModelParams();
        String accountId = userEntity.getUserName();
        //开始环节路由信息
        processModelParams.setParameter(Constants.SOURCE, StringUtils.EMPTY);
        //设置是否为根流程
        processModelParams.setParameter(Constants.IS_ROOT, Constants.Y);
        //设置第一步环节参与者
        String participant =
                "{'areacode':[],'majorcode':[],'orgcode':[],'productcode':[],'participant':[{'participantID':'"
                        + accountId + "','participantName':'','participantType':'1'}]}";
        processModelParams.setParameter(Constants.FIRST_STEP_USER, participant);
        OrgEntity orgEntity = AAAAAdapter.getInstence().getCompany(Integer.valueOf(userEntity.getOrgID().toString()));
        processModelParams.setParameter(Constants.ORG_NAME, orgEntity.getOrgName());
        processModelParams.setParameter(Constants.ORG_CODE, orgEntity.getOrgCode());
        // 上级部门审核参与者
        WFParticipant wfParticipant = new WFParticipant();
        wfParticipant.setTypeCode("person");
        wfParticipant.setName(accountId);
        wfParticipant.setId(accountId);
        processModelParams.setParameter(Constants.PARENTDISPATCHER, wfParticipant);
        processModelParams.setParameter(Constants.RELATEDDISPATCHID, Constants.DEFAULT_RELATEDDISPATCHID);
        //子流程驳回父流程需要的参数
        Field[] fields = baseForm.getClass().getDeclaredFields();
        if (baseForm.getReqFdbkTime() != null) {
            processModelParams.setParameter(Constants.BIZ_DATCOLUMN1, String.valueOf(baseForm.getReqFdbkTime().getTime()));
        } else {
            processModelParams.setParameter(Constants.BIZ_DATCOLUMN1, "");
        }
        if (Constants.IS_SHOW_MAJOR) {
            String speciality = "";
            if (baseForm != null && baseForm.getSpecialty() != null && !"".equals(baseForm.getSpecialty())) {
                try {
                    speciality = EnumConfigAdapter.getInstence().getEnumValueById(baseForm.getSpecialty()).getEnumValueName();
                } catch (PaasException e) {
                    e.printStackTrace();
                }
            }
            processModelParams.setParameter(Constants.BIZ_STRCOLUMN1, speciality);
        }

        processModelParams.setParameter(Constants.JOB_TITLE, baseForm.getTheme());
        processModelParams.setParameter(Constants.JOB_CODE, baseForm.getDisOrderNumber() == null ? baseForm.getAppOrderNumber() : baseForm.getDisOrderNumber());
        if (baseForm.getAppOrderNumber() != null) {
            processModelParams.setParameter(Constants.ACTION_TYPE, "apply");
        } else {
            processModelParams.setParameter(Constants.ACTION_TYPE, "taskdispatch");
        }
        processModelParams.setParameter(Constants.approvalStatusSub, Constants.Y);
        return processModelParams;
    }


    /**
     * 获取流程挂接扩展信息
     *
     * @param baseForm 业务实体
     * @return
     */
    protected abstract String getProcessExtendAttribute(BaseForm baseForm);

    /**
     * 创建流程
     *
     * @param baseForm               业务实体
     * @param bizModleParams         业务参数
     * @param participants           下一步操作人
     * @param processModelParams     流程模型对象参数
     * @param businessCode           业务类型
     * @param userEntity             当前操作人
     * @param processExtendAttribute
     * @return
     * @throws ServiceException
     */
    public String startProcess(BaseForm baseForm, Map<String, Object> bizModleParams, List<Participant> participants,
                               ProcessModelParams processModelParams, String processExtendAttribute,
                               String businessCode, UserEntity userEntity) throws ServiceException {
        //查询当前组织关联的流程模板
        ProcessDefInfo processDefInfo = AAAAAdapter.getInstence().getProcessDefName(StringUtils.isEmpty(businessCode) ? Constants.BUSINESS_TYPE_CODE : businessCode, processExtendAttribute);
        if (null == processDefInfo) {
            logger.info("未配置流程数据");
            return "";
        }
        //目前api只支持传入一个参与者
        Participant participant = null;
        if (participants != null && participants.size() > 0) {
            participant = participants.get(0);
        }
        return workflowBaseService.startProcess(userEntity, baseForm, processDefInfo.getProcessDefName(), participant, bizModleParams, processModelParams);
    }


    /**
     * 提交流程
     */
    /*public void submitProcess(TaskInstance taskInstance, List<Participant> participants, BaseForm baseForm,
                              UserEntity userEntity) throws ServiceException {
        try {
            workflowBaseService.submitTask(taskInstance, participants, baseForm, userEntity);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }
*/
/*    *//**
     * 保存流程对象关联表
     *//*
    public TEomFlowingObjProcInsRel saveFlowingObjProcInsRel(baseForm baseForm, String processInstID, String activityDefId,
                                                             UserEntity userEntity) throws ServiceException {
        try {
            return workflowOperateService.saveFlowingObjProcInsRel(baseForm, processInstID,
                    activityDefId, userEntity);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }*/

/*    *//**
     * 保存附件
     *//*
    public void saveAttachmentInfo(BaseForm baseForm, String processInstID,
                                   UserEntity userEntity) throws ServiceException {
        //操作附件
        if (baseForm.getAttachmentList() == null || baseForm.getAttachmentList().size() == 0) {
            return;
        }
        for (String attachmentInfo : baseForm.getAttachmentList()) {
            attachmentRelProcService.updateAttachements(userEntity, attachmentInfo, baseForm.getObjectId(),
                    Constants.START_ACTIVITY, processInstID, null, null);
        }
    }*/

    /**
     * 创建启动流程时需要传递给相关数据区的数据集
     *
     * @param baseForm 业务实体
     * @return
     * @throws ServiceException
     */
    public Map<String, Object> getRelativeData(BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        Map<String, Object> relDatas = new HashMap<String, Object>();
        try {
            //设置操作的业务表名及主键值
            relDatas.put(Constants.OBJECT_ID, baseForm.getObjectId().toString());
            relDatas.put(Constants.OBJECT_TABLE, HibernateBeanUtils.getTableName(baseForm.getClass()));
        } catch (Exception e) {
            throw new ServiceException(e);
        }
        return relDatas;
    }

    /**
     * 保存前处理业务
     *
     * @param baseForm     业务实体
     * @param participants 参与者
     * @param route        路由选择
     * @param userEntity   当前用户
     * @throws ServiceException
     */
    public void beforeSaveProcess(TaskInstance taskInstance, BaseForm baseForm, List<Participant> participants,
                                  String route, UserEntity userEntity) throws ServiceException {
//        baseForm.setRootProInstId(taskInstance.getRootProcessInstId());
        baseForm.setProcessInstId(taskInstance.getProcessInstID());
        baseForm.setTaskInstId(taskInstance.getTaskInstID());
        baseForm.setActivityInstName(taskInstance.getActivityInstName());

    }


    protected Long findTEomSequence(String sequenceType, String woType, String orgIdentifier, String periodIdentifier)
            throws ServiceException {
        String sequenceName = (sequenceType + "_" + woType + "_" + orgIdentifier + "_" + periodIdentifier).replace(
                "/", "_");
        Long sequenceNextValue = 1L;
        try {
            sequenceNextValue = baseDAO.getSequenceNextValue(sequenceName);

            if (sequenceNextValue == 0L) {
                baseDAO.createSequence(sequenceName);
                sequenceNextValue = 1L;
            }
        } catch (DAOException e) {
            baseDAO.createSequence(sequenceName);
            logger.info("======findTEomSequence:error======");
            if (e.getMessage().contains("Sequence name is already used")) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    throw new ServiceException(e1);
                }
                return findTEomSequence(sequenceType, woType, orgIdentifier, periodIdentifier);
            }
            throw new ServiceException(e);
        } finally {
            return sequenceNextValue;
        }
    }

    public abstract void processPostStart(BaseForm baseForm, UserEntity userEntity) throws ServiceException;

    /**
     * 新建流程业务
     *
     * @param baseForm       业务实体
     * @param processCode    业务流程编码
     * @param route          路由选择
     * @param userEntity     当前用户
     * @param operTypeEnumId 操作类型枚举ID
     * @throws ServiceException
     */
    public final String start(BaseForm baseForm, String processCode, String route, String businessCode, Map relaDataMap,
                              UserEntity userEntity, Integer operTypeEnumId) throws ServiceException {
        return start(baseForm, processCode, route, businessCode, relaDataMap, userEntity, operTypeEnumId, "");
    }

    public final String start(BaseForm baseForm, String processCode, String route, String businessCode, Map relaDataMap,
                              UserEntity userEntity, Integer operTypeEnumId, String nextCandidateUser) throws ServiceException {
        try {
            //解析业务信息
            List<Participant> participants = new ArrayList<Participant>();
            analyzeForm(baseForm, participants, route);
            //保存业务信息
            saveOrUpdateForm(baseForm, userEntity);
            //设置业务参数
            Map<String, Object> bizModleParams = getBizModleParams(baseForm, userEntity);
            //设置开始环节参数
            ProcessModelParams processModelParams = getStartProcessModelParams(baseForm, userEntity);
            //获取流程挂接扩展信息
            String processExtendAttribute = getProcessExtendAttribute(baseForm);
            List<Participant> participantsList = new ArrayList<Participant>();
            Participant _participant = new Participant();
            _participant.setParticipantID(userEntity.getUserName());
            _participant.setParticipantName(userEntity.getTrueName());
            _participant.setParticipantType("1");
            participantsList.add(_participant);
            //启动流程
            String processInstID = startProcess(baseForm, bizModleParams, participantsList, processModelParams,
                    processExtendAttribute, businessCode, userEntity);
            //设置流程参数
            Map<String, Object> relaDatas = new HashMap<String, Object>();
            relaDatas.put(Constants.SOURCE, route);
            if (relaDataMap != null) {
                relaDatas.putAll(relaDataMap);
            }
            TaskInstance taskInstance = workflowBaseService.getTaskInstance(processInstID, "");
            relaDatas.put("processInstID", processInstID);
            relaDatas.put(Constants.ROOT_PROCESS_INST_ID, processInstID);
            WorkflowAdapter.setRelativeData(processInstID, relaDatas, userEntity.getUserName());
            if (null != route && Constants.ROUTE_START_SUBMIT.equals(route)) {
                workflowBaseService.setNextParticipant(taskInstance.getProcessInstID(), participants, userEntity, taskInstance);
            }
            workflowBaseService.submitTask(taskInstance, participants, baseForm, userEntity, operTypeEnumId, true, nextCandidateUser);
//                notifyInter.notice(NotifyInter.NOTICE_TYPE_CREATE, taskInstance, null, userEntity);

            //保存归属信息
            workflowBaseService.setBelongInfo(baseForm, userEntity);

            //保存流程信息
            saveProcessInfo(baseForm, taskInstance);
            processPostStart(baseForm, userEntity);
            SendNotice(taskInstance, userEntity, baseForm);
            return processInstID;
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public final void submit(TaskInstance taskInstance, BaseForm baseForm, String processCode, String route,
                             UserEntity userEntity, Integer operTypeEnumId) throws ServiceException {
        submit(taskInstance, baseForm, processCode, route, userEntity, operTypeEnumId, true, "");
    }

    public final void submit(TaskInstance taskInstance, BaseForm baseForm, String processCode, String route,
                             UserEntity userEntity, Integer operTypeEnumId, Boolean nolog, String nextCandidateUserNames) throws ServiceException {
        try {
            String accountId = userEntity.getUserName();
            //处理分片id
            if (Constants.IS_SHARDING) {
                Object shardingId = HibernateBeanUtils.getValue(baseForm, Constants.ADB_SHARDING_ID);
                if (shardingId == null || Integer.parseInt(String.valueOf(shardingId)) == 0) {
                    HibernateBeanUtils.setValue(baseForm, Constants.ADB_SHARDING_ID, Integer.parseInt(taskInstance.getShard()));
                }
            }
            //提交可能需要下一步参与者，participants 不能为空
            List<Participant> participants = new ArrayList<Participant>();
            //解析业务信息(主送、抄送、附件、下一步审核人)
            analyzeForm(baseForm, participants, route);
            //保存业务信息
            beforeSaveProcess(taskInstance, baseForm, participants, route, userEntity);
            saveOrUpdateForm(baseForm, processCode, userEntity, taskInstance);
            Map<String, Object> relaDatas = getRelativeData(baseForm, userEntity);
            //根流程设置分片信息(处理过程会使用)
            if (Constants.IS_SHARDING) {
                relaDatas.put(Constants.SHARDING_ID, HibernateBeanUtils.getValue(baseForm, Constants.ADB_SHARDING_ID));
            }
            relaDatas.put(Constants.ROUTE, route);
//            WFParticipant dispatcher = new WFParticipant();
//            dispatcher.setTypeCode("person");
//            dispatcher.setName(accountId);
//            dispatcher.setId(accountId);
//            relaDatas.put(Constants.SUBPARENTDISPATCHER, dispatcher);
//            relaDatas.put(Constants.SUB_RELATEDDISPATCHID, baseForm.getObjectId().toString());
            WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relaDatas, accountId);
            if (null != route && Constants.ROUTE_START_SUBMIT.equals(route)) {
                workflowBaseService.setNextParticipant(taskInstance.getProcessInstID(), participants, userEntity, taskInstance);
            }
            workflowBaseService.submitTask(taskInstance, participants, baseForm, userEntity, operTypeEnumId, nolog, nextCandidateUserNames);

            //保存表单归属信息
            workflowBaseService.setBelongInfo(baseForm, userEntity);
            //保存流程信息
//            saveProcessInfo(baseForm, taskInstance);
            processPostStart(baseForm, userEntity);
            SendNotice(taskInstance, userEntity, baseForm);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public final void submit(TaskInstance taskInstance, BaseForm baseForm, String processCode, String route,
                             UserEntity userEntity, List<String> list, Integer operTypeEnumId) throws ServiceException {
        try {
            String accountId = userEntity.getUserName();
            //处理分片id
            if (Constants.IS_SHARDING) {
                Object shardingId = HibernateBeanUtils.getValue(baseForm, Constants.ADB_SHARDING_ID);
                if (shardingId == null || Integer.parseInt(String.valueOf(shardingId)) == 0) {
                    HibernateBeanUtils.setValue(baseForm, Constants.ADB_SHARDING_ID, Integer.parseInt(taskInstance.getShard()));
                }
            }
            //提交可能需要下一步参与者，participants 不能为空
            List<Participant> participants = new ArrayList<Participant>();
            //解析业务信息(主送、抄送、附件、下一步审核人)
            Map<String, Object> relativeData = WorkflowAdapter.getRelativeData(taskInstance.getProcessInstID(), Arrays.asList(Constants.APPLY_ID, Constants.ROOT_DISPATCH_ID, Constants.PARENT_DISPATCH_ID, Constants.DISPATCH_ID), userEntity.getUserName());
            baseForm.setApplyId(relativeData.get(Constants.APPLY_ID) == null ? 0L : Long.valueOf(relativeData.get(Constants.APPLY_ID).toString()));
            baseForm.setRootDisId(relativeData.get(Constants.ROOT_DISPATCH_ID) == null ? 0L : Long.valueOf(relativeData.get(Constants.ROOT_DISPATCH_ID).toString()));
            baseForm.setParentDisId(relativeData.get(Constants.PARENT_DISPATCH_ID) == null ? 0L : Long.valueOf(relativeData.get(Constants.PARENT_DISPATCH_ID).toString()));
            baseForm.setDispatchId(relativeData.get(Constants.DISPATCH_ID) == null ? 0L : Long.valueOf(relativeData.get(Constants.DISPATCH_ID).toString()));
            analyzeForm(baseForm, participants, route);
            //保存业务信息
            beforeSaveProcess(taskInstance, baseForm, participants, route, userEntity);
            saveOrUpdateForm(baseForm, processCode, userEntity, taskInstance);
            Map<String, Object> relaDatas = getRelativeData(baseForm, userEntity);
            //根流程设置分片信息(处理过程会使用)
            if (Constants.IS_SHARDING) {
                relaDatas.put(Constants.SHARDING_ID, HibernateBeanUtils.getValue(baseForm, Constants.ADB_SHARDING_ID));
            }
            relaDatas.put(Constants.ROUTE, route);
//            WFParticipant dispatcher = new WFParticipant();
//            dispatcher.setTypeCode("person");
//            dispatcher.setName(accountId);
//            dispatcher.setId(accountId);
//            relaDatas.put(Constants.SUBPARENTDISPATCHER, dispatcher);
//            relaDatas.put(Constants.SUB_RELATEDDISPATCHID, baseForm.getObjectId().toString());
            WorkflowAdapter.setRelativeData(taskInstance.getProcessInstID(), relaDatas, accountId);
            if (null != route && Constants.ROUTE_START_SUBMIT.equals(route)) {
                workflowBaseService.setNextParticipant(taskInstance.getProcessInstID(), participants, userEntity, list, taskInstance);
            }
            workflowBaseService.submitTask(taskInstance, participants, baseForm, userEntity, list, operTypeEnumId);
            //保存表单归属信息
            workflowBaseService.setBelongInfo(baseForm, userEntity);
            //保存流程信息
//            saveProcessInfo(baseForm, taskInstance);
            processPostStart(baseForm, userEntity);
            SendNotice(taskInstance, userEntity, baseForm);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * 追加子流程前处理
     *
     * @param taskInstance 任务实例
     * @param participants 下一步执行人列表
     * @param entity       业务实体（已知的有TEomApprovalInfoRecord、com.unicom.ucloud.common.model.TEomGenProcessingInfoRec）
     * @param params       参数,目前来源只是建模时配置
     * @param ps           传入子流程参数
     * @throws ServiceException
     */
    public abstract void beforeAddSubProcess(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity,
                                             String params, LinkedHashMap<String, Object> ps) throws ServiceException;


    @Override
    public void saveDraft(BaseForm baseForm, UserEntity userEntity) throws ServiceException {
        baseForm.setDraftFlag(true);
        saveOrUpdateForm(baseForm, userEntity);
    }


    @Override
    public void recordLog(TaskInstance taskInstance, Object entity, UserEntity userEntity) throws ServiceException {

    }

    public void SendNotice(TaskInstance taskInstance, UserEntity userEntity, BaseForm baseForm) {
        final TaskInstance taskInst = taskInstance;
        final UserEntity user = userEntity;
        final BaseForm base = baseForm;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<UserEntity> userEntityList = new ArrayList<UserEntity>();
                    Map map = WorkflowAdapter.getRelativeData(taskInst.getProcessInstID(), Arrays.asList(Constants.NEXT_STEP, Constants.CANDIDATEUSERS), user.getUserName());
                    //turnToDispatch任务派发流程。turnToDispatchDuban任务派发督办任务单。
                    if ("turnToDispatch".equals(taskInst.getProcessModelName()) || "turnToDispatchDuban".equals(taskInst.getProcessModelName())) {
                        //转派是起子流程。需要循环读取子流程发送短信。
                        if (map != null && "转派".equals(map.get(Constants.NEXT_STEP))) {
                            List<ProcessInstance> processInstances = WorkflowAdapter.getSubProcessInstance(user.getUserName(), taskInst.getProcessInstID());
                            if (processInstances != null && processInstances.size() > 0) {
                                for (int i = 0; i < processInstances.size(); i++) {
                                    userEntityList = new ArrayList<UserEntity>();
                                    ProcessInstance processInstance = processInstances.get(i);
                                    Map map1 = WorkflowAdapter.getRelativeData(processInstance.getProcessInstID(), Arrays.asList(Constants.CANDIDATEUSERS), user.getUserName());
                                    String candidateusers = map1.get(Constants.CANDIDATEUSERS).toString();
                                    userEntityList.add(AAAAAdapter.getInstence().findUserByUserName(candidateusers));
                                    TaskInstance t = workflowBaseService.getTaskInstance(processInstance.getProcessInstID(), "");
                                    remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_CREATE, t, userEntityList, user);
//        onTimeSmsContent=您有一条@_@派发的【#_#】任务单，将于【%_%】到期，请通过协同待办或流程管理平台(10.60.59.52:8080账号密码与协同一致)处理，推荐您使用360浏览器极速模式，谢谢!
                                }
                            }
                        } else if (map != null && "退回".equals(map.get(Constants.NEXT_STEP))) {
                            TaskInstance t = workflowBaseService.getTaskInstance(taskInst.getProcessInstID(), "");
                            String candidateusers = map.get(Constants.CANDIDATEUSERS).toString();
                            userEntityList.add(AAAAAdapter.getInstence().findUserByUserName(candidateusers));
                            remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_REJECT, t, userEntityList, user);
                        } else if (map != null && "反馈".equals(map.get(Constants.NEXT_STEP))) {
                            TaskInstance t = workflowBaseService.getTaskInstance(taskInst.getProcessInstID(), "");
                            String candidateusers = map.get(Constants.CANDIDATEUSERS).toString();
                            userEntityList.add(AAAAAdapter.getInstence().findUserByUserName(candidateusers));
                            remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_FDBK, t, userEntityList, user);
                        } else if (map != null && "通过".equals(map.get(Constants.NEXT_STEP))) {
                            ProcessInstance processInstance = WorkflowAdapter.getProcessInstance("", taskInst.getProcessInstID());
                            TaskInstance t = workflowBaseService.getTaskInstance(processInstance.getParentProcessInstID(), "");
                            Map map1 = WorkflowAdapter.getRelativeData(processInstance.getParentProcessInstID(), Arrays.asList(Constants.CANDIDATEUSERS), user.getUserName());
                            String candidateusers = map1.get(Constants.CANDIDATEUSERS).toString();
                            userEntityList.add(AAAAAdapter.getInstence().findUserByUserName(candidateusers));
                            remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_FDBK, t, userEntityList, user);
                        } else if (map != null && "归档".equals(map.get(Constants.NEXT_STEP))) {
                            userEntityList.add(user);
                            remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_COMPLETE, taskInst, userEntityList, user);
                        }
                    }
                    //流程优化建议流程
                    else if ("pmaSuggestionNew".equals(taskInst.getProcessModelName())) {
                        if (map != null && "转派".equals(map.get(Constants.NEXT_STEP))) {
                            List<ProcessInstance> processInstances = WorkflowAdapter.getSubProcessInstance(user.getUserName(), taskInst.getProcessInstID());
                            if (processInstances != null && processInstances.size() > 0) {
                                for (int i = 0; i < processInstances.size(); i++) {
                                    userEntityList = new ArrayList<UserEntity>();
                                    ProcessInstance processInstance = processInstances.get(i);
                                    TaskInstance t = workflowBaseService.getTaskInstance(processInstance.getProcessInstID(), "");
                                    //根流程和当前流程相同，说明是第一级派发
                                    if (taskInst.getProcessInstID().equals(t.getRootProcessInstId())) {
                                        Map map1 = WorkflowAdapter.getRelativeData(processInstance.getProcessInstID(), Arrays.asList(Constants.CANDIDATEUSERS), user.getUserName());
                                        String candidateusers = map1.get(Constants.CANDIDATEUSERS).toString();
                                        userEntityList.add(AAAAAdapter.getInstence().findUserByUserName(candidateusers));
                                        remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_CREATE, t, userEntityList, user);
                                    } else {
                                        Map map1 = WorkflowAdapter.getRelativeData(processInstance.getProcessInstID(), Arrays.asList(Constants.CANDIDATEUSERS), user.getUserName());
                                        String candidateusers = map1.get(Constants.CANDIDATEUSERS).toString();
                                        userEntityList.add(AAAAAdapter.getInstence().findUserByUserName(candidateusers));
//                                        TaskInstance t = workflowBaseService.getTaskInstance(processInstance.getProcessInstID(), "");
                                        remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_TURNTODISPATCH, t, userEntityList, user);
                                    }
                                }
                            }
                        } else {
//                            TaskInstance t = workflowBaseService.getTaskInstance(taskInst.getProcessInstID(), "");
                            Map map1 = WorkflowAdapter.getRelativeData(taskInst.getRootProcessInstId(), Arrays.asList(Constants.CREATE_USER), user.getUserName());
                            String candidateusers = map1.get(Constants.CREATE_USER).toString();
                            userEntityList.add(AAAAAdapter.getInstence().findUserByUserName(candidateusers));
                            remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_DAFU, taskInst, userEntityList, user);
                            ProcessInstance processInstance = WorkflowAdapter.getProcessInstance("", taskInst.getProcessInstID());
                            if (!processInstance.getParentProcessInstID().equals(taskInst.getRootProcessInstId())) {
                                Map map2 = WorkflowAdapter.getRelativeData(processInstance.getParentProcessInstID(), Arrays.asList(Constants.CREATE_USER), user.getUserName());
                                String candidateusers2 = map2.get(Constants.CREATE_USER).toString();
                                userEntityList = new ArrayList<UserEntity>();
                                userEntityList.add(AAAAAdapter.getInstence().findUserByUserName(candidateusers2));
                                remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_FDBK, taskInst, userEntityList, user);
                            }
                        }
                    } else {
                        if (map != null && !"撤销".equals(map.get(Constants.NEXT_STEP))) {
                            String candidateusers = map.get(Constants.CANDIDATEUSERS).toString();
                            userEntityList.add(AAAAAdapter.getInstence().findUserByUserName(candidateusers));
                            TaskInstance t = workflowBaseService.getTaskInstance(taskInst.getProcessInstID(), "");
                            remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_CREATE, t, userEntityList, user);
                        } else {
                            userEntityList.add(user);
                        }
                    }
                    remoteOAForXJ.notice(NotifyInter.NOTICE_TYPE_COMPLETE, taskInst, userEntityList, user);
                } catch (Exception e) {
                    if ("turnToDispatch".equals(taskInst.getProcessModelName())) {
                        logger.error("任务派发工单发送待办消息出错，根流程实例ID：" + taskInst.getProcessInstID() + " 工单ID:" + base.getObjectId());
                    } else {
                        logger.error("流程管理工单发送待办消息出错，根流程实例ID：" + taskInst.getProcessInstID() + " 工单ID:" + base.getObjectId());
                    }
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
