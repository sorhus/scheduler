package com.github.sorhus.scheduler.pipe.runnable;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.pipe.control.JobStatus;
import com.github.sorhus.scheduler.pipe.control.PipeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobExecution implements Runnable {

    private final Job job;
    private final PipeControl pipeControl;
    private Process process;
    private InputStream logStream;
    private IOException e;
    private JobLogger jobLogger;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public JobExecution(Job job, PipeControl pipeControl) {
        this.job = job;
        this.pipeControl = pipeControl;
        String[] command = String.format("jobs/%s/run.sh %s", job.getName(), job.getParameters()).trim().split(" ");
        ProcessBuilder processBuilder = new ProcessBuilder()
            .command(command)
            .redirectErrorStream(true);
        try {
            this.process = processBuilder.start();
            this.logStream = process.getInputStream();
        } catch (IOException e) {
            this.e = e;
        }
    }

    public Job getJob() {
        return job;
    }

    public InputStream getLogStream() {
        return logStream;
    }

    public void setJobLogger(JobLogger jobLogger) {
        this.jobLogger = jobLogger;
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
                } else {
                    pipeControl.setStatus(job, JobStatus.FAILED);
                    log.warn("Job {} returned code {}", job, rc);
                }
            } catch (InterruptedException e) {
                pipeControl.setStatus(job, JobStatus.FAILED);
                log.warn("JobExecution {} failed with exception", job, e);
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
