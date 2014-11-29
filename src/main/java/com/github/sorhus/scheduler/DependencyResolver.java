package com.github.sorhus.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author: anton.sorhus@gmail.com
 */
public class DependencyResolver {

    private final JobFactory jobFactory;

    private final static Logger log = LoggerFactory.getLogger(DependencyResolver.class);

    public DependencyResolver(JobFactory jobFactory) {
        this.jobFactory = jobFactory;
    }


}
