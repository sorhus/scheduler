package com.github.sorhus.scheduler.pipe.control;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.job.JobStatus;

/**
 * @author Anton Sorhus <anton.sorhus@visualdna.com>
 */
public abstract class PipeControl {

    protected final int nJobs;

    public PipeControl(int nJobs) {
        this.nJobs = nJobs;
    }

    public abstract int jobsLeft();
    public abstract int jobsDone();
    public abstract boolean run();
    public abstract void kill();

    public abstract void setStatus(Job job, JobStatus jobStatus);
    public abstract JobStatus getStatus(Job job);

    public abstract boolean available(Job job);
    public abstract void done(Job job);
    public abstract boolean isDone(Job job);
}
