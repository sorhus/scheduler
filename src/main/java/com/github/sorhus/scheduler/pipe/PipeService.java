package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.pipe.control.SimplePipeControlFactory;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 * @author: anton.sorhus@gmail.com
 */
public class PipeService {

    private final ExecutorService executorService;
    private final Map<String, Pipe> pipes;

    private final static Logger log = LoggerFactory.getLogger(PipeService.class);

    public PipeService(ExecutorService executorService) {
        this.executorService = executorService;
        this.pipes = new HashMap<>();
    }

    public synchronized boolean submit(String name, List<String> specificationStrings, Integer workers) {
        try {
            if(pipes.containsKey(name)) {
                throw new RuntimeException("Pipe already submitted");
            }
            log.info("Incoming pipe submission: {}. spec size {}", name, specificationStrings.size());
            log.info("JobSpecifications: {}", Joiner.on(",").join(specificationStrings));
            Properties properties = new Properties();
            properties.setProperty("pipe.numberOfWorkers", String.valueOf(workers));
            Pipe pipe = new Pipe(specificationStrings, new SimplePipeControlFactory(), properties);
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
