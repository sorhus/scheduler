package com.github.sorhus.scheduler.pipe;

/**
 * @author: anton.sorhus@gmail.com
 */
public class SimplePipeControl extends PipeControl {

    private int jobsDone;
    private boolean run;
    private boolean lockFree;

    public SimplePipeControl(int nJobs) {
        super(nJobs);
        this.jobsDone = 0;
        this.run = true;
        this.lockFree = true;
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

    public void set(boolean run) {
        this.run = run;
    }

    public void jobDone() {
        jobsDone++;
    }

    @Override
    public void acquireLock() {
        if(lockFree) {
            lockFree = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean releaseLock() {
        lockFree = true;
        return true;
    }
}
