package com.github.sorhus.scheduler.job;

import java.util.List;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobSpecification {

    private String name;
    private String description;
    private List<String> parameters;
    private List<String> dependencies;
    private List<Job> jobs;

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


    @Override
    public String toString() {
        return getName();
    }
}

