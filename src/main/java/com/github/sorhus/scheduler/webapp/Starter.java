package com.github.sorhus.scheduler.webapp;

import com.github.sorhus.scheduler.pipe.PipeService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.concurrent.Executors;

public class Starter {

    public static void main(String[] args) throws Exception {
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        Server jettyServer = new Server(8080);

        PipeRest pipeRest = new PipeRest(new PipeService(Executors.newFixedThreadPool(2)));
        ResourceConfig resourceConfig = new ResourceConfig().register(pipeRest);
        contextHandler.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/*");

        jettyServer.setHandler(contextHandler);

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }
}