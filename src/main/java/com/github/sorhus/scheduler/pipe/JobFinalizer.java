package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.job.JobQueue;
import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.job.JobExecution;
import com.github.sorhus.scheduler.job.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobFinalizer implements Runnable {

    private final JobQueue jobQueue;
    private final SimplePipeControl pipeControl;
    private final JobExecution jobExec;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public JobFinalizer(JobQueue jobQueue, SimplePipeControl pipeControl, JobExecution jobExec) {
        this.jobQueue = jobQueue;
        this.pipeControl = pipeControl;
        this.jobExec = jobExec;
    }

    @Override
    public void run() {
        jobExec.await();
        Job job = jobExec.getJob();
        if(job.getStatus() == JobStatus.DONE) {
            pipeControl.jobDone();
            log.info("Job {} is done, evaluating dependents as candidates for job queue: {}", job, job.getDependents());
            synchronized (pipeControl) { // or poke the thread that does this..
                for (Job candidate : job.getDependents()) {
                    if(candidate.available()) {
                        log.info("All dependencies for Job {} is done, putting it in job queue", candidate);
                        jobQueue.offer(candidate);
                    }
                }
            }
        } else {
            log.info("JobExecution failed, adding it back to jobExec queue");
            jobQueue.offer(job);
        }
    }
}