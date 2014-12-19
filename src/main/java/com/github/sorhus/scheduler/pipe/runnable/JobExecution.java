package com.github.sorhus.scheduler.pipe.runnable;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.job.JobStatus;
import com.github.sorhus.scheduler.pipe.control.PipeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author: anton.sorhus@gmail.com
 *
 * TODO: ability to abort
 * TODO: timeout
 * TODO: check if done
 * TODO: force run
 */
public class JobExecution implements Runnable {

    private final Job job;
    private final PipeControl pipeControl;
    private final JobQueueSubmitter jobQueueSubmitter;

    private Process process;
    private JobLogger jobLogger;
    private IOException e;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public JobExecution(Job job, PipeControl pipeControl, JobQueueSubmitter jobQueueSubmitter) {
        this.job = job;
        this.pipeControl = pipeControl;
        this.jobQueueSubmitter = jobQueueSubmitter;
        String[] command = String.format("jobs/%s/run.sh %s", job.getName(), job.getParameters()).trim().split(" ");
        ProcessBuilder processBuilder = new ProcessBuilder()
            .command(command)
            .redirectErrorStream(true);
        try {
            this.process = processBuilder.start();
        } catch (IOException e) {
            this.e = e;
        }
    }

    public Job getJob() {
        return job;
    }

    public InputStream getProcessInputStream() {
        return process.getInputStream();
    }

    public void setJobLogger(JobLogger jobLogger) {
        this.jobLogger = jobLogger;
    }

    public JobLogger getJobLogger() {
        return jobLogger;
    }

    @Override
    public void run() {
        pipeControl.setStatus(job, JobStatus.RUNNING);
        if(null != e) {
            log.error("Job {} failed", e);
        } else {
            log.info("Waiting for {} to complete", this);
            try {
                int rc = process.waitFor();
                if(rc == 0) {
                    pipeControl.done(job);
                    log.info("Job finished: {}", job);
                    jobQueueSubmitter.interrupt(job.getDependents());
                } else {
                    pipeControl.setStatus(job, JobStatus.FAILED);
                    log.warn("Job {} returned code {}", job, rc);
                    jobQueueSubmitter.interrupt(job);
                }
            } catch (InterruptedException e) {
                pipeControl.setStatus(job, JobStatus.FAILED);
                log.warn("JobExecution {} failed with exception", job, e);
                jobQueueSubmitter.interrupt(job);
            } finally {
                jobLogger.shutDown();
            }
        }
    }

    @Override
    public String toString() {
        return "JobExecution: " + job.toString();
    }
}
