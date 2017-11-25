package com.metarnet.core.common.outtime;

import org.apache.log4j.Logger;

/**
 * Created by zzc on 2014/7/11.
 */
public class SendSmsThreadService {
    public static Logger log = Logger.getLogger(SendSmsThreadService.class);


    public class SheetSateSyn  implements Runnable {
        private String Form;
        private  String msg;
        private  boolean isFilter;
        public SheetSateSyn(String address, String msg,boolean isFilter){
            this.Form=address;
            this.msg=msg;
            this.isFilter=isFilter;
        }
        public void run() {
            try {
                if(isFilter){
                 //   log.info("需要过滤短信address:"+Form+"    MSGTEXT ："+msg);
                new SendAdapter().sendMessageFilter(Form,msg);
                }else{
                 //   log.info("不需要过滤短信address:"+Form+"    MSGTEXT ："+msg);
                    new SendAdapter().sendMessage(new String[]{Form},msg);
                }
            } catch (Exception e) {
                log.error("--SendSmsThreadService--SheetSateSyn--run--报错--\n"+e);
            }
        }
    }



}
