package com.github.sorhus.scheduler.webapp;

import com.github.sorhus.scheduler.pipe.PipeService;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * @author: anton.sorhus@gmail.com
 */
@Path("pipe")
public class PipeRest {

    private final PipeService pipeService;
    private final Gson gson = new Gson();
    private final Logger log = LoggerFactory.getLogger(PipeRest.class);

    public PipeRest(PipeService pipeService) {
        this.pipeService = pipeService;
    }

    @GET
    @Path("start")
    @Produces(MediaType.TEXT_HTML)
    public Response start(
        @QueryParam("name") String name,
        @QueryParam("specification") List<String> specifications,
        @QueryParam("workers") Integer workers
    ) {
        return pipeService.submit(name, specifications, workers) ?
            Response.status(OK).entity("Pipe started").build() :
            Response.status(BAD_REQUEST).entity("Could not instantiate Pipe").build();
    }

    @GET
    @Path("info")
    @Produces(MediaType.TEXT_HTML)
    public Response info(@QueryParam("name") String name) {
        return Response.status(OK).entity(pipeService.info(name)).build();
    }

    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response status(@QueryParam("name") String name) {
        return Response.status(OK).entity(pipeService.status(name)).build();
    }

    @GET
    @Path("abort")
    @Produces(MediaType.TEXT_HTML)
    public Response abort(@QueryParam("name") String name) {
        pipeService.abort(name);
        return Response.status(OK).build();
    }

    @GET
    @Path("test")
    @Produces(MediaType.TEXT_HTML)
    public Response test() {
        List<String> jobSpecsArg = Lists.newArrayList(
            "{\"name\":\"job_a\",\"description\":\"\",\"parameters\":[\"foo\"],\"dependencies\":[]}",
            "{\"name\":\"job_b\",\"description\":\"\",\"parameters\":[\"bar\", \"foo\"],\"dependencies\":[\"job_a\"]}",
            "{\"name\":\"job_c\",\"description\":\"\",\"parameters\":[\"f o o\"],\"dependencies\":[\"job_a\", \"job_b\"]}"
        );
        return start("test-pipe", jobSpecsArg, 2);
    }

// TODO
//    @GET
//    @Path("pause")
//    @Produces(MediaType.TEXT_HTML)
//    public Response pause(@QueryParam("name") String name, @QueryParam("job") String job) {
//        return pipeService.pause(name, job) ?
//            Response.status(OK).build() :
//            Response.status(BAD_REQUEST).build();
//    }
//
//    @GET
//    @Path("unpause")
//    @Produces(MediaType.TEXT_HTML)
//    public Response unpause(@QueryParam("name") String name, @QueryParam("job") String job) {
//        return pipeService.unpause(name, job) ?
//            Response.status(OK).build() :
//            Response.status(BAD_REQUEST).build();
//    }
}