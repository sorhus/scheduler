package com.github.sorhus.scheduler.pipe.runnable;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.job.JobContainer;
import com.github.sorhus.scheduler.job.JobSpecification;
import com.github.sorhus.scheduler.pipe.control.PipeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * @author: anton.sorhus@gmail.com
 */
public class BackupJobQueueSubmitter implements Runnable {

    private final JobContainer jobContainer;
    private final Queue<Job> jobQueue;
    private final PipeControl pipeControl;

    private final Logger log = LoggerFactory.getLogger("Pipe");

    public BackupJobQueueSubmitter(JobContainer jobContainer, Queue<Job> jobQueue, PipeControl pipeControl) {
        this.jobContainer = jobContainer;
        this.jobQueue = jobQueue;
        this.pipeControl = pipeControl;
    }

    @Override
    public void run() {
        while (pipeControl.run()) {
            for (JobSpecification jobSpecification : jobContainer.getSpecifications().values()) {
                for (Job job : jobSpecification.getJobs()) {
                    if(pipeControl.available(job)) {
                        jobQueue.offer(job);
                    }
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {}
                }
            }
        }
    }
}
