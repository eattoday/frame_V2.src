package com.metarnet.core.common.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Administrator on 2017/2/6/0006.
 */
public class WFServiceLoadListener implements ServletContextListener {

    private static Logger logger = LogManager.getLogger("WFServiceLoadListener");

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("init WFServiceLoadListener...");
        Properties pro = new Properties();
        InputStream in = null;
        try {
            in = WFServiceLoadListener.class.getClassLoader().getResourceAsStream("/paas-agent-config.properties");
            pro.load(in);
        } catch (FileNotFoundException e) {
            logger.info("File paas-agent-config.properties not found......");
            logger.info(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    logger.info(e.getMessage());
                }
            }
        }

        String wfs_master = pro.getProperty("wfs_master");
        if(wfs_master == null || "".equals(wfs_master)){
            logger.info("wfs_master in paas-agent-config.properties is null......");
            return;
        }
        if(!wfs_master.toLowerCase().startsWith("http")){
            wfs_master = "http://" + wfs_master;
        }
        WFServiceClient.getInstance().setWfs_master(wfs_master);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
