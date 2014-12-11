package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.job.JobQueue;
import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.job.JobContainer;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author: anton.sorhus@gmail.com
 */
public class Pipe implements Runnable {

    private final JobContainer jobContainer;
    private final JobQueue jobQueue;
    private final PipeControl pipeControl;
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
        this.jobQueue = new JobQueue();
        this.pipeControl = new PipeControl(jobContainer.getNumberOfJobs());

        ThreadFactory jobLoggerThreadFactory = new ThreadFactoryBuilder().setNameFormat("JobLogger-%d").build();
        ExecutorService jobLoggerExecutorService = Executors.newFixedThreadPool(workers, jobLoggerThreadFactory);
        JobSubmissionService jobSubmissionService = new JobSubmissionService(jobLoggerExecutorService);

        ThreadFactory jobFinaliserThreadFactory = new ThreadFactoryBuilder().setNameFormat("JobFinaliser-%d").build();
        ExecutorService jobFinaliserExecutorService = Executors.newFixedThreadPool(workers, jobFinaliserThreadFactory);
        JobFinalizingService jobFinalizingService =
            new JobFinalizingService(jobFinaliserExecutorService, jobQueue, pipeControl);

        JobQueuePoller jobQueuePoller = new JobQueuePoller(jobQueue, jobSubmissionService, jobFinalizingService, pipeControl);
        new Thread(jobQueuePoller, "JobQueuePoller-0").start();

        this.executorServices = ImmutableList.of(jobLoggerExecutorService, jobFinaliserExecutorService);
    }


    public void abort() {
        pipeControl.set(false);
    }

    public JobContainer getJobContainer() {
        return jobContainer;
    }

    @Override
    public void run() {

        // kick off pipe
        log.info("Kicking off pipe");
        this.startTime = DateTime.now();
        for(Job job: jobContainer.getEntryPoints()) {
            jobQueue.offer(job);
        }

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

    public boolean pause(String job) {
        return jobContainer.pause(job);
    }

    public boolean unpause(String job) {
        return jobContainer.unpause(job);
    }
}
