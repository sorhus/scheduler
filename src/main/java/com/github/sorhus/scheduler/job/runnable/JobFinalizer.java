package com.github.sorhus.scheduler.job.runnable;

import com.github.sorhus.scheduler.JobQueue;
import com.github.sorhus.scheduler.job.model.Job;
import com.github.sorhus.scheduler.job.model.JobExecution;
import com.github.sorhus.scheduler.job.model.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobFinalizer implements Runnable {

    private final JobQueue jobQueue;
    private final AtomicInteger jobCounter;
    private final JobExecution jobExec;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public JobFinalizer(JobQueue jobQueue, AtomicInteger jobCounter, JobExecution jobExec) {
        this.jobQueue = jobQueue;
        this.jobCounter = jobCounter;
        this.jobExec = jobExec;
    }

    @Override
    public void run() {
        boolean success = false;
        try {
            success = jobExec.get();
        } catch (ExecutionException | InterruptedException | IOException e) {
            log.warn("JobExecution {} failed with exception", jobExec, e);
        }

        Job job = jobExec.getJob();
        if(success) {
            jobCounter.decrementAndGet();
            log.info("Job {} is done, evaluating dependents as candidates for job queue: {}", job, job.getDependents());
            for (Job candidate : job.getDependents()) {
                synchronized (candidate) {
                    boolean approved = candidate.getStatus() == Status.WAITING;
                    for (Job dependency : candidate.getDependencies()) {
                        approved &= dependency.getStatus() == Status.DONE;
                    }
                    if(approved) {
                        log.info("All dependencies for Job {} is done, putting it in job queue", candidate);
                        jobQueue.offer(candidate);
                    }
                }
            }
        } else {
            log.info("JobExecution failed, adding it back to jobExec queue");
            jobQueue.offer(job);
        }
    }

}