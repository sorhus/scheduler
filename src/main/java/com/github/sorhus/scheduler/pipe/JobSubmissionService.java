package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.job.JobExecution;

import java.util.concurrent.ExecutorService;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobSubmissionService {

    private final ExecutorService executorService;

    public JobSubmissionService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public JobExecution submitJob(Job job) {
        JobExecution jobExecution = new JobExecution(job);
        JobLogger jobLogger = new JobLogger(jobExecution.getLogStream());
        jobExecution.setJobLogger(jobLogger);
        executorService.submit(jobLogger);
        return jobExecution;
    }
}
