package com.github.sorhus.scheduler.pipe;

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
    abstract void set(boolean run);
    abstract void jobDone();
    abstract boolean acquireLock();
    abstract boolean releaseLock();
}
