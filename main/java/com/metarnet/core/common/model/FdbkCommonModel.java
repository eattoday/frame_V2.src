package com.metarnet.core.common.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.sql.Timestamp;

/**
 * Created with IntelliJ IDEA.
 * User: wangzwty
 * Date: 16-3-31
 * Time: 下午4:26
 * 反馈单
 */
@MappedSuperclass
@AttributeOverride(name = "objectId", column = @Column(name = "FEEDBACK_FORM_ID"))
public abstract class FdbkCommonModel extends BaseForm {

    private Boolean isTimeout;

    private String disAssignObjectId;

    private String disAssignObjectName;

    private String disAssignObjectType;

    private Long issueUserId;    //派发人
    private String issueUserTrueName;    //派发人
    private Timestamp issueTime;    //派发时间

    private String auditReason; //驳回原因

    @Column(name = "TIME_OUT")
    public Boolean getIsTimeout() {
        return isTimeout;
    }

    public void setIsTimeout(Boolean isTimeout) {
        this.isTimeout = isTimeout;
    }

    @Column(name = "DIS_ASSIGN_OBJECT_ID")
    public String getDisAssignObjectId() {
        return disAssignObjectId;
    }

    public void setDisAssignObjectId(String disAssignObjectId) {
        this.disAssignObjectId = disAssignObjectId;
    }

    @Column(name = "DIS_ASSIGN_OBJECT_NAME")
    public String getDisAssignObjectName() {
        return disAssignObjectName;
    }

    public void setDisAssignObjectName(String disAssignObjectName) {
        this.disAssignObjectName = disAssignObjectName;
    }

    @Column(name = "DIS_ASSIGN_OBJECT_TYPE")
    public String getDisAssignObjectType() {
        return disAssignObjectType;
    }

    public void setDisAssignObjectType(String disAssignObjectType) {
        this.disAssignObjectType = disAssignObjectType;
    }

    public Long getIssueUserId() {
        return issueUserId;
    }

    public void setIssueUserId(Long issueUserId) {
        this.issueUserId = issueUserId;
    }

    public String getIssueUserTrueName() {
        return issueUserTrueName;
    }

    public void setIssueUserTrueName(String issueUserTrueName) {
        this.issueUserTrueName = issueUserTrueName;
    }

    public Timestamp getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Timestamp issueTime) {
        this.issueTime = issueTime;
    }


    public String getAuditReason() {
        return auditReason;
    }

    public void setAuditReason(String auditReason) {
        this.auditReason = auditReason;
    }
}
