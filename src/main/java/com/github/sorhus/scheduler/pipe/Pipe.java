package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.job.JobContainer;
import com.github.sorhus.scheduler.pipe.control.JobStatus;
import com.github.sorhus.scheduler.pipe.control.SimplePipeControl;
import com.github.sorhus.scheduler.pipe.runnable.JobQueuePoller;
import com.github.sorhus.scheduler.pipe.runnable.JobQueueSubitter;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author: anton.sorhus@gmail.com
 */
public class Pipe implements Runnable {

    private final JobContainer jobContainer;
    private final Queue<Job> jobQueue;
    private final SimplePipeControl pipeControl;
    private final List<ExecutorService> executorServices;

    DateTime startTime;
    DateTime finishTime;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    private final static PeriodFormatter formatter = new PeriodFormatterBuilder()
        .appendHours().appendSuffix(" Hours, ")
        .appendMinutes().appendSuffix(" Minutes and ")
        .appendSeconds().appendSuffix(" Seconds")
        .toFormatter();

    public Pipe(List<String> specificationStrings, int workers) {
        this.jobContainer = new JobContainer(specificationStrings);
        this.jobQueue = new ConcurrentLinkedQueue<>();
        this.pipeControl = new SimplePipeControl(jobContainer.getNumberOfJobs());

        ThreadFactory jobExecutorThreadFactory = new ThreadFactoryBuilder().setNameFormat("JobExecutor-%d").build();
        ExecutorService jobExecutorService = Executors.newFixedThreadPool(workers, jobExecutorThreadFactory);
        ThreadFactory logExecutorThreadFactory = new ThreadFactoryBuilder().setNameFormat("JobLogger-%d").build();
        ExecutorService logExecutorService = Executors.newFixedThreadPool(workers, logExecutorThreadFactory);
        JobSubmissionService jobSubmissionService = new JobSubmissionService(pipeControl, jobExecutorService, logExecutorService);

        JobQueuePoller jobQueuePoller = new JobQueuePoller(jobQueue, jobSubmissionService, pipeControl);
        new Thread(jobQueuePoller, "JobQueuePoller").start();

        this.executorServices = ImmutableList.of(logExecutorService, jobExecutorService);
    }

    public void abort() {
        pipeControl.kill();
    }

    public JobContainer getJobContainer() {
        return jobContainer;
    }

    @Override
    public void run() {

        // kick off pipe
        this.startTime = DateTime.now();
        new Thread(new JobQueueSubitter(jobContainer, jobQueue, pipeControl), "JobQueueSubmitter").start();


        // await completion
        while(pipeControl.run()) {
            log.info("Waiting for {} unfinished jobs", pipeControl.jobsLeft());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {}
        }

        this.finishTime = DateTime.now();
        log.info("All jobs finished in {}", formatter.print(new Period(startTime, finishTime)));
        log.info("GREAT SUCCESS");

        // shut down
        for (ExecutorService executorService : executorServices) {
            try {
                executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.warn("ExecutorService {} did not terminate gracefully", executorService, e);
            } finally {
                executorService.shutdownNow();
            }
        }
    }

    @Override
    public String toString() {
        JsonObject json = new JsonObject();
        json.addProperty("Jobs complete", pipeControl.jobsDone());
        json.addProperty("Jobs left", pipeControl.jobsLeft());
        json.addProperty("Jobs in queue", jobQueue.size());
        if(null == finishTime) {
            json.addProperty("Elapsed time since start", formatter.print(new Period(startTime, DateTime.now())));
        } else {
            json.addProperty("Pipe finished in", formatter.print(new Period(startTime, finishTime)));
        }
        return json.toString();
    }

    // TODO
    public boolean pause(String jobName) {
//        for (Job job : jobContainer.getSpecifications().get(jobName).getJobs()) {
//            pipeControl.setStatus(job, JobStatus.PAUSED);
//        }
        return true;
    }

    // TODO
    public boolean unpause(String jobName) {
//        for (Job job : jobContainer.getSpecifications().get(jobName).getJobs()) {
//            pipeControl.setStatus(job, JobStatus.WAITING);
//        }
        return true;
    }
}
