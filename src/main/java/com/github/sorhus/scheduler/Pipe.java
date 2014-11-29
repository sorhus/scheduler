package com.github.sorhus.scheduler;

import com.google.common.base.Joiner;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: anton.sorhus@gmail.com
 */
public class Pipe {

    private final static Logger log = LoggerFactory.getLogger(Pipe.class);

    public Pipe(List<JobSpecification> jobSpecifications) {
        log.info("New pipe initialising with {} job specifications: {}", jobSpecifications.size(), Joiner.on(", ").join(jobSpecifications));
        JobFactory jobFactory = new JobFactory(jobSpecifications);
        log.info("There were {} jobs in total", jobFactory.getNumberOfJobs());

        ThreadFactory jobLoggerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("JobLogger-%d").build();
        ExecutorService jobLoggerExecutorService = Executors.newFixedThreadPool(2, jobLoggerThreadFactory);
        JobExecutionService jobExecutionService = new JobExecutionService(jobLoggerExecutorService);

        ThreadFactory jobFinaliserThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("JobFinaliser-%d").build();
        ExecutorService jobFinaliserExecutorService = Executors.newFixedThreadPool(2, jobFinaliserThreadFactory);
        Queue<Job> jobQueue = new ConcurrentLinkedQueue<>();
        AtomicInteger jobCounter = new AtomicInteger(jobFactory.getNumberOfJobs());
        JobFinaliserService jobFinaliserService = new JobFinaliserService(jobFinaliserExecutorService, jobQueue, jobCounter);

        JobSubmitter jobSubmitter = new JobSubmitter(jobQueue, jobExecutionService, jobFinaliserService);
        new Thread(jobSubmitter, "JobSubmitter-0").start();

        List<Job> entryPoints = jobFactory.getEntryPoints();
        log.info("Found {} entry points", entryPoints.size());
        log.info("Kicking off pipe");
        for(Job job: entryPoints) {
            job.setDormant(false);
            jobQueue.offer(job);
        }

        while(jobCounter.get() > 0) {
            log.info("Waiting for {} unfinished jobs", jobCounter);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {}
        }
        log.info("All jobs finished, SUCCESS");
    }

}
