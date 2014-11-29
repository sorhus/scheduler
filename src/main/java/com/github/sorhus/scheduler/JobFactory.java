package com.github.sorhus.scheduler;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobFactory {

    private final int nJobs;
    private final List<Job> entryPoints;

    public JobFactory(List<JobSpecification> jobSpecifications) {
        this.nJobs = createJobs(jobSpecifications);
        resolveDependencies(jobSpecifications);
        this.entryPoints = getEntryPoints(jobSpecifications);
    }

    public int getNumberOfJobs() {
        return nJobs;
    }

    public List<Job> getEntryPoints() {
        return entryPoints;
    }

    private int createJobs(List<JobSpecification> jobSpecifications) {
        int nJobs = 0;
        for (JobSpecification jobSpecification : jobSpecifications) {
            if(jobSpecification.getParameters().isEmpty()) {
                Job job = new Job(jobSpecification.getName());
                ImmutableList<Job> jobs = ImmutableList.of(job);
                jobSpecification.setJobs(jobs);
                nJobs++;
            } else {
                ImmutableList.Builder<Job> builder = ImmutableList.builder();
                for (String param : jobSpecification.getParameters()) {
                    Job job = new Job(jobSpecification.getName(), param);
                    builder.add(job);
                }
                List<Job> jobs = builder.build();
                jobSpecification.setJobs(jobs);
                nJobs += jobs.size();
            }
        }
        return nJobs;
    }

    private void resolveDependencies(List<JobSpecification> jobSpecifications) {
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

    private List<Job> getEntryPoints(List<JobSpecification> jobSpecifications) {
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
}