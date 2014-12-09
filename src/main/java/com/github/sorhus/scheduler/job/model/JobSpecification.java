package com.github.sorhus.scheduler.job.model;

import java.util.List;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobSpecification implements Comparable<JobSpecification> {

    private String name;
    private String description;
    private List<String> parameters;
    private List<String> dependencies;
    private List<Job> jobs;
    private int depth = INVALID;
    private final static int INVALID = -1;

    public JobSpecification(String name, String description, List<String> parameters, List<String> dependencies) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    protected List<Job> getJobs() {
        return jobs;
    }

    protected void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(JobSpecification that) {
        if(depth == INVALID) {
            throw new RuntimeException("Depth not initialised");
        }
        return this.depth - that.depth;
    }
}

