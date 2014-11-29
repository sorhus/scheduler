package com.github.sorhus.scheduler;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: anton.sorhus@gmail.com
 */
public class Job {

    private final String name;
    private final String parameters;
    private final List<Job> dependencies;
    private final List<Job> dependents;

    private boolean done;
    private boolean dormant;

    public Job(String name) {
        this(name, "");
    }

    public Job(String name, String parameters) {
        this.name = name;
        this.parameters = parameters;
        this.dependencies = new LinkedList<>();
        this.dependents = new LinkedList<>();
        this.done = false;
        this.dormant = true;
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
        dependencies.add(dependency);
    }

    public List<Job> getDependents() {
        return dependents;
    }

    public void addDependent(Job dependent) {
        dependents.add(dependent);
    }

    public void setDone(boolean value) {
        done = value;
    }

    public boolean isDone() {
        return done;
    }

    public void setDormant(boolean value) {
        dormant = value;
    }

    public boolean isDormant() {
        return dormant;
    }



    @Override
    public String toString() {
        return name + (parameters.length() == 0 ? "" : "-" + parameters);
    }
}
