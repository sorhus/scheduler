package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.pipe.runnable.JobExecution;
import com.github.sorhus.scheduler.pipe.control.PipeControl;
import com.github.sorhus.scheduler.pipe.runnable.JobLogger;

import java.util.concurrent.ExecutorService;

/**
 * @author Anton Sorhus <anton.sorhus@visualdna.com>
 */
public class JobSubmissionService {

    private final PipeControl pipeControl;
    private final ExecutorService jobExecutorService;
    private final ExecutorService logExecutorService;

    public JobSubmissionService(PipeControl pipeControl, ExecutorService jobExecutorService, ExecutorService logExecutorService) {
        this.pipeControl = pipeControl;
        this.jobExecutorService = jobExecutorService;
        this.logExecutorService = logExecutorService;
    }

    public void submitJob(Job job) {
        JobExecution jobExecution = new JobExecution(job, pipeControl);
        JobLogger jobLogger = new JobLogger(jobExecution.getLogStream());
        jobExecution.setJobLogger(jobLogger);
        logExecutorService.submit(jobLogger);
        jobExecutorService.submit(jobExecution);
    }

}
