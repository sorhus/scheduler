package com.github.sorhus.scheduler.job;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
        return name;
    }

    public JsonObject asJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        JsonArray dependencies = new JsonArray();
        for (String dependency : this.dependencies) {
            dependencies.add(new JsonPrimitive(dependency));
        }
        json.add("dependencies", dependencies);
        JsonArray jobs = new JsonArray();
        for (Job job : this.jobs) {
            jobs.add(job.asJson());
        }
        json.add("jobs", jobs);
        return json;
    }

    @Override
    public int compareTo(JobSpecification that) {
        if(depth == INVALID) {
            throw new RuntimeException("Depth not initialised");
        }
        return this.depth - that.depth;
    }
}

