package com.github.sorhus.scheduler.job;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * @author: anton.sorhus@gmail.com
 */
public class Job {

    private final String name;
    private final String parameters;

    private List<Job> dependencies;
    private List<Job> dependents;

    private JobStatus status;

    private ImmutableList.Builder<Job> dependenciesBuilder = ImmutableList.builder();
    private ImmutableList.Builder<Job> dependentsBuilder = ImmutableList.builder();

    public Job(String name) {
        this(name, "");
    }

    public Job(String name, String parameters) {
        this.name = name;
        this.parameters = parameters;
        this.status = JobStatus.WAITING;
    }

    public String getName() {
        return name;
    }

    public String getParameters() {
        return parameters;
    }

    public List<Job> getDependencies() {
        return dependencies;
    }

    public void addDependency(Job dependency) {
        dependenciesBuilder.add(dependency);
    }

    public List<Job> getDependents() {
        return dependents;
    }

    public void addDependent(Job dependent) {
        dependentsBuilder.add(dependent);
    }

    protected void setStatus(JobStatus status) {
        this.status = status;
    }

    public JobStatus getStatus() {
        return status;
    }

    public boolean available() {
        boolean available = status == JobStatus.WAITING;
        for (Job dependency : dependencies) {
            available &= dependency.getStatus() == JobStatus.DONE;
        }
        return available;
    }

    public void finalise() {
        dependencies = dependenciesBuilder.build();
        dependenciesBuilder = null;
        dependents = dependentsBuilder.build();
        dependentsBuilder = null;
    }

    @Override
    public String toString() {
        return parameters.length() == 0 ? name : name + "-" + parameters;
    }

    public JsonObject asJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", toString());
        json.addProperty("status", status.toString());
        return json;
    }

    public void evaluate() {
        // figure out if this job has unfinished dependencies or not.
    }
}
