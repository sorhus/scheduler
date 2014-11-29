package com.github.sorhus.scheduler;

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

    private final Queue<Job> jobQueue;
    private final AtomicInteger jobCounter;
    private final JobExecution jobExec;

    private final static Logger log = LoggerFactory.getLogger(JobFinalizer.class);

    public JobFinalizer(
        Queue<Job> jobQueue,
        AtomicInteger jobCounter,
        JobExecution jobExec
    ) {
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

        if(success) {
            log.info("Job {} finished, evaluating dependents as candidates for job queue: {}", jobExec, jobExec.getJob().getDependents());
            jobCounter.decrementAndGet();
            for (Job candidate : jobExec.getJob().getDependents()) {
                synchronized (candidate) {
                    boolean approved = candidate.isDormant();
                    for (Job dependency : candidate.getDependencies()) {
                        approved &= dependency.isDone();
                    }
                    if(approved) {
                        log.info("All dependencies for Job {} is done, putting it in job queue");
                        candidate.setDormant(false);
                        jobQueue.offer(candidate);
                    }
                }
            }
        } else {
            log.info("JobExecution failed, adding it back to jobExec queue");
            jobQueue.offer(jobExec.getJob());
        }
    }

}