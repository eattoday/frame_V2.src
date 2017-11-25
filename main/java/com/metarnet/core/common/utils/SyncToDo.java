package com.metarnet.core.common.utils;

import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.workflow.Participant;
import com.metarnet.core.common.workflow.TaskInstance;
import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangjian on 2017/2/16.
 */
public class SyncToDo implements Runnable {
    private Logger logger = Logger.getLogger(this.getClass());
    private String userName;
    private TaskInstance taskInstance;
    private String modifyType;
    private boolean flag=true;

    public SyncToDo(String accountId, TaskInstance taskInstance, String modifyType) {
        this.userName = accountId;
        this.taskInstance = taskInstance;
        this.modifyType=modifyType;
    }

    @Override
    public void run() {
        try {
            if(flag==false){
                while (true){
                    if(flag==true){
                        sendData(modifyType);
                        break;
                    }
                }
            }else{
                sendData(modifyType);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendData(String modifyType) {
        try {
            flag=false;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<Participant> participantList = WorkflowAdapter.findDoingParticipant(taskInstance.getProcessInstID(), userName);
            String user = "";
            if(participantList!=null||participantList.size()>0) {
                for (Participant participant : participantList) {
                    user = participant.getParticipantID() + ",";
                }
                if (user.endsWith(",")) {
                    user = user.substring(0, user.length() - 1);
                }

                Map<String, String> map = new HashMap<String, String>();
                map.put("woCode", taskInstance.getJobCode());
                map.put("userName", user);
                map.put("workType", Constants.MODEL_CODE);
                map.put("theme", taskInstance.getJobTitle());
                map.put("requreTime", df.format(taskInstance.getDatColumn1()));
                map.put("params", "");
                String url = "http://132.175.71.39:7015/demand/pageBuild.do?method=build&fromPage=todo&type=waiting&buildMethod=build" +
                        "&processInstID=" + taskInstance.getProcessInstID() +
                        "&processModelId=" + taskInstance.getProcessModelId() +
                        "&processModelName=" + taskInstance.getProcessModelName() +
                        "&activityInstID=" + taskInstance.getActivityInstID() +
                        "&activityDefID=" + taskInstance.getActivityDefID() +
                        "&taskInstID=" + taskInstance.getTaskInstID() +
                        "&activityInstName=" + taskInstance.getActivityInstName() +
                        "&jobTitle=" + taskInstance.getJobTitle() +
                        "&jobCode=" + taskInstance.getJobCode() +
                        "&jobID=" + taskInstance.getJobID() +
                        "&appID=" + taskInstance.getAppID() +
                        "&shard=" + taskInstance.getShard() +
                        "&businessId=" + taskInstance.getBusinessId() +
                        "&rootProcessInstId=" + taskInstance.getRootProcessInstId() +
                        "&createDate=" + taskInstance.getCreateDate() +
                        "&taskWarning=" + taskInstance.getWarningDate() +
                        "&strColumn4=" + taskInstance.getStrColumn4() +
                        "&__returnUrl=/base/frame/todo.jsp";
                map.put("infoUrl", url);
                map.put("modifType", modifyType);
                map.put("processInstId", taskInstance.getProcessInstID());
                HttpClientUtil.sendPostRequestByJava("http://132.175.71.39:7015/EOM_TD/doWaitController.do?method=modifyTodo", map);
                Thread.sleep(200);
                flag=true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public TaskInstance getTaskInstance() {
        return taskInstance;
    }

    public void setTaskInstance(TaskInstance taskInstance) {
        this.taskInstance = taskInstance;
    }


    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getModifyType() {
        return modifyType;
    }

    public void setModifyType(String modifyType) {
        this.modifyType = modifyType;
    }
}
