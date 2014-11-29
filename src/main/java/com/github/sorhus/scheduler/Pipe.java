package com.github.sorhus.scheduler;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
public class Pipe {

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    public Pipe(List<JobSpecification> jobSpecifications, int workers) {

        log.info("New pipe initialising with {} job specifications: {}", jobSpecifications.size(), Joiner.on(", ").join(jobSpecifications));
        JobFactory jobFactory = new JobFactory(jobSpecifications);
        log.info("There were {} jobs in total", jobFactory.getNumberOfJobs());

        Queue<Job> jobQueue = new ConcurrentLinkedQueue<>();
        AtomicInteger jobCounter = new AtomicInteger(jobFactory.getNumberOfJobs());
        List<ExecutorService> executorServices = initialiseExecutors(workers, jobQueue, jobCounter);

        List<Job> entryPoints = jobFactory.getEntryPoints();
        log.info("Found {} entry points", entryPoints.size());
        log.info("Kicking off pipe");
        for(Job job: entryPoints) {
            job.setDormant(false);
            jobQueue.offer(job);
        }

        awaitCompletion(jobCounter);
        log.info("All jobs finished, GREAT SUCCESS");

        shutDown(executorServices);
    }

    private List<ExecutorService> initialiseExecutors(int workers,Queue<Job> jobQueue, AtomicInteger jobCounter) {

        ThreadFactory jobLoggerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("JobLogger-%d").build();
        ExecutorService jobLoggerExecutorService = Executors.newFixedThreadPool(workers, jobLoggerThreadFactory);
        JobExecutionService jobExecutionService = new JobExecutionService(jobLoggerExecutorService);

        ThreadFactory jobFinaliserThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("JobFinaliser-%d").build();
        ExecutorService jobFinaliserExecutorService = Executors.newFixedThreadPool(workers, jobFinaliserThreadFactory);
        JobFinaliserService jobFinaliserService = new JobFinaliserService(jobFinaliserExecutorService, jobQueue, jobCounter);

        JobSubmitter jobSubmitter = new JobSubmitter(jobQueue, jobExecutionService, jobFinaliserService, jobCounter);
        new Thread(jobSubmitter, "JobSubmitter-0").start();

        return ImmutableList.of(jobLoggerExecutorService, jobFinaliserExecutorService);
    }

    private void awaitCompletion(AtomicInteger jobCounter) {
        while(jobCounter.get() > 0) {
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
}
