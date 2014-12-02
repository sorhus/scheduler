package com.github.sorhus.scheduler.job;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobContainer {

    private final List<JobSpecification> jobSpecifications;
    private final int numberOfJobs;

    public JobContainer(List<JobSpecification> jobSpecifications) {
        this.jobSpecifications = jobSpecifications;
        this.numberOfJobs = createJobs();
        resolveDependencies();
    }

    public int getNumberOfJobs() {
        return numberOfJobs;
    }

    public List<Job> getEntryPoints() {
        ImmutableList.Builder<Job> builder = ImmutableList.builder();
        for (JobSpecification jobSpecification : jobSpecifications) {
            if(jobSpecification.getDependencies().isEmpty()) {
                for (Job job : jobSpecification.getJobs()) {
                    builder.add(job);
                }
            }
        }
        return builder.build();
    }

    private int createJobs() {
        int numberOfJobs = 0;
        for (JobSpecification jobSpecification : jobSpecifications) {
            if(jobSpecification.getParameters().isEmpty()) {
                Job job = new Job(jobSpecification.getName());
                ImmutableList<Job> jobs = ImmutableList.of(job);
                jobSpecification.setJobs(jobs);
                numberOfJobs++;
            } else {
                ImmutableList.Builder<Job> builder = ImmutableList.builder();
                for (String param : jobSpecification.getParameters()) {
                    Job job = new Job(jobSpecification.getName(), param);
                    builder.add(job);
                }
                List<Job> jobs = builder.build();
                jobSpecification.setJobs(jobs);
                numberOfJobs += jobs.size();
            }
        }
        return numberOfJobs;
    }

    private void resolveDependencies() {
        Map<String, JobSpecification> jobSpecsByName = new HashMap<>(jobSpecifications.size());
        for (JobSpecification jobSpecification : jobSpecifications) {
            jobSpecsByName.put(jobSpecification.getName(), jobSpecification);
        }
        for (JobSpecification jobSpecification : jobSpecifications) {
            for (String dependencyName : jobSpecification.getDependencies()) {
                for (Job dependent : jobSpecification.getJobs()) {
                    for(Job dependency : jobSpecsByName.get(dependencyName).getJobs()) {
                        dependent.addDependency(dependency);
                        dependency.addDependent(dependent);
                    }
                }
            }
        }
        for (JobSpecification jobSpecification : jobSpecsByName.values()) {
            for (Job job : jobSpecification.getJobs()) {
                job.finalise();
            }
        }
    }
}
