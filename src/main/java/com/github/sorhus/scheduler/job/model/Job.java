package com.github.sorhus.scheduler.job.model;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
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

    private Status status;

    private ImmutableList.Builder<Job> dependenciesBuilder = ImmutableList.builder();
    private ImmutableList.Builder<Job> dependentsBuilder = ImmutableList.builder();

    public Job(String name) {
        this(name, "");
    }

    public Job(String name, String parameters) {
        this.name = name;
        this.parameters = parameters;
        this.status = Status.WAITING;
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

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
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
}
