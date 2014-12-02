package com.github.sorhus.scheduler;


import com.github.sorhus.scheduler.job.JobSpecification;
import com.github.sorhus.scheduler.pipe.Pipe;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.util.List;

/**
 * @author: anton.sorhus@gmail.com
 */
public class Main {

    public static void main(String[] args) throws Exception {

        List<String> jobSpecsArg = Lists.newArrayList(
            "{\"name\":\"job_a\",\"description\":\"\",\"parameters\":[\"foo\"],\"dependencies\":[]}",
            "{\"name\":\"job_b\",\"description\":\"\",\"parameters\":[\"bar\", \"foo\"],\"dependencies\":[\"job_a\"]}",
            "{\"name\":\"job_c\",\"description\":\"\",\"parameters\":[\"f o o\"],\"dependencies\":[\"job_a\", \"job_b\"]}"
        );
        List<JobSpecification> jobSpecifications = getJobSpecFromString(jobSpecsArg);

        new Pipe(jobSpecifications, 2);

    }


    public static List<JobSpecification> getJobSpecFromString(List<String> jobSpecs) {
        Gson gson = new Gson();
        ImmutableList.Builder<JobSpecification> builder = ImmutableList.builder();
        for(String json : jobSpecs) {
            JobSpecification jobSpecification = gson.fromJson(json, JobSpecification.class);
            builder.add(jobSpecification);
        }
        return builder.build();
    }
}
