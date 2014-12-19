package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.job.Job;
import com.github.sorhus.scheduler.job.JobContainer;
import com.github.sorhus.scheduler.pipe.control.PipeControl;
import com.github.sorhus.scheduler.pipe.control.SimplePipeControl;
import com.github.sorhus.scheduler.pipe.runnable.JobExecutionFactory;
import com.github.sorhus.scheduler.pipe.runnable.JobQueuePoller;
import com.github.sorhus.scheduler.pipe.runnable.JobQueueSubmitter;
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
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @author: anton.sorhus@gmail.com
 *
 * TODO: Wire with spring
 * TODO: Make sleep times configurable
 * TODO: External PipeControl
 */
public class Pipe implements Runnable {

    private final JobContainer jobContainer;
    private final Queue<Job> jobQueue;
    private final PipeControl pipeControl;
    private final JobQueueSubmitter jobQueueSubmitter;
    private final JobQueuePoller jobQueuePoller;
    private final List<ExecutorService> executorServices;

    DateTime startTime;
    DateTime finishTime;

    private final static Logger log = LoggerFactory.getLogger("Pipe");

    private final static PeriodFormatter formatter = new PeriodFormatterBuilder()
        .appendHours().appendSuffix(" Hours, ")
        .appendMinutes().appendSuffix(" Minutes and ")
        .appendSeconds().appendSuffix(" Seconds")
        .toFormatter();

    public Pipe(List<String> specificationStrings, PipeControlFactory pipeControlFactory, Properties properties) {
        this.jobContainer = new JobContainer(specificationStrings);
        this.jobQueue = new ConcurrentLinkedQueue<>();
        properties.setProperty("pipe.numberOfJobs", String.valueOf(jobContainer.getNumberOfJobs()));
        this.pipeControl = pipeControlFactory.getPipeControl(properties);

        int jobQueueSleep = Integer.parseInt(properties.getProperty("pipe.jobQueueSleep", "1000"));
        this.jobQueueSubmitter =
            new JobQueueSubmitter(jobContainer.getEntryPoints(), pipeControl, jobQueue, jobQueueSleep);

        int numberOfWorkers = Integer.parseInt(properties.getProperty("pipe.numberOfWorkers", "3"));

        ThreadFactory jobExecutorThreadFactory = new ThreadFactoryBuilder().setNameFormat("JobExecutor-%d").build();
        ExecutorService jobExecutorService = Executors.newFixedThreadPool(numberOfWorkers, jobExecutorThreadFactory);
        ThreadFactory logExecutorThreadFactory = new ThreadFactoryBuilder().setNameFormat("JobLogger-%d").build();
        ExecutorService logExecutorService = Executors.newFixedThreadPool(numberOfWorkers, logExecutorThreadFactory);

        int jobLoggerSleep = Integer.parseInt(properties.getProperty("pipe.jobLoggerSleep", "1000"));
        JobExecutionFactory jobExecutionFactory =
            new JobExecutionFactory(pipeControl, jobQueueSubmitter, jobLoggerSleep);

        int jobQueuePollerSleep = Integer.parseInt(properties.getProperty("pipe.jobQueuePollerSleep", "1000"));
        this.jobQueuePoller = new JobQueuePoller(pipeControl, jobQueue, jobExecutorService,
            logExecutorService, jobExecutionFactory, jobQueuePollerSleep);
        this.executorServices = ImmutableList.of(jobExecutorService, logExecutorService);
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
        jobQueueSubmitter.start();
        new Thread(jobQueuePoller, "JobQueuePoller").start();

        // await completion
        while(pipeControl.run()) {
            log.info("Waiting for {} unfinished jobs", pipeControl.jobsLeft());
            try {
                Thread.sleep(10000);
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
