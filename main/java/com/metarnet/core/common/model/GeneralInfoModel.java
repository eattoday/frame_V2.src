package com.metarnet.core.common.model;

import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Administrator on 2016/4/14.
 */
@Entity
@Table(name = "t_eom_general_info")
@Where(clause = "DELETED_FLAG=0")
@AttributeOverrides({
        @AttributeOverride(name = "objectId", column = @Column(name = "GENERAL_ID", nullable = false))})
public class GeneralInfoModel extends BaseForm {
    private Long processingObjectID;
    private String processingObjectTable;
    private String processingStatus;
    private String report;

    private String formDataId;  //表单数据ID
    private String formId;      //表单ID
    private String formType;    //表单类型
    private String tenantId;    //租户ID

    private String areaName;    //表单域名称

    private String nextStep;    //下一环节
    private String operResult;  //处理结果
    private String nextCandidateUsers; //后续处理人
    private Timestamp openDateTime; //打开时间
//    private String processModelName;    //流程定义名称

    @Column(name = "PROCESSING_OBJECT_ID")
    public Long getProcessingObjectID() {
        return processingObjectID;
    }

    public void setProcessingObjectID(Long processingObjectID) {
        this.processingObjectID = processingObjectID;
    }

    @Column(name = "PROCESSING_OBJECT_TABLE")
    public String getProcessingObjectTable() {
        return processingObjectTable;
    }

    public void setProcessingObjectTable(String processingObjectTable) {
        this.processingObjectTable = processingObjectTable;
    }

    @Column(name = "PROCESSING_STATUS")
    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    @Column(name = "REPORT")
    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    @Column(name = "FORM_DATA_ID")
    public String getFormDataId() {
        return formDataId;
    }

    public void setFormDataId(String formDataId) {
        this.formDataId = formDataId;
    }

    @Column(name = "FORM_ID")
    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    @Column(name = "FORM_TYPE")
    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    @Column(name = "TENANT_ID")
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Column(name = "AREA_NAME")
    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    @Column(name = "NEXT_STEP")
    public String getNextStep() {
        return nextStep;
    }

    public void setNextStep(String nextStep) {
        this.nextStep = nextStep;
    }
    @Column(name = "OPER_RESULT")
    public String getOperResult() {
        return operResult;
    }

    public void setOperResult(String operResult) {
        this.operResult = operResult;
    }

    @Column(name = "NEXT_CANDIDATEUSERS")
    public String getNextCandidateUsers() {
        return nextCandidateUsers;
    }

    public void setNextCandidateUsers(String nextCandidateUsers) {
        this.nextCandidateUsers = nextCandidateUsers;
    }
    @Column(name = "OPEN_DATE_TIME")
    public Timestamp getOpenDateTime() {
        return openDateTime;
    }

    public void setOpenDateTime(Timestamp openDateTime) {
        this.openDateTime = openDateTime;
    }
}
