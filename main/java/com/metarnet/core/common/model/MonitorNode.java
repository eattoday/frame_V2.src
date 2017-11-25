package com.metarnet.core.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/14/0014.
 */
public class MonitorNode {

    private String id;

    private String label;

    private List<MonitorNode> preNodes = new ArrayList<MonitorNode>();

    private List<MonitorNode> nextNodes = new ArrayList<MonitorNode>();

//    private MonitorNode rightNode;

    private int positionX;

    private int positionY;

    private String orgName;

    private Integer orgId;

    private String processID;

    private String parentProcessID;

    private String state;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<MonitorNode> getPreNodes() {
        return preNodes;
    }

    public void setPreNodes(List<MonitorNode> preNodes) {
        this.preNodes = preNodes;
    }

    public List<MonitorNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<MonitorNode> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public String getProcessID() {
        return processID;
    }

    public void setProcessID(String processID) {
        this.processID = processID;
    }

    public String getParentProcessID() {
        return parentProcessID;
    }

    public void setParentProcessID(String parentProcessID) {
        this.parentProcessID = parentProcessID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

//    public MonitorNode getRightNode() {
//        return rightNode;
//    }
//
//    public void setRightNode(MonitorNode rightNode) {
//        this.rightNode = rightNode;
//    }

    public void movePositionXCascade(int moveX){
        this.positionX = positionX + moveX;
        for(MonitorNode pre : preNodes){
            pre.movePositionXCascade(moveX);
        }
    }
}
