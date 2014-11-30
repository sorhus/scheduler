package com.github.sorhus.scheduler.webapp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class Starter {

    public static void main(String[] args) throws Exception {
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.setContextPath("/");

        Server jettyServer = new Server(8080);

        ResourceConfig resourceConfig = new ResourceConfig().register(new PipeRestService());
        contextHandler.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/*");

        jettyServer.setHandler(contextHandler);

//        ServletHolder jerseyServlet = contextHandler.addServlet(
//            org.glassfish.jersey.servlet.ServletContainer.class, "/*");
//        jerseyServlet.setInitOrder(0);
//
//        jerseyServlet.setInitParameter(
//            "jersey.config.server.provider.classnames",
//            PipeRestService.class.getCanonicalName()
//        );

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }
}