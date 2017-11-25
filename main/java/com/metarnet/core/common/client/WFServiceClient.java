package com.metarnet.core.common.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metarnet.core.common.model.Pager;
import com.metarnet.core.common.workflow.TaskInstance;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by Administrator on 2017/2/6/0006.
 */
public class WFServiceClient {

    private static Logger logger = LogManager.getLogger("WFServiceClient");

    private String wfs_master;

    private static WFServiceClient client = new WFServiceClient();

    public static WFServiceClient getInstance(){
        return client;
    }

    /**
     * 获取流程节点配置信息
     */
    public String getNodeSetting(String processModelName , String activityDefID , String processModelId){
        String getNodeSettingUrl = wfs_master + "/getNodeSetting.do";

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("processModelName", processModelName));
        nvps.add(new BasicNameValuePair("activityDefID", activityDefID));
        if(processModelId != null && !processModelId.startsWith("-")){
            nvps.add(new BasicNameValuePair("processModelId", processModelId));
        }

        return sendHttpRequest(getNodeSettingUrl , nvps);
    }

    /**
     * 获取已办信息
     */
    public Pager getMyCompletedTasks(String accountId , Pager pager){

        String getNodeSettingUrl = wfs_master + "/getMyCompletedTasks.do";

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("accountId", accountId));
        try {
            nvps.add(new BasicNameValuePair("pager", URLEncoder.encode(JSON.toJSONString(pager), "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String returnResult = sendHttpRequest(getNodeSettingUrl , nvps);

        if(!"".equals(returnResult)){
            pager = JSON.parseObject(returnResult , Pager.class);
            List list = pager.getExhibitDatas();
            list = convertTaskInstanceFromJSON(list);
            pager.setExhibitDatas(list);
        }
        return pager;
    }

    /**
     * 更新业务扩展信息
     * 目前只支持修改strcolumn,numcolumn
     */
    public Boolean updateBusiInfoByRoot(String rootProcessInstId , Map dataMap){

        String getNodeSettingUrl = wfs_master + "/updateBusiInfoByRoot.do";

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("rootProcessInstId", rootProcessInstId));

        for(String key : (Set<String>)dataMap.keySet()){
            Object value = dataMap.get(key);
            if(value != null){
                if(key.startsWith("strColumn")){
                    try {
                        nvps.add(new BasicNameValuePair(key, URLEncoder.encode(value.toString(), "UTF-8") ));
                    } catch (UnsupportedEncodingException e) {
                        logger.info("对" + key + "进行UTF8编码报错");
                    }
                } else{
                    nvps.add(new BasicNameValuePair(key, value.toString()));
                }

            }
        }

        String returnResult = sendHttpRequest(getNodeSettingUrl , nvps);

        return Boolean.valueOf(returnResult);
    }

    public String addLog(TaskInstance taskInstance , String formDataId , String operDesc , String formId , Long userId){
        String getNodeSettingUrl = wfs_master + "/addLog.do";

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        try {
            nvps.add(new BasicNameValuePair("taskInstanceStr", URLEncoder.encode(JSON.toJSONString(taskInstance) , "UTF-8")));
        } catch (UnsupportedEncodingException e) {

        }
        nvps.add(new BasicNameValuePair("formType", "4"));
        nvps.add(new BasicNameValuePair("formDataId", formDataId));
        try {
            nvps.add(new BasicNameValuePair("operDesc", URLEncoder.encode(operDesc , "UTF-8")));
        } catch (UnsupportedEncodingException e) {

        }
        nvps.add(new BasicNameValuePair("formId", formId));
        nvps.add(new BasicNameValuePair("userId", userId + ""));

        return sendHttpRequest(getNodeSettingUrl , nvps);
    }

    /**
     * 发送http post请求
     * @param requestUrl
     * @param nvps
     * @return
     */
    private String sendHttpRequest(String requestUrl , List<NameValuePair> nvps){
        String nodeSettingStr = "";

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(requestUrl);

        try {
            post.setEntity(new UrlEncodedFormEntity(nvps));
            HttpResponse response = httpClient.execute(post); //执行GET请求
            HttpEntity entity = response.getEntity();
            if(null != entity){
                nodeSettingStr = EntityUtils.toString(entity, "UTF-8");
                EntityUtils.consume(entity); //Consume response content
            }
        } catch (UnsupportedEncodingException e) {
            logger.info(e.getMessage());
        } catch (ClientProtocolException e) {
            logger.info(e.getMessage());
        } catch (IOException e) {
            logger.info(e.getMessage());
        } finally {
            httpClient.getConnectionManager().shutdown();
        }

        return nodeSettingStr;
    }

    private List<TaskInstance> convertTaskInstanceFromJSON(List<JSONObject> jsonList){
        List<TaskInstance> list = new ArrayList<TaskInstance>();

        for(int i = 0 ; i < jsonList.size() ; i++){
            list.add(convertTaskInstanceFromJSON(jsonList.get(i)));
        }

        return list;
    }

    private TaskInstance convertTaskInstanceFromJSON(JSONObject jsonObject){
        TaskInstance taskInstance = new TaskInstance();

//        taskInstance = JSON.toJavaObject(jsonObject , TaskInstance.class);
        taskInstance.setJobID(jsonObject.getString("jobid"));
        taskInstance.setJobTitle(jsonObject.getString("jobtitle"));
        taskInstance.setJobCode(jsonObject.getString("jobcode"));
        taskInstance.setRootProcessInstId(jsonObject.getString("rootprocessinstid"));
        taskInstance.setActivityDefID(jsonObject.getString("activitydefid"));
        taskInstance.setActivityInstID(jsonObject.getString("activityinstid"));
        taskInstance.setActivityInstName(jsonObject.getString("activityinstname"));
        taskInstance.setAppID(jsonObject.getString("appid"));
        taskInstance.setBusinessId(jsonObject.getString("businessid"));
        taskInstance.setCompletionDate(jsonObject.getDate("completiondate"));
        taskInstance.setCreateDate(jsonObject.getDate("createdate"));
        taskInstance.setCurrentState(jsonObject.getString("currentstate"));
        taskInstance.setDatColumn1(jsonObject.getDate("datcolumn1"));
        taskInstance.setDatColumn2(jsonObject.getDate("datcolumn2"));
        taskInstance.setEndDate(jsonObject.getDate("enddate"));
        taskInstance.setFormURL(jsonObject.getString("formurl"));
        if(jsonObject.getInteger("numcolumn1") != null){
            taskInstance.setNumColumn1(jsonObject.getInteger("numcolumn1"));
        }
        if(jsonObject.getInteger("numcolumn2") != null){
            taskInstance.setNumColumn2(jsonObject.getInteger("numcolumn2"));
        }

        taskInstance.setProcessInstID(jsonObject.getString("processinstid"));
        taskInstance.setProcessModelCNName(jsonObject.getString("processmodelcnname"));
        taskInstance.setProcessModelId(jsonObject.getString("processmodelid"));
        taskInstance.setProcessModelName(jsonObject.getString("processmodelname"));
        taskInstance.setTaskInstID(jsonObject.getString("taskinstid"));
        taskInstance.setStrColumn1(jsonObject.getString("strcolumn1"));
        taskInstance.setStrColumn2(jsonObject.getString("strcolumn2"));
        taskInstance.setStrColumn3(jsonObject.getString("strcolumn3"));
        taskInstance.setStrColumn4(jsonObject.getString("strcolumn4"));
        taskInstance.setStrColumn5(jsonObject.getString("strcolumn5"));
        taskInstance.setStrColumn6(jsonObject.getString("strcolumn6"));
        taskInstance.setStrColumn7(jsonObject.getString("strcolumn7"));


        return taskInstance;
    }


    public void setWfs_master(String wfs_master) {
        this.wfs_master = wfs_master;
    }
}
