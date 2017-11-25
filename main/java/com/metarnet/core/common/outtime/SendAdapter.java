package com.metarnet.core.common.outtime;

import com.alibaba.fastjson.JSONArray;

import com.metarnet.core.common.utils.Constants;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: yyou
 * Date: 16-1-4
 * Time: 下午2:39
 * To change this template use File | Settings | File Templates.
 */

//@Service
public class SendAdapter {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("sms");
    // ConnectionFactory ：连接工厂，JMS 用它创建连接
    static ConnectionFactory connectionFactory;
    // Connection ：JMS 客户端到JMS Provider 的连接
    static Connection connection;

    // Destination ：消息的目的地;消息发送给谁.
    static Destination destination;

    // Session： 一个发送或接收消息的线程
    static Session session;
   static String queueName = "MsgSendQueue";
    // MessageProducer：消息发送者
    private static MessageProducer producer;

    static {

        // TextMessage message;
        // 构造ConnectionFactory实例对象，此处采用ActiveMq的实现jar
        connectionFactory = new ActiveMQConnectionFactory(
                ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD,
                Constants.MQ_CONN_URL);
        try {
            // 构造从工厂得到连接对象
            connection = connectionFactory.createConnection();
            // 启动
            connection.start();
            // 获取操作连接
            session = connection.createSession(Boolean.TRUE , Session.AUTO_ACKNOWLEDGE);
            // 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }

    public SendAdapter(){
        try {
           // String queueName = "MsgSendQueue"; //TODO_MSG_NOTICE//MsgSendQueue
            destination = session.createQueue(queueName);
            producer = session.createProducer(destination);
            // 设置不持久化，此处学习，实际根据项目决定
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void closeSession(){
        try {
            if (null != connection)
                connection.close();
        } catch (Throwable ignore) {
        }
    }

    public static void initSession()
    {
        try
        {
            if (connection == null)
            {
                 connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD,  Constants.MQ_CONN_URL);
            }
            connection = connectionFactory.createConnection();

            connection.start();

            session = connection.createSession(Boolean.TRUE.booleanValue(), 1);

            destination = session.createQueue(queueName);
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
    }

    public static void initProducer()
    {
        try
        {
            producer = session.createProducer(destination);
            producer.setDeliveryMode(1);
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
    }
    /**
     * 发送MQ消息方法
     *
     * 注：要将消息体写成JSON
     */
    public static void sendMessage(String[] address,String msg){
        log.info("直发短信："+msg);
        TextMessage message = null;
        //拼接json串
        Map map = new HashMap<String, Object>();

        map.put("orgSystem","电子运维");
        map.put("appId",null); //系统标识
        map.put("msgKey",new Date().getTime());
        map.put("msgType","SMS");
        map.put("msgText",msg);
        map.put("msgAddress",address);
        map.put("msgLevel","MINOR");


        String messText = JSONArray.toJSONString(map);
        log.info("直发短信messText---："+messText);
        try
        {
            System.out.println("================================================ session is transacted" + session.getTransacted());
            if (session == null)
            {
                System.out.println("________________________ session id null");
                initSession();
            }
            System.out.println("________________________ session:" + session);
            message = session.createTextMessage(messText);
            if (producer == null)
            {
                System.out.println("------------------------------------------producer id null");
                initProducer();
            }
            producer.send(message);
            session.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("...........................mq  restart....");
            initSession();
            initProducer();
        }

    }
    /**
     * 发送MQ消息方法过滤模式Appid不为空
     *
     * 注：要将消息体写成JSON
     */
    public static void sendMessageFilter(String address,String msg){
        log.info("过滤短信："+msg);
        TextMessage message = null;
        //拼接json串
        Map map = new HashMap<String, Object>();

        map.put("orgSystem","电子运维");
        map.put("appId", Constants.MODEL_NAME); //系统标识
        map.put("msgKey",new Date().getTime());
        map.put("msgType","PERSON_SETTINGS");
        map.put("msgText",msg);
        map.put("msgAddress",address);
        map.put("msgLevel","MINOR");

        String messText = JSONArray.toJSONString(map);
        log.info("过滤短信messText---："+messText);
        try
        {
            System.out.println("================================================ session is transacted" + session.getTransacted());
            if (session == null)
            {
                System.out.println("________________________ session id null");
                initSession();
            }
            System.out.println("________________________ session:" + session);
            message = session.createTextMessage(messText);
            if (producer == null)
            {
                System.out.println("------------------------------------------producer id null");
                initProducer();
            }
            producer.send(message);
            session.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("...........................mq  restart....");
            initSession();
            initProducer();
        }

    }

}
