package com.github.sorhus.scheduler.pipe.control;

import com.github.sorhus.scheduler.job.Job;

/**
 * @author: anton.sorhus@gmail.com
 */
public class SimplePipeControl extends PipeControl {

    private int jobsDone;
    private boolean run;

    public SimplePipeControl(int nJobs) {
        super(nJobs);
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

    public void kill() {
        this.run = false;
    }

    @Override
    public void setStatus(Job job, JobStatus jobStatus) {
        job.setStatus(jobStatus);
    }

    @Override
    public JobStatus getStatus(Job job) {
        return job.getStatus();
    }

    @Override
    public boolean available(Job job) {
        boolean available = job.getStatus() == JobStatus.WAITING || job.getStatus() == JobStatus.FAILED;
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
}
