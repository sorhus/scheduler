package com.github.sorhus.scheduler.job;

import com.github.sorhus.scheduler.pipe.control.JobStatus;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobContainer {

    private final Map<String, JobSpecification> specifications;
    private final int numberOfJobs;

    public JobContainer(List<String> specificationStrings) {
        this.specifications = getJobSpecificationsFromStrings(specificationStrings);
        this.numberOfJobs = createJobs();
        resolveDependencies();
    }

    public int getNumberOfJobs() {
        return numberOfJobs;
    }

    public Map<String, JobSpecification> getSpecifications() {
        return specifications;
    }

    private Map<String, JobSpecification> getJobSpecificationsFromStrings(List<String> specificationString) {
        Gson gson = new Gson();
        Map<String, JobSpecification> specifications = new HashMap<>(specificationString.size());
        for(String json : specificationString) {
            JobSpecification specification = gson.fromJson(json, JobSpecification.class);
            specifications.put(specification.getName(), specification);
        }

        Map<String, Integer> maxDepthsCache = new HashMap<>();
        for (JobSpecification specification : specifications.values()) {
            int maxDepth = getMaxDepth(specification.getName(), specifications, maxDepthsCache);
            specification.setDepth(maxDepth);
        }

        Function<String, JobSpecification> lookup = Functions.forMap(specifications);
        Ordering<String> valueOrdering = Ordering.natural().onResultOf(lookup);
        return ImmutableSortedMap.copyOf(specifications, valueOrdering);
    }

    private int getMaxDepth(String name, Map<String, JobSpecification> specifications, Map<String, Integer> maxDepthsCache) {
        if(maxDepthsCache.containsKey(name)) {
            return maxDepthsCache.get(name);
        } else if(specifications.get(name).getDependencies().isEmpty()) {
            return 0;
        } else {
            SortedMap<String, Integer> depths = new TreeMap<>();
            for (String dependency : specifications.get(name).getDependencies()) {
                int maxDepth = getMaxDepth(specifications.get(dependency).getName(), specifications, maxDepthsCache);
                depths.put(dependency, maxDepth);
            }
            return 1 + depths.get(depths.lastKey());
        }
    }

    public List<Job> getEntryPoints() {
        ImmutableList.Builder<Job> builder = ImmutableList.builder();
        for (JobSpecification jobSpecification : specifications.values()) {
            if(jobSpecification.getDependencies().isEmpty()) {
                builder.addAll(jobSpecification.getJobs());
            } else {
                return builder.build();
            }
        }
        return builder.build();
    }

    private int createJobs() {
        int numberOfJobs = 0;
        for (JobSpecification jobSpecification : specifications.values()) {
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
        for (JobSpecification jobSpecification : specifications.values()) {
            for (String dependencyName : jobSpecification.getDependencies()) {
                for (Job dependent : jobSpecification.getJobs()) {
                    for(Job dependency : specifications.get(dependencyName).getJobs()) {
                        dependent.addDependency(dependency);
                        dependency.addDependent(dependent);
                    }
                }
            }
        }
        for (JobSpecification jobSpecification : specifications.values()) {
            for (Job job : jobSpecification.getJobs()) {
                job.finalise();
            }
        }
    }

    @Override
    public String toString() {
        JsonArray jsons = new JsonArray();
        for (JobSpecification specification : specifications.values()) {
            jsons.add(specification.asJson());
        }
        return jsons.toString();
    }

    public boolean pause(String name) {
        boolean success = true;
        for (Job job : specifications.get(name).getJobs()) {
            synchronized (job) {
                if(job.getStatus() != JobStatus.RUNNING && job.getStatus() != JobStatus.DONE) {
                    job.setStatus(JobStatus.PAUSED);
                } else {
                    success = false;
                }
            }
        }
        return success;
    }

    public boolean unpause(String name) {
        boolean success = true;
        for (Job job : specifications.get(name).getJobs()) {
            synchronized (job) {
                if(job.getStatus() == JobStatus.PAUSED) {
                    // requires new thread to continuously figure out status
                } else {
                    success = false;
                }
            }
        }
        return success;
    }
}
