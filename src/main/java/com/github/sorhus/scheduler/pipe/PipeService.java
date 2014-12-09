package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.job.model.JobContainer;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;


/**
 * @author: anton.sorhus@gmail.com
 */
public class PipeService {

    private final ExecutorService executorService;
    private final Map<String, Pipe> pipes;
    private final Gson gson;

    private final static Logger log = LoggerFactory.getLogger(PipeService.class);

    public PipeService(ExecutorService executorService) {
        this.executorService = executorService;
        this.pipes = new HashMap<>();
        this.gson = new Gson();
    }

    public synchronized boolean submit(String name, List<String> specificationStrings, Integer workers) {
        try {
            log.info("Incoming pipe submission: {}. spec size {}", name, specificationStrings.size());
            log.info("JobSpecifications: {}", Joiner.on(",").join(specificationStrings));
            JobContainer jobContainer = new JobContainer(specificationStrings);
            Pipe pipe = new Pipe(jobContainer, Optional.fromNullable(workers).or(3));
            log.info("Pipe instantiated: {}", pipe);
            pipes.put(name, pipe);
            executorService.submit(pipe);
            return true;
        } catch (RuntimeException e) {
            log.warn("Could not instantiate Pipe", e);
            return false;
        }
    }

    public synchronized boolean exists(String name) {
        return pipes.containsKey(name);
    }

    public synchronized String info(String name) {
        return exists(name) ?
            pipes.get(name).toString() :
            "Pipe not found";
    }

    public synchronized String status(String name) {
        return exists(name) ?
            pipes.get(name).getJobContainer().toString() :
            "Pipe not found";
    }

    public synchronized void abort(String name) {
        if(pipes.containsKey(name)) {
            pipes.get(name).abort();
            pipes.remove(name);
        }
    }

}
