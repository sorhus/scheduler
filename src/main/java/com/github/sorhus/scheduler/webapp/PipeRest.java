package com.github.sorhus.scheduler.webapp;

import com.github.sorhus.scheduler.pipe.PipeService;
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response start(
        @QueryParam("name") String name,
        @QueryParam("workers") Integer workers,
        @QueryParam("specification") List<String> specifications
    ) {
        return pipeService.submit(name, specifications, workers) ?
            Response.status(OK).entity("Pipe started").build() :
            Response.status(BAD_REQUEST).entity("Could not instantiate Pipe").build();
    }

    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response info(@QueryParam("name") String name) {
        return Response.status(OK).entity(pipeService.info(name)).build();
    }

    @GET
    @Path("abort")
    @Produces(MediaType.APPLICATION_JSON)
    public Response abort(@QueryParam("name") String name) {
        pipeService.abort(name);
        return Response.status(OK).build();
    }


}