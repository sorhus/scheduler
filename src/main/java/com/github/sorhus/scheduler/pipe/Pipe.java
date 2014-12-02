package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.job.JobContainer;
import com.github.sorhus.scheduler.job.JobExecutionService;
import com.github.sorhus.scheduler.job.JobFinaliserService;
import com.github.sorhus.scheduler.job.JobSpecification;
import com.github.sorhus.scheduler.job.JobSubmitter;
import com.google.common.base.Joiner;
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

    private final int workers;

    private final AtomicBoolean keepRunning;
    private final Queue<Job> jobQueue;
    private final AtomicInteger jobCounter;
    private final JobContainer jobContainer;

    DateTime startingTime;
    DateTime finishTime;
    List<ExecutorService> executorServices;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    private final static PeriodFormatter formatter = new PeriodFormatterBuilder()
        .appendHours().appendSuffix(" Hours, ")
        .appendMinutes().appendSuffix(" Minutes and ")
        .appendSeconds().appendSuffix(" Seconds")
        .toFormatter();

    public Pipe(List<JobSpecification> jobSpecifications, int workers) {
        this.workers = workers;
        this.keepRunning = new AtomicBoolean(true);
        log.info(
            "New pipe initialising with {} job specifications: {}",
            jobSpecifications.size(),
            Joiner.on(", ").join(jobSpecifications)
        );
        this.jobContainer = new JobContainer(jobSpecifications);
        this.jobQueue = new ConcurrentLinkedQueue<>();
        log.info("There were {} jobs in total", jobContainer.getNumberOfJobs());
        this.jobCounter = new AtomicInteger(jobContainer.getNumberOfJobs());
    }

    @Override
    public void run() {

        log.info("Kicking off pipe");
        this.startingTime = DateTime.now();
        this.executorServices = initialiseExecutorsAndServices(workers, jobQueue, jobCounter);

        for(Job job: jobContainer.getEntryPoints()) {
            job.setDormant(false);
            jobQueue.offer(job);
        }

        awaitCompletion(jobCounter);

        this.finishTime = DateTime.now();
        log.info("All jobs finished in {}", formatter.print(new Period(startingTime, finishTime)));
        log.info("GREAT SUCCESS");

        shutDown(executorServices);
    }

    public void abort() {
        this.keepRunning.set(false);
    }

    private List<ExecutorService> initialiseExecutorsAndServices(
            int workers, Queue<Job> jobQueue, AtomicInteger jobCounter) {

        ThreadFactory jobLoggerThreadFactory = new ThreadFactoryBuilder().setNameFormat("JobLogger-%d").build();
        ExecutorService jobLoggerExecutorService = Executors.newFixedThreadPool(workers, jobLoggerThreadFactory);
        JobExecutionService jobExecutionService = new JobExecutionService(jobLoggerExecutorService);

        ThreadFactory jobFinaliserThreadFactory = new ThreadFactoryBuilder().setNameFormat("JobFinaliser-%d").build();
        ExecutorService jobFinaliserExecutorService = Executors.newFixedThreadPool(workers, jobFinaliserThreadFactory);
        JobFinaliserService jobFinaliserService =
            new JobFinaliserService(jobFinaliserExecutorService, jobQueue, jobCounter);

        JobSubmitter jobSubmitter = new JobSubmitter(jobQueue, jobExecutionService, jobFinaliserService, jobCounter);
        new Thread(jobSubmitter, "JobSubmitter-0").start();

        return ImmutableList.of(jobLoggerExecutorService, jobFinaliserExecutorService);
    }

    private void awaitCompletion(AtomicInteger jobCounter) {
        while(keepRunning.get() && jobCounter.get() > 0) {
            log.info("Waiting for {} unfinished jobs", jobCounter);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {}
        }

    }

    private void shutDown(List<ExecutorService> executorServices) {
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
            json.addProperty("Elapsed time since start", formatter.print(new Period(startingTime, DateTime.now())));
        } else {
            json.addProperty("Pipe finished in", formatter.print(new Period(startingTime, finishTime)));
        }
        return json.toString();
    }
}
