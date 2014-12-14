package com.github.sorhus.scheduler.pipe.runnable;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.pipe.JobSubmissionService;
import com.github.sorhus.scheduler.pipe.control.SimplePipeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobQueuePoller implements Runnable {

    private final Queue<Job> jobQueue;
    private final JobSubmissionService jobSubmissionService;
    private final SimplePipeControl pipeControl;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public JobQueuePoller(
        Queue<Job> jobQueue,
        JobSubmissionService jobSubmissionService,
        SimplePipeControl pipeControl
    ) {
        this.jobQueue = jobQueue;
        this.jobSubmissionService = jobSubmissionService;
        this.pipeControl = pipeControl;
    }

    @Override
    public void run() {
        while(pipeControl.run()) {
            if(!jobQueue.isEmpty()) {
                Job job = jobQueue.poll();
                if(null != job) {
                    log.info("Found Job in queue, submitting: {}", job);
                    jobSubmissionService.submitJob(job);
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
