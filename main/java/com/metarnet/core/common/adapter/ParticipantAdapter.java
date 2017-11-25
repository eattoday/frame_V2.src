//package com.metarnet.core.common.adapter;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.eos.workflow.omservice.WFParticipant;
//
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
///**
// * Created by Administrator on 2016/7/8/0008.
// */
//public class ParticipantAdapter {
//
//    private static String ORG_CODE = "orgcode";
//    private static String MAJOR_CODE = "majorcode";
//    private static String PRODUCT_CODE = "productcode";
//    private static String AREA_CODE = "areacode";
//    private static String PARTICIPANT = "participant";
//    private static String PARTICIPANT_ID = "participantID";
//    private static String PARTICIPANT_NAME = "participantName";
//    private static String PARTICIPANT_TYPE = "participantType";
//    private static String PARTICIPANT_TYPE_PERSON = "person";
//
////    private static String defaultRemoteParticipantUrl = "http://114.251.172.66/pmos/powerController.do?method=getParticipant";
//    private static String defaultRemoteParticipantUrl = "http://127.0.0.1:9087/PMOS/powerController.do?method=getParticipant";
//
//
//    public static List<WFParticipant> getParticipant(String ruleJSON , String processDefName , String activityDefID , String remoteParticipantUrl) {
//        System.out.println(new Date().toLocaleString() + "\tGoto into com.metarnet.core.common.adapter.ParticipantAdapter.getParticipant...");
//        System.out.println(new Date().toLocaleString() + "\tReceive params:" + ruleJSON + "," + processDefName + "," + activityDefID + "," + remoteParticipantUrl);
//        List<WFParticipant> list = new ArrayList<WFParticipant>();
////        WFParticipant wfParticipant = new WFParticipant();
////        wfParticipant.setId("root-js");
////        wfParticipant.setName("江苏管理员");
////        wfParticipant.setTypeCode("person");
////        list.add(wfParticipant);
//
//        //转换整串JSON
//        JSONObject jsonObject = JSON.parseObject(ruleJSON);
//
//        //获取参与者人员列表
//        JSONArray participant = null;
//        try{
//            participant = jsonObject.getJSONArray(PARTICIPANT);
//        } catch (ClassCastException e){
//            System.out.println(new Date().toLocaleString() + "\t解析参与者人列表出错：\n" + e.getLocalizedMessage());
//        }
//
//        /**
//         * 1、如果参与者人员列表不为空，则返回参与者人员列表
//         * 2、如果参与者人员列表为空，则获取5个维度参数值，调用远程API获取参与者人员列表
//         */
//
//        if(participant == null || participant.size() == 0){
//            if(remoteParticipantUrl == null || "".equals(remoteParticipantUrl)){
//                remoteParticipantUrl = defaultRemoteParticipantUrl;
//            }
//            String participantStr = getParticipantByRemote(ruleJSON , processDefName , activityDefID , remoteParticipantUrl);
//            System.out.println(new Date().toLocaleString() + "\t返回参与者列表:\n" + participantStr);
//            if(participantStr != null){
//                participant = JSON.parseArray(participantStr);
//            }
//        }
//
//        if(participant != null && participant.size() > 0){
//            for(int i = 0 ; i < participant.size() ; i++){
//                JSONObject person = participant.getJSONObject(i);
//                WFParticipant wfParticipant = new WFParticipant();
//                wfParticipant.setId(person.getString(PARTICIPANT_ID));
//                wfParticipant.setName(person.getString(PARTICIPANT_NAME));
//                wfParticipant.setTypeCode(PARTICIPANT_TYPE_PERSON);
//                list.add(wfParticipant);
//            }
//        }
//        return list;
//    }
//
//    private static String getParticipantByRemote(String ruleJSON , String processDefName , String activityDefID , String remoteParticipantUrl){
//        HttpURLConnection httpURLConnection = null;
//        OutputStream out = null; //写
//        InputStream in = null;   //读
//        int httpStatusCode = 0;  //远程主机响应的HTTP状态码
//
//        StringBuffer sendData = new StringBuffer();
//        sendData.append("ruleJSON=");
//        sendData.append(ruleJSON);
//        sendData.append("&");
//        sendData.append("processDefName=");
//        sendData.append(processDefName);
//        sendData.append("&");
//        sendData.append("activityDefID=");
//        sendData.append(activityDefID);
//
//        try{
//            URL sendUrl = new URL(remoteParticipantUrl);
//            httpURLConnection = (HttpURLConnection)sendUrl.openConnection();
//            httpURLConnection.setRequestMethod("POST");
//            httpURLConnection.setDoOutput(true);        //指示应用程序要将数据写入URL连接,其值默认为false
//            httpURLConnection.setUseCaches(false);
//            httpURLConnection.setConnectTimeout(30000); //30秒连接超时
//            httpURLConnection.setReadTimeout(30000);    //30秒读取超时
//
//            out = httpURLConnection.getOutputStream();
//            out.write(sendData.toString().getBytes("UTF-8"));
//
//            //清空缓冲区,发送数据
//            out.flush();
//
//            //获取HTTP状态码
//            httpStatusCode = httpURLConnection.getResponseCode();
//
//            in = httpURLConnection.getInputStream();
////            byte[] byteDatas = new byte[in.available()];
//            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//            int c;
//            while((c = in.read()) >= 0){
//                buffer.write(c);
//            }
//            buffer.close();
//            byte[] byteDatas = buffer.toByteArray();
//            in.read(byteDatas);
//            return new String(byteDatas);
//        }catch(Exception e){
//            System.out.println(e.getMessage());
//            return "Failed`" + httpStatusCode;
//        }finally{
//            if(out != null){
//                try{
//                    out.close();
//                }catch (Exception e){
//                    System.out.println(new Date().toLocaleString() + "\t关闭输出流时发生异常,堆栈信息如下");
//                }
//            }
//            if(in != null){
//                try{
//                    in.close();
//                }catch(Exception e){
//                    System.out.println(new Date().toLocaleString() + "\t关闭输入流时发生异常,堆栈信息如下");
//                }
//            }
//            if(httpURLConnection != null){
//                httpURLConnection.disconnect();
//            }
//        }
//    }
//
//    public static void main(String args[]){
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put(ORG_CODE , "120");
//        jsonObject.put(MAJOR_CODE , "ALL");
//        jsonObject.put(PRODUCT_CODE , "");
//        jsonObject.put(AREA_CODE , "");
//        List participantList = new ArrayList();
//        JSONObject participant = new JSONObject();
//        participant.put(PARTICIPANT_ID , "root");
//        participant.put(PARTICIPANT_NAME , "超级管理员");
//        participant.put(PARTICIPANT_TYPE , "1");
//        participantList.add(participant);
//        jsonObject.put(PARTICIPANT , "");
//
//        String activityDefID = "manualActivity1";
//        String processDefName = "yanshi.test";
//        String remoteParticipantUrl = "http://10.249.6.32:8080/PAOOS/powerController.do?method=getParticipant";
//        getParticipant(jsonObject.toJSONString() , processDefName , activityDefID , null);
//    }
//}
