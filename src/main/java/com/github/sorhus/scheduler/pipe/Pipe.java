package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.JobQueue;
import com.github.sorhus.scheduler.job.model.Job;
import com.github.sorhus.scheduler.job.model.JobContainer;
import com.github.sorhus.scheduler.job.service.JobExecutionService;
import com.github.sorhus.scheduler.job.service.JobFinaliserService;
import com.github.sorhus.scheduler.job.runnable.JobSubmitter;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: anton.sorhus@gmail.com
 */
public class Pipe implements Runnable {

    private final JobContainer jobContainer;
    private final JobQueue jobQueue;
    private final AtomicInteger jobCounter;
    private final List<ExecutorService> executorServices;
    private final AtomicBoolean keepRunning;

    DateTime startTime;
    DateTime finishTime;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    private final static PeriodFormatter formatter = new PeriodFormatterBuilder()
        .appendHours().appendSuffix(" Hours, ")
        .appendMinutes().appendSuffix(" Minutes and ")
        .appendSeconds().appendSuffix(" Seconds")
        .toFormatter();

    public Pipe(JobContainer jobContainer, int workers) {
        this.jobContainer = jobContainer;
        this.jobQueue = new JobQueue();
        this.jobCounter = new AtomicInteger(jobContainer.getNumberOfJobs());

        ThreadFactory jobLoggerThreadFactory = new ThreadFactoryBuilder().setNameFormat("JobLogger-%d").build();
        ExecutorService jobLoggerExecutorService = Executors.newFixedThreadPool(workers, jobLoggerThreadFactory);
        JobExecutionService jobExecutionService = new JobExecutionService(jobLoggerExecutorService);

        ThreadFactory jobFinaliserThreadFactory = new ThreadFactoryBuilder().setNameFormat("JobFinaliser-%d").build();
        ExecutorService jobFinaliserExecutorService = Executors.newFixedThreadPool(workers, jobFinaliserThreadFactory);
        JobFinaliserService jobFinaliserService =
            new JobFinaliserService(jobFinaliserExecutorService, jobQueue, jobCounter);

        JobSubmitter jobSubmitter = new JobSubmitter(jobQueue, jobExecutionService, jobFinaliserService, jobCounter);
        new Thread(jobSubmitter, "JobSubmitter-0").start();

        this.executorServices = ImmutableList.of(jobLoggerExecutorService, jobFinaliserExecutorService);
        this.keepRunning = new AtomicBoolean(true);
    }


    public void abort() {
        this.keepRunning.set(false);
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
        while(keepRunning.get() && jobCounter.get() > 0) {
            log.info("Waiting for {} unfinished jobs", jobCounter);
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
        json.addProperty("Jobs complete", jobContainer.getNumberOfJobs() - jobCounter.get());
        json.addProperty("Jobs left", jobCounter.get());
        json.addProperty("Jobs in queue", jobQueue.size());
        if(null == finishTime) {
            json.addProperty("Elapsed time since start", formatter.print(new Period(startTime, DateTime.now())));
        } else {
            json.addProperty("Pipe finished in", formatter.print(new Period(startTime, finishTime)));
        }
        return json.toString();
    }
}
