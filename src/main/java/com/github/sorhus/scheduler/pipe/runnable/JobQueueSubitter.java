package com.github.sorhus.scheduler.pipe.runnable;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.job.JobContainer;
import com.github.sorhus.scheduler.job.JobSpecification;
import com.github.sorhus.scheduler.pipe.control.PipeControl;

import java.util.Map;
import java.util.Queue;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobQueueSubitter implements Runnable {

    private final JobContainer jobContainer;
    private final Queue<Job> jobQueue;
    private final PipeControl pipeControl;

    public JobQueueSubitter(JobContainer jobContainer, Queue<Job> jobQueue, PipeControl pipeControl) {
        this.jobContainer = jobContainer;
        this.jobQueue = jobQueue;
        this.pipeControl = pipeControl;
        for(Job job: jobContainer.getEntryPoints()) {
            if(pipeControl.available(job)) {
                jobQueue.offer(job);
            }
        }
    }

    /*
     * TODO improve
     */
    @Override
    public void run() {
        while (true) {
            for (Map.Entry<String, JobSpecification> entry : jobContainer.getSpecifications().entrySet()) {
                for (Job job : entry.getValue().getJobs()) {
                    if(pipeControl.available(job)) {
                        jobQueue.offer(job);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            }
        }
    }
}
