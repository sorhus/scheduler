package com.github.sorhus.scheduler.pipe.control;

import com.github.sorhus.scheduler.job.Job;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: anton.sorhus@gmail.com
 */
public class SimplePipeControl extends PipeControl {

    private int jobsDone;
    private boolean run;
    private final Map<Job, JobStatus> status;

    public SimplePipeControl(int nJobs) {
        super(nJobs);
        this.jobsDone = 0;
        this.run = true;
        this.status = new HashMap<>();
    }

    public int jobsLeft() {
        return nJobs - jobsDone;
    }

    public int jobsDone() {
        return jobsDone;
    }

    public boolean run() {
        return run && jobsLeft() > 0;
    }

    public void kill() {
        this.run = false;
    }

    @Override
    public void setStatus(Job job, JobStatus jobStatus) {
        status.put(job, jobStatus);
    }

    @Override
    public JobStatus getStatus(Job job) {
        //TODO: fix hack
        if(!status.containsKey(job)) {
            setStatus(job, JobStatus.WAITING);
        }
        return status.get(job);
    }

    @Override
    public boolean available(Job job) {
        boolean available = getStatus(job) == JobStatus.WAITING || getStatus(job) == JobStatus.FAILED;
        for (Job dependency : job.getDependencies()) {
            available &= getStatus(dependency) == JobStatus.DONE;
        }
        if(available) {
            setStatus(job, JobStatus.QUEUED);
            return true;
        }
        return false;
    }

    @Override
    public void done(Job job) {
        setStatus(job, JobStatus.DONE);
        jobsDone++;
    }

    @Override
    public boolean isDone(Job job) {
        return getStatus(job) == JobStatus.DONE;
    }
}
