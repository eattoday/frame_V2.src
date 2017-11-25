package com.metarnet.core.common.utils;

import com.metarnet.core.common.workflow.TaskInstance;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;

import java.util.List;

/**
 * 用于每个工程完成通知统一接口
 * Created by huopf on 2017/8/22.
 */
public interface NotifyInter {
    public static final String NOTICE_TYPE_CREATE = "create"; //发起
    public static final String NOTICE_TYPE_COMPLETE = "complete"; //完成待办
    public static final String NOTICE_TYPE_FDBK = "fdbk"; //反馈
    public static final String NOTICE_TYPE_REJECT = "reject"; //退回
    public static final String NOTICE_TYPE_ONTIME = "onTime"; //工单到期
    public static final String NOTICE_TYPE_TURNTODISPATCH = "turnToDispatch"; //转派 优化建议用到的，转派需要发任务工单。
    public static final String NOTICE_TYPE_DAFU = "dafu"; //答复  优化建议用到的
    /**
     * @param noticeType 通知类型：发起待办(create)和完成待办(complete)
     * @param ti 任务实体
     * @param users 被派发人或者被取消人
     * @param curUser 当前人
     * @return
     */
    public Boolean notice(String noticeType, TaskInstance ti, List<UserEntity> users, UserEntity curUser);
}
