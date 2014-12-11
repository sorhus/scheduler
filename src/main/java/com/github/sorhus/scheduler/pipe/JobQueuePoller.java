package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.job.JobQueue;
import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.job.JobExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobQueuePoller implements Runnable {

    private final JobQueue jobQueue;
    private final JobSubmissionService jobSubmissionService;
    private final JobFinalizingService jobFinalizingService;
    private final PipeControl pipeControl;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public JobQueuePoller(
        JobQueue jobQueue,
        JobSubmissionService jobSubmissionService,
        JobFinalizingService jobFinalizingService,
        PipeControl pipeControl
    ) {
        this.jobQueue = jobQueue;
        this.jobSubmissionService = jobSubmissionService;
        this.jobFinalizingService = jobFinalizingService;
        this.pipeControl = pipeControl;
    }

    @Override
    public void run() {
        while(pipeControl.run()) {
            if(!jobQueue.isEmpty()) {
                Job job = jobQueue.poll();
                if(null != job) {
                    log.info("Found Job in queue, submitting: {}", job);
                    JobExecution jobExecution = jobSubmissionService.submitJob(job);
                    jobFinalizingService.finaliseJob(jobExecution);
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
