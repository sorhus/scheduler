package com.github.sorhus.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobSubmitter implements Runnable {

    private final Queue<Job> jobQueue;
    private final JobExecutionService jobExecutionService;
    private final JobFinaliserService jobFinaliserService;
    private final AtomicInteger jobCounter;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public JobSubmitter(
        Queue<Job> jobQueue,
        JobExecutionService jobExecutionService,
        JobFinaliserService jobFinaliserService,
        AtomicInteger jobCounter
    ) {
        this.jobQueue = jobQueue;
        this.jobExecutionService = jobExecutionService;
        this.jobFinaliserService = jobFinaliserService;
        this.jobCounter = jobCounter;
    }

    @Override
    public void run() {
        while(jobCounter.get() > 0) {
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
