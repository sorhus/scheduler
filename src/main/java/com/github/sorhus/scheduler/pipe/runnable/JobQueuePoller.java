package com.github.sorhus.scheduler.pipe.runnable;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.pipe.control.PipeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ExecutorService;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobQueuePoller implements Runnable {

    private final PipeControl pipeControl;
    private final Queue<Job> jobQueue;
    private final JobExecutionFactory jobExecutionFactory;
    private final ExecutorService jobExecutorService;
    private final ExecutorService logExecutorService;
    private final int sleep;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public JobQueuePoller(
        PipeControl pipeControl,
        Queue<Job> jobQueue,
        ExecutorService jobExecutorService,
        ExecutorService logExecutorService,
        JobExecutionFactory jobExecutionFactory,
        int sleep
    ) {

        this.pipeControl = pipeControl;
        this.jobQueue = jobQueue;
        this.jobExecutionFactory = jobExecutionFactory;
        this.jobExecutorService = jobExecutorService;
        this.logExecutorService = logExecutorService;
        this.sleep = sleep;
    }

    @Override
    public void run() {
        while(pipeControl.run()) {
            if(!jobQueue.isEmpty()) {
                Job job = jobQueue.poll();
                if(null != job) {
                    log.info("Found Job in queue, submitting: {}", job);
                    JobExecution jobExecution = jobExecutionFactory.getJobExecution(job);
                    logExecutorService.submit(jobExecution.getJobLogger());
                    jobExecutorService.submit(jobExecution);
                }
            } else {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    log.warn("Sleep interrupted", e);
                }
            }
        }
    }
}
