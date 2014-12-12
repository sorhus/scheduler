package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.job.JobQueue;
import com.github.sorhus.scheduler.job.JobExecution;

import java.util.concurrent.ExecutorService;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobFinalizingService {

    private final ExecutorService executorService;
    private final JobQueue jobQueue;
    private final SimplePipeControl pipeControl;

    public JobFinalizingService(ExecutorService executorService, JobQueue jobQueue, SimplePipeControl pipeControl) {
        this.executorService = executorService;
        this.jobQueue = jobQueue;
        this.pipeControl = pipeControl;
    }

    public void finaliseJob(JobExecution job) {
        JobFinalizer jobFinalizer = new JobFinalizer(jobQueue, pipeControl, job);
        executorService.submit(jobFinalizer);
    }
}