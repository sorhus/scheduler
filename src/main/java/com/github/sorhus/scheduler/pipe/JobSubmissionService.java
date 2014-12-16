package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.pipe.runnable.JobExecution;
import com.github.sorhus.scheduler.pipe.control.PipeControl;
import com.github.sorhus.scheduler.pipe.runnable.JobLogger;
import com.github.sorhus.scheduler.pipe.runnable.JobQueueSubmitter;

import java.util.concurrent.ExecutorService;

/**
 * @author Anton Sorhus <anton.sorhus@visualdna.com>
 */
public class JobSubmissionService {

    private final PipeControl pipeControl;
    private final JobQueueSubmitter jobQueueSubmitter;
    private final ExecutorService jobExecutorService;
    private final ExecutorService logExecutorService;

    public JobSubmissionService(PipeControl pipeControl, JobQueueSubmitter jobQueueSubmitter, ExecutorService jobExecutorService, ExecutorService logExecutorService) {
        this.pipeControl = pipeControl;
        this.jobQueueSubmitter = jobQueueSubmitter;
        this.jobExecutorService = jobExecutorService;
        this.logExecutorService = logExecutorService;
    }

    public void submitJob(Job job) {
        JobExecution jobExecution = new JobExecution(job, pipeControl, jobQueueSubmitter);
        JobLogger jobLogger = new JobLogger(jobExecution.getLogStream());
        jobExecution.setJobLogger(jobLogger);
        logExecutorService.submit(jobLogger);
        jobExecutorService.submit(jobExecution);
    }

}
