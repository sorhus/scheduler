package com.github.sorhus.scheduler.pipe.runnable;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.pipe.control.PipeControl;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobExecutionFactory {

    private final PipeControl pipeControl;
    private final JobQueueSubmitter jobQueueSubmitter;
    private final int jobLoggerSleep;

    public JobExecutionFactory(PipeControl pipeControl, JobQueueSubmitter jobQueueSubmitter, int jobLoggerSleep) {
        this.pipeControl = pipeControl;
        this.jobQueueSubmitter = jobQueueSubmitter;
        this.jobLoggerSleep = jobLoggerSleep;
    }

    public JobExecution getJobExecution(Job job) {
        JobExecution jobExecution = new JobExecution(job, pipeControl, jobQueueSubmitter);
        JobLogger jobLogger = new JobLogger(jobExecution.getProcessInputStream(), jobLoggerSleep);
        jobExecution.setJobLogger(jobLogger);
        return jobExecution;
    }
}
