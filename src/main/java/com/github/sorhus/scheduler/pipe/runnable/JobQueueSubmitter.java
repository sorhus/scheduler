package com.github.sorhus.scheduler.pipe.runnable;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.pipe.control.PipeControl;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobQueueSubmitter extends Thread {

    private final PipeControl pipeControl;
    private final Queue<Job> jobQueue;
    private final int sleep;
    private final Set<Job> candidates;

    private final Logger log = LoggerFactory.getLogger("Pipe");

    public JobQueueSubmitter(Iterable<Job> entryPoints, PipeControl pipeControl, Queue<Job> jobQueue, int sleep) {
        super("JobQueueSubmitter");
        this.pipeControl = pipeControl;
        this.jobQueue = jobQueue;
        this.sleep = sleep;
        this.candidates = Collections.newSetFromMap(new ConcurrentHashMap<Job, Boolean>());
        for(Job job : entryPoints) {
            if(pipeControl.available(job)) {
                jobQueue.offer(job);
            }
        }
    }

    public void interrupt(Job candidate) {
        interrupt(Lists.newArrayList(candidate));
    }

    public void interrupt(List<Job> candidates) {
        this.candidates.addAll(candidates);
        super.interrupt();
    }

    @Override
    public void run() {
        while (pipeControl.run()) {
            for (Job job : candidates) {
                if(pipeControl.available(job)) {
                    jobQueue.offer(job);
                    candidates.remove(job);
                    log.info("Job {} put in Job Queue", job);
                } else if(pipeControl.isDone(job)) {
                    candidates.remove(job);
                    log.info("Job {} removed from candidates", job);
                }
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                log.info("Sleep interrupted. Candidates contains: {{}}", Joiner.on(",").join(candidates));
            }
        }
    }
}
