package com.github.sorhus.scheduler.webapp;

import com.github.sorhus.scheduler.JobSpecification;
import com.github.sorhus.scheduler.Pipe;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * @author: anton.sorhus@gmail.com
 */
@Path("pipe")
public class PipeRestService {

    private final Gson gson = new Gson();
    private final Map<String, Pipe> pipes = new ConcurrentHashMap<>();

    private final Logger log = LoggerFactory.getLogger(PipeRestService.class);

    @GET
    @Path("start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response start(
        @QueryParam("name") String name,
        @QueryParam("workers") Integer workers,
        @QueryParam("specification") List<String> specifications
    ) {
        if(null == name) {
            log.info("No name specified, aborting");
            return Response.status(BAD_REQUEST).entity("No name specified").build();
        }

        log.info("spec size {}", specifications.size());

        try {
            List<JobSpecification> jobSpecifications = getJobSpecFromStrings(specifications);
            log.info("JobSpecifications: {}", Joiner.on(",").join(specifications));
            Pipe pipe = new Pipe(jobSpecifications, Optional.fromNullable(workers).or(3));
            log.info("Pipe instantiated: {}", pipe);
            pipes.put(name, pipe);
            new Thread(pipe).start();
            return Response.status(OK).entity("Pipe started").build();
        } catch (RuntimeException e) {
            return Response.status(BAD_REQUEST).entity("Could not instantiate Pipe").build();
        }
    }

    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response info(@QueryParam("name") String name) {
        return pipes.containsKey(name) ?
            Response.status(OK).entity(pipes.get(name).toString()).build() :
            Response.status(NOT_FOUND).entity("Unknown pipe").build();
    }

    @GET
    @Path("abort")
    @Produces(MediaType.APPLICATION_JSON)
    public Response clear(@QueryParam("name") String name) {
        if(pipes.containsKey(name)) {
            pipes.get(name).abort();
            pipes.remove(name);
            return Response.status(OK).build();
        } else {
            return Response.status(NOT_FOUND).entity("Unknown pipe").build();
        }
    }

    public List<JobSpecification> getJobSpecFromStrings(List<String> jobSpecs) {
        ImmutableList.Builder<JobSpecification> builder = ImmutableList.builder();
        for(String json : jobSpecs) {
            JobSpecification jobSpecification = gson.fromJson(json, JobSpecification.class);
            builder.add(jobSpecification);
        }
        return builder.build();
    }

}