package com.metarnet.core.common.outtime;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.concurrent.*;

/**
 * Created by hlguo on 2014/7/22.
 */
public class MetarThreadPoolExecutor extends ThreadPoolExecutor {

    private Logger logger = Logger.getLogger("pool");

    private ConcurrentHashMap<String, Date> startTimes;

    public MetarThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                   long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        startTimes = new ConcurrentHashMap<String, Date>();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
       // logger.info("Task:"+r.getClass().toString()+" is beginning.");
        startTimes.put(String.valueOf(r.hashCode()), new Date());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if(!r.getClass().isAssignableFrom(Callable.class)){
            try {

                Date startDate = startTimes.remove(String.valueOf(r.hashCode()));
                Date finishDate = new Date();
                long diff = finishDate.getTime() - startDate.getTime();

//                logger.info("Task:"+r.getClass().toString()+" is finishing. Duration:"+diff +" ms");
//                logger.info("Executed tasks:"+getCompletedTaskCount());
//                logger.info("Running tasks:"+getActiveCount());
//                logger.info("Pending tasks:"+getQueue().size());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
    }
}
