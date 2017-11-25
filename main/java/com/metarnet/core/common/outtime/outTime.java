package com.metarnet.core.common.outtime;

import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.utils.SpringContextUtils;
import com.metarnet.core.common.workflow.Participant;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import org.apache.log4j.Logger;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: yangjian
 * Date: 16-4-29
 * Time: 上午1:17
 * To change this template use File | Settings | File Templates.
 */


public class outTime {


//    private MetarThreadPoolListener metarThreadPoolListener = (MetarThreadPoolListener) SpringContextUtils.getBean("metarThreadPoolListener");
//
//    private Logger loggertime = Logger.getLogger("Alarm_Time_Log");
//    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    private Boolean startOuttime;
//    private String soonTime;
//    private String outTimeStamp;
//    private String sendSoonSMS;
//    private String sendOutTimeSMS;
//    private String sendOutTimeNSMS;
//    private String appName;
//    private String sql;
//    private Map<String, Integer> soonTimeMap = new HashMap<String, Integer>();
//    private Map<String, Integer> outTimeStampMap = new HashMap<String, Integer>();
//    private Map<String, Boolean> sendSoonSMSMap = new HashMap<String,Boolean>();
//    private Map<String, Boolean> sendOutTimeSMSMap = new HashMap<String,Boolean>();
//    private Map<String, Boolean> sendOutTimeNSMSMap = new HashMap<String,Boolean>();
//    private List<String> senderMsgList =new ArrayList<String>();
//    long time_long = 0;
//
//    public void run() {
//
//
//        try {
//            if (startOuttime) {
//                loggertime.info("启动工单超时----------------");
//                stringToMap();
//                queryData();
//                senderMsgList.clear();
//            }else{
//                loggertime.info("工单超时开关未打开----------------");
//            }
//        } catch (Throwable e) {
//            loggertime.error("程序执行错误", e);
//        }
//
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    public void queryData() {
//        try {
//            String sql = getSql();
//
//            updateData(getConn(sql));
//        } catch (SQLException e) {
//
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//    }
//
//    private List<Map<String, Object>> getConn(String sql) throws SQLException {
//        List<Map<String, Object>> list = null;
//        Statement ps = null;
//        ResultSet rs = null;
//        Connection conn = null;
//        try {
//            conn = SpringContextUtils.getConnection();
//            list = new ArrayList<Map<String, Object>>();
//            ps = conn.createStatement();
//            rs = ps.executeQuery(sql);
//            Map<String, Object> map = null;
//            while (rs.next()) {
//                ResultSetMetaData rsmd = rs.getMetaData();
//                map = new HashMap<String, Object>();
//                String isUpdate = "0";
//                String msgtext = "";
//                if ("1".equals(rs.getString("OUTTIMESTATE"))) {
//                    if (rs.getDate("OUTTIME").before(new Date())) {
//                        map.put("NEWOUTTIMESTATE", "2");
//                        isUpdate = "1";
//                        loggertime.info(rs.getString("WO_CODE") + "工单超时");
//                        if (sendOutTimeSMSMap.get(rs.getString("OUTTIMETYPE").trim())) {
//                            String actName=  getActivename(rs.getString("PROCESSINSTID"),"root");
//                            if (rs.getString("CCSEND") != null && !StringUtil.isEmpty(rs.getString("CCSEND"))) {
//                                String ccmsgtext = getMsgText(rs.getString("WO_CODE"), rs.getString("SUBJECT"), df.format(rs.getDate("OUTTIME")), "3", DateStamp(rs.getDate("OUTTIME"), new Date()),actName);
//                                String ccsendList = getLsitUser(rs.getString("CCSEND"));
//
//                                metarThreadPoolListener.getQueue().put(new SendSmsThreadService().new SheetSateSyn(ccsendList, ccmsgtext, false));
//
//                            }
//                            msgtext = getMsgText(rs.getString("WO_CODE"), rs.getString("SUBJECT"), df.format(rs.getDate("OUTTIME")), "1", DateStamp(rs.getDate("OUTTIME"), new Date()),actName);
//                            if (rs.getString("SENDTOMOBILE") != null && !StringUtil.isEmpty(rs.getString("SENDTOMOBILE"))) {
//                                metarThreadPoolListener.getQueue().put(new SendSmsThreadService().new SheetSateSyn(rs.getString("SENDTOMOBILE"), msgtext, false));
//                            } else {
//                                List<String> parcitipantList = getParcitipant(rs.getString("PROCESSINSTID"));
//                                if (parcitipantList != null && parcitipantList.size() > 0) {
//                                    for (String userName : parcitipantList) {
//
//                                        if(!senderMsgList.contains(rs.getString("WO_CODE")+userName)){
//                                            metarThreadPoolListener.getQueue().put(new SendSmsThreadService().new SheetSateSyn(userName, msgtext, false));
//                                            senderMsgList.add(rs.getString("WO_CODE")+userName);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                } else if ("2".equals(rs.getString("OUTTIMESTATE"))) {
//                    // loggertime.info(rs.getString("WO_CODE") + "已经超时" + DateStamp( rs.getDate("OUTTIME"),new java.util.Date())  + "分钟");
///*                    Date outDate = rs.getDate("OUTTIME");
//                    if (rs.getDate("OUTTIMESENDTIME") != null) {
//                        outDate = rs.getDate("OUTTIMESENDTIME");
//                    }
//                    if (OutTimeAddtimeStamp(outDate, outTimeStampMap.get(rs.getString("OUTTIMETYPE").trim())).before(new Date())) {
//                        isUpdate = "1";
//                        map.put("NEWOUTTIMESTATE", "2");
//                        if (sendOutTimeNSMSMap.get(rs.getString("OUTTIMETYPE").trim())) {
//                            if (rs.getString("CCSEND") != null && !StringUtil.isEmpty(rs.getString("CCSEND"))) {
//                                String ccmsgtext = getMsgText(rs.getString("WO_CODE"), rs.getString("SUBJECT"), df.format(rs.getDate("OUTTIME")), "3", DateStamp(rs.getDate("OUTTIME"), new Date()));
//                                String ccsendList = getLsitUser(rs.getString("CCSEND"));
//                                metarThreadPoolListener.getQueue().put(new SendSmsThreadService().new SheetSateSyn(ccsendList, ccmsgtext, false));
//
//                            }
//                            msgtext = getMsgText(rs.getString("WO_CODE"), rs.getString("SUBJECT"), df.format(rs.getDate("OUTTIME")), "2", DateStamp(rs.getDate("OUTTIME"), new Date()));
//                            if (rs.getString("SENDTOMOBILE") != null && !StringUtil.isEmpty(rs.getString("SENDTOMOBILE"))) {
//                                metarThreadPoolListener.getQueue().put(new SendSmsThreadService().new SheetSateSyn(rs.getString("SENDTOMOBILE"), msgtext, false));
//                            } else {
//                                List<String> parcitipantList = getParcitipant(rs.getString("PROCESSINSTID"));
//                                if (parcitipantList != null && parcitipantList.size() > 0) {
//                                    for (String userName : parcitipantList) {
//                                        if(!senderMsgList.contains(rs.getString("WO_CODE")+userName)){
//                                            metarThreadPoolListener.getQueue().put(new SendSmsThreadService().new SheetSateSyn(userName, msgtext, false));
//                                            senderMsgList.add(rs.getString("WO_CODE")+userName);
//                                        }
//
//                                    }
//                                }
//                            }
//                        }
//                    }*/
//                } else {
//                    if (rs.getDate("OUTTIME") != null && soonOutTime(rs.getDate("OUTTIME"), soonTimeMap.get(rs.getString("OUTTIMETYPE").trim())).before(new Date())) {
//                        isUpdate = "1";
//                        map.put("NEWOUTTIMESTATE", "1");
//                        loggertime.info(rs.getString("WO_CODE") + "工单即将超时");
//                        if (sendSoonSMSMap.get(rs.getString("OUTTIMETYPE").trim())) {
//                            String actName=  getActivename(rs.getString("PROCESSINSTID"),"root");
//                            msgtext = getMsgText(rs.getString("WO_CODE"), rs.getString("SUBJECT"), df.format(rs.getDate("OUTTIME")), "0", "",actName);
//                            if (rs.getString("SENDTOMOBILE") != null && !StringUtil.isEmpty(rs.getString("SENDTOMOBILE"))) {
//                                metarThreadPoolListener.getQueue().put(new SendSmsThreadService().new SheetSateSyn(rs.getString("SENDTOMOBILE"), msgtext, false));
//                            } else {
//                                List<String> parcitipantList = getParcitipant(rs.getString("PROCESSINSTID"));
//                                if (parcitipantList != null && parcitipantList.size() > 0) {
//                                    for (String userName : parcitipantList) {
//                                        if(!senderMsgList.contains(rs.getString("WO_CODE")+userName)){
//                                            metarThreadPoolListener.getQueue().put(new SendSmsThreadService().new SheetSateSyn(userName, msgtext, false));
//                                            senderMsgList.add(rs.getString("WO_CODE")+userName);
//                                        }
//
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                if ("1".equals(isUpdate)) {
//                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
//                        String columnName = rsmd.getColumnName(i);
//                        Object columnValue = rs.getString(columnName);
//                        map.put(columnName, columnValue);
//                    }
//                    list.add(map);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (rs != null) rs.close();
//                if (ps != null) ps.close();
//                if (conn != null) conn.close();
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
//        }
//
//        return list;
//    }
//
//    private String getMsgText(String wo_code, String subject, String reqTime, String msgtype, String timestamp,String actName) {
//        StringBuffer sb = new StringBuffer();
//        if ("0".equals(msgtype)) {
//            sb.append(" 您有一条待办工单即将超时，当前所处环节："+actName+"，请及时处理。【" + appName + "】工单主题：");
//            sb.append(subject);
//            sb.append("，工单编号：" + wo_code);
//            sb.append("。要求完成时间：[" + reqTime);
//            sb.append("]【沃运维】");
//        } else if ("1".equals(msgtype)) {
//            sb.append("  您有一条待办工单已经超时[" + timestamp + "分钟]，当前所处环节："+actName+"，请及时处理。【" + appName + "】工单主题：");
//            sb.append(subject);
//            sb.append("，工单编号：" + wo_code);
//            sb.append("。要求完成时间：[" + reqTime);
//            sb.append("]【沃运维】");
//        } else if ("2".equals(msgtype)) {
//            sb.append("  您有一条待办工单已经超时[" + timestamp + "分钟]，当前所处环节："+actName+"，请及时处理。【" + appName + "】工单主题：");
//            sb.append(subject);
//            sb.append("，工单编号：" + wo_code);
//            sb.append("。要求完成时间：[" + reqTime);
//            sb.append("]【沃运维】");
//        } else if ("3".equals(msgtype)) {
//            sb.append(" 您有一条故障工单[抄送]已经超时，当前所处环节："+actName+"，请安排人员及时处理。【" + appName + "】工单主题：");
//            sb.append(subject);
//            sb.append("，工单编号：" + wo_code);
//            sb.append("。要求完成时间：[" + reqTime);
//            sb.append("]【沃运维】");
//        }
//        // loggertime.info(sb.toString());
//        return sb.toString();
//    }
//
//
//    private Integer updateData(List<Map<String, Object>> maps) {
//        Connection conn = SpringContextUtils.getConnection();
//
//        Statement stmt = null;
//        try {
//            conn.setAutoCommit(false);
//            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//            int updatecount = 0;
//            for (int x = 0; x < maps.size(); x++) {
//                stmt.addBatch("update " + maps.get(x).get("TABLENAME") + " set " + maps.get(x).get("UPDATESTATECOLUMN") + "='" +
//                        maps.get(x).get("NEWOUTTIMESTATE") + "'," + maps.get(x).get("UPDATELASTSENDSMSCOLIMN") + "='" +
//                        df.format(new Date()) + "' where " + maps.get(x).get("UPDATEOBJECTCOLIMN") + "=" + maps.get(x).get("OBJECTID"));
//                if (x % 1000 == 0) {
//                    stmt.executeBatch();
//                    conn.commit();
//                    stmt.clearBatch();
//
//                }
//            }
//
//            stmt.executeBatch();
//            conn.commit();
//
//
//            loggertime.info("入库完成入库数量" + maps.size());
//        } catch (SQLException ex) {
//            loggertime.info(ex);
//        } finally {
//            try {
//                if (stmt != null) stmt.close();
//                if (conn != null) conn.close();
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//    private Date soonOutTime(Date time, Integer minuteNum) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(time);
//        calendar.add(Calendar.MINUTE, -minuteNum);
//        return calendar.getTime();
//    }
//
//    private Date OutTimeAddtimeStamp(Date time, Integer minuteNum) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(time);
//        calendar.add(Calendar.MINUTE, minuteNum);
////        loggertime.info(df.format(calendar.getTime()));
//        return calendar.getTime();
//
//    }
//
//    private String DateStamp(Date StartTime, Date EndTime) {
//        long between = (EndTime.getTime() - StartTime.getTime()) / 1000;//除以1000是为了转换成秒
//        return String.valueOf(between / 60);
//      /*  long day1 = between / (24 * 3600);
//        long hour1 = between % (24 * 3600) / 3600;
//        long minute1 = between % 3600 / 60;
//        long second1 = between % 60 / 60;
//        System.out.println("" + day1 + "天" + hour1 + "小时" + minute1 + "分" + second1 + "秒");
//        return "" + day1 + "天" + hour1 + "小时" + minute1 + "分" + second1 + "秒";*/
//    }
//
//    private List<String> getParcitipant(String processInstId) {
//        try {
//
//            List<Participant> ParticipantList = null;
//            if (processInstId != null) {
//                time_long = new Date().getTime();
//                ParticipantList = WorkflowAdapter.findDoingParticipant(processInstId, "root");
//                // loggertime.info("findDoingParticipant" + (new Date().getTime() - time_long));
//            }
//            List<String> addresList = new ArrayList<String>();
//            if (ParticipantList != null && ParticipantList.size() > 0) {
//                for (Participant par : ParticipantList) {
//                    if (par.getParticipantID() != null && !StringUtil.isEmpty(par.getParticipantID())) {
//                        time_long = new Date().getTime();
//
//                        UserEntity userEntity =  AAAAAdapter.findUserByPortalAccountId(par.getParticipantID());
//                        //   loggertime.info("findUserByPortalAccountId" + (new Date().getTime() - time_long));
//                        if (userEntity != null && userEntity.getMobilePhone() != null && !StringUtil.isEmpty(userEntity.getMobilePhone())) {
//                            addresList.add(userEntity.getMobilePhone());
//                        }
//
//                    }
//                }
//                return addresList;
//            }
//        } catch (AdapterException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }catch (PaasAAAAException pa){
//            pa.printStackTrace();
//        }
//        return null;
//    }
//
//    private String getLsitUser(String users) {
//
//        try {
//            if (users != null && !StringUtil.isEmpty(users)) {
//                List<UserEntity> userlsit = AAAAAdapter.getInstence().findUserListByUserNames(Arrays.asList(users.split(",")));
//                String result = "";
//                for (UserEntity entity : userlsit) {
//                    result += entity.getMobilePhone() + ",";
//                }
//                result = result.substring(0, result.length() - 1);
//                return result;
//            }
//        } catch (PaasAAAAException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        return null;
//    }
//
//    private void stringToMap() throws Exception {
//        try {
//            if (!StringUtil.isEmpty(soonTime)) {
//                for (String str : soonTime.split(",")) {
//
//                    soonTimeMap.put(str.split(":")[0].trim(), Integer.parseInt(str.split(":")[1].trim()));
//                }
//            }
//        } catch (Exception e) {
//            throw new Exception("soonTime配置错误，请按照对应格式配置如 soonTime=accept:5,resolve:30,orgfdbk:1440");
//
//        }
//        try {
//            if (!StringUtil.isEmpty(outTimeStamp)) {
//                for (String str : outTimeStamp.split(",")) {
//
//                    outTimeStampMap.put(str.split(":")[0].trim(), Integer.parseInt(str.split(":")[1].trim()));
//                }
//            }
//        } catch (Exception e) {
//            throw new Exception("outTimeStamp配置错误，请按照对应格式配置如 outTimeStamp =accept:30,resolve:30,orgfdbk:30");
//
//        }
//
//        try {
//            if (!StringUtil.isEmpty(sendSoonSMS)) {
//
//                for (String str : sendSoonSMS.split(",")) {
//
//                    sendSoonSMSMap.put(str.split(":")[0].trim(), Boolean.parseBoolean(str.split(":")[1].trim()));
//                }
//            }
//        } catch (Exception e) {
//            throw new Exception("sendSoonSMS配置错误，请按照对应格式配置如 sendSoonSMS=accept:false,resolve:false,orgfdbk:false");
//
//        }
//
//
//        try {
//            if (!StringUtil.isEmpty(sendOutTimeSMS)) {
//                for (String str : sendOutTimeSMS.split(",")) {
//
//                    sendOutTimeSMSMap.put(str.split(":")[0].trim(), Boolean.parseBoolean(str.split(":")[1].trim()));
//                }
//            }
//        } catch (Exception e) {
//            throw new Exception("sendOutTimeSMS配置错误，请按照对应格式配置如 sendOutTimeSMS =accept:false,resolve:false,orgfdbk:false");
//
//        }
//
//
//        try {
//            if (!StringUtil.isEmpty(sendOutTimeNSMS)) {
//                for (String str : sendOutTimeNSMS.split(",")) {
//
//                    sendOutTimeNSMSMap.put(str.split(":")[0].trim(), Boolean.parseBoolean(str.split(":")[1].trim()));
//                }
//            }
//        } catch (Exception e) {
//            throw new Exception("sendOutTimeNSMS配置错误，请按照对应格式配置如  sendOutTimeNSMS =accept:true,resolve:true,orgfdbk:true");
//
//        }
//    }
//    private String getActivename(String proId,String user)  {
//        List<WFActivityInst> wfActivityInsts=  WorkflowAdapter.queryActivityInstsByProcessInstID(Long.parseLong(proId), null);
//        if(wfActivityInsts!=null&&wfActivityInsts.size()>0)  {
//            for(WFActivityInst act:wfActivityInsts)  {
//                if(act.getCurrentState()==2){
//                    return  act.getActivityInstName();
//                }
//            }
//        }
//        return  null;
//    }
//    public MetarThreadPoolListener getMetarThreadPoolListener() {
//        return metarThreadPoolListener;
//    }
//
//    public void setMetarThreadPoolListener(MetarThreadPoolListener metarThreadPoolListener) {
//        this.metarThreadPoolListener = metarThreadPoolListener;
//    }
//
//    public String getSendSoonSMS() {
//        return sendSoonSMS;
//    }
//
//    public void setSendSoonSMS(String sendSoonSMS) {
//        this.sendSoonSMS = sendSoonSMS;
//    }
//
//    public String getSendOutTimeSMS() {
//        return sendOutTimeSMS;
//    }
//
//    public void setSendOutTimeSMS(String sendOutTimeSMS) {
//        this.sendOutTimeSMS = sendOutTimeSMS;
//    }
//
//    public String getSendOutTimeNSMS() {
//        return sendOutTimeNSMS;
//    }
//
//    public void setSendOutTimeNSMS(String sendOutTimeNSMS) {
//        this.sendOutTimeNSMS = sendOutTimeNSMS;
//    }
//
//    public String getSoonTime() {
//        return soonTime;
//    }
//
//    public void setSoonTime(String soonTime) {
//        this.soonTime = soonTime;
//    }
//
//    public String getOutTimeStamp() {
//        return outTimeStamp;
//    }
//
//    public void setOutTimeStamp(String outTimeStamp) {
//        this.outTimeStamp = outTimeStamp;
//    }
//
//    public String getAppName() {
//        return appName;
//    }
//
//    public void setAppName(String appName) {
//        this.appName = appName;
//    }
//
//    public String getSql() {
//        return sql;
//    }
//
//    public void setSql(String sql) {
//        this.sql = sql;
//    }
//
//    public Boolean getStartOuttime() {
//        return startOuttime;
//    }
//
//    public void setStartOuttime(Boolean startOuttime) {
//        this.startOuttime = startOuttime;
//    }
}
