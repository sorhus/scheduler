package com.github.sorhus.scheduler.job.runnable;

import com.github.sorhus.scheduler.JobQueue;
import com.github.sorhus.scheduler.job.model.Status;
import com.github.sorhus.scheduler.job.service.JobExecutionService;
import com.github.sorhus.scheduler.job.service.JobFinaliserService;
import com.github.sorhus.scheduler.job.model.Job;
import com.github.sorhus.scheduler.job.model.JobExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobSubmitter implements Runnable {

    private final JobQueue jobQueue;
    private final JobExecutionService jobExecutionService;
    private final JobFinaliserService jobFinaliserService;
    private final AtomicInteger jobCounter;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public JobSubmitter(
        JobQueue jobQueue,
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
                if(null != job) {
                    log.info("Found Job in queue, submitting: {}", job);
                    JobExecution jobExecution = jobExecutionService.initialiseJob(job);
                    jobFinaliserService.finaliseJob(jobExecution);
                }
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
