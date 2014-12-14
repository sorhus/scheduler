package com.github.sorhus.scheduler.pipe.control;

import com.github.sorhus.scheduler.job.Job;

/**
 * @author Anton Sorhus <anton.sorhus@visualdna.com>
 */
public abstract class PipeControl {

    protected final int nJobs;

    protected PipeControl(int nJobs) {
        this.nJobs = nJobs;
    }

    abstract int jobsLeft();
    abstract int jobsDone();
    abstract boolean run();
    abstract void kill();

    public abstract void setStatus(Job job, JobStatus jobStatus);
    public abstract JobStatus getStatus(Job job);

    public abstract boolean available(Job job);
    public abstract void done(Job job);
}
