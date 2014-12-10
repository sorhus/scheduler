package com.github.sorhus.scheduler;

import com.github.sorhus.scheduler.job.model.Job;
import com.github.sorhus.scheduler.job.model.Status;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobQueue {
    Queue<Job> queue;

    public JobQueue() {
        queue = new ConcurrentLinkedQueue<>();
    }

    public Job poll() {
        Job job = queue.poll();
        if(null != job) {
            job.setStatus(Status.RUNNING);
        }
        return job;
    }

    public void offer(Job job) {
        job.setStatus(Status.QUEUED);
        queue.offer(job);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}
