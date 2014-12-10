package com.github.sorhus.scheduler.job.service;

import com.github.sorhus.scheduler.JobQueue;
import com.github.sorhus.scheduler.job.model.Job;
import com.github.sorhus.scheduler.job.model.JobExecution;
import com.github.sorhus.scheduler.job.runnable.JobFinalizer;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobFinaliserService {

    private final ExecutorService executorService;
    private final JobQueue jobQueue;
    private final AtomicInteger jobCounter;

    public JobFinaliserService(ExecutorService executorService, JobQueue jobQueue, AtomicInteger jobCounter) {
        this.executorService = executorService;
        this.jobQueue = jobQueue;
        this.jobCounter = jobCounter;
    }

    public void finaliseJob(JobExecution job) {
        JobFinalizer jobFinalizer = new JobFinalizer(jobQueue, jobCounter, job);
        executorService.submit(jobFinalizer);
    }
}