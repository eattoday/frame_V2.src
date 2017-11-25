package com.metarnet.core.common.service.impl;

import com.metarnet.core.common.utils.NotifyInter;
import com.metarnet.core.common.workflow.TaskInstance;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Administrator on 10月25日, 0025.
 */
@Service
public class NotifyInterImpl implements NotifyInter {
    @Override
    public Boolean notice(String noticeType, TaskInstance ti, List<UserEntity> users, UserEntity curUser) {
        return null;
    }
}
