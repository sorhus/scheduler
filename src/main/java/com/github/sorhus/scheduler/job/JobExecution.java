package com.github.sorhus.scheduler.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobExecution {

    private final Job job;
    private Process process;
    private InputStream logStream;
    private IOException e;
    private JobLogger jobLogger;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public JobExecution(Job job) {
        this.job = job;
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

    public Boolean get() throws InterruptedException, ExecutionException, IOException {
        if(null != e) {
            log.error("Job {} failed", e);
            return Boolean.FALSE;
        } else {
            log.info("Waiting for {} to complete", this);
            boolean success = process.waitFor() == 0;
            jobLogger.shutDown();
            if(success) {
                job.setDone(true);
            }
            return success;
        }
    }

    @Override
    public String toString() {
        return "JobExecution: " + job.toString();
    }
}
