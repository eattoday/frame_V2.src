package com.metarnet.core.common.outtime;


import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service("metarThreadPoolListener")
public class MetarThreadPoolListener extends Thread {


    private int corePoolSize = 5;

    private int maximumPoolSize = 10;

    private int keepAliveTime=2000;

	private BlockingQueue<Runnable> queue = null;
	private BlockingQueue<Runnable> queueThreadPool = null;
	private MetarThreadPoolExecutor pool = null;

	@Override
	public void run() {
		while (true) {
			try {
				Runnable handle=queue.take();
				Thread.sleep(0);
				pool.submit(handle);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public BlockingQueue<Runnable> getQueue() {
		return queue;
	}

	public void setQueue(BlockingQueue<Runnable> queue) {
		this.queue = queue;
	}

    @PostConstruct
	public void Initialization() {
		queue = new LinkedBlockingQueue<Runnable>();
		queueThreadPool = new LinkedBlockingQueue<Runnable>();
		pool = new MetarThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
				TimeUnit.MILLISECONDS, queueThreadPool);

        this.start();

	}

}
