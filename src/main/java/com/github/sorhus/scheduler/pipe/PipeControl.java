package com.github.sorhus.scheduler.pipe;

/**
 * @author: anton.sorhus@gmail.com
 */
public class PipeControl {

    private final int nJobs;
    private int jobsDone;
    private boolean run;

    public PipeControl(int nJobs) {
        this.nJobs = nJobs;
        this.jobsDone = 0;
        this.run = true;
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
}
