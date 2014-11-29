package com.github.sorhus.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ExecutorService;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobSubmitter implements Runnable {

    private final Queue<Job> jobQueue;
    private final JobExecutionService jobExecutionService;
    private final JobFinaliserService jobFinaliserService;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public JobSubmitter(
        Queue<Job> jobQueue,
        JobExecutionService jobExecutionService,
        JobFinaliserService jobFinaliserService
    ) {
        this.jobQueue = jobQueue;
        this.jobExecutionService = jobExecutionService;
        this.jobFinaliserService = jobFinaliserService;
    }

    @Override
    public void run() {
        while(true) {
            if(!jobQueue.isEmpty()) {
                Job job = jobQueue.poll();
                log.info("Found Job in queue, submitting: {}", job);
                JobExecution jobExecution = jobExecutionService.initialiseJob(job);
                jobFinaliserService.finaliseJob(jobExecution);
            } else {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.warn("Sleep interrupted", e);
                }
            }
        }
    }
}
