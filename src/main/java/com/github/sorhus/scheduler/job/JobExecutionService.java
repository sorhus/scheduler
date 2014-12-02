package com.github.sorhus.scheduler.job;

import java.util.concurrent.ExecutorService;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobExecutionService {

    private final ExecutorService executorService;

    public JobExecutionService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public JobExecution initialiseJob(Job job) {
        JobExecution jobExecution = new JobExecution(job);
        JobLogger jobLogger = new JobLogger(jobExecution.getLogStream());
        executorService.submit(jobLogger);
        jobExecution.setJobLogger(jobLogger);
        return jobExecution;
    }
}